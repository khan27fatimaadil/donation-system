package dms.controllers;

import dao.CampaignDAO;
import dao.AnalyticsDAO;
import model.Campaign;
import dao.AllocationDAO;
import dao.FundDAO;
import model.Allocation;
import model.Fund;
import dms.exceptions.DatabaseOperationException;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class CausesController implements Initializable {

    @FXML private TableView<Campaign> causeTable;
    @FXML private TableColumn<Campaign, String> colTitle;
    @FXML private TableColumn<Campaign, String> colGoal;
    @FXML private TableColumn<Campaign, String> colRaised;
    @FXML private TableColumn<Campaign, String> colStatus;
    @FXML private TableColumn<Campaign, String> colPercent;

    private final ObservableList<Campaign> campaignList = FXCollections.observableArrayList();
    private final CampaignDAO campaignDAO = new CampaignDAO();
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();
    // Key = campaign ID (reliable, unique) → progress data
    private Map<String, Map<String, Object>> progressCache = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colTitle.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getTitle() != null ? cd.getValue().getTitle() : "(Untitled)"));

        colGoal.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getGoalAmount() != null
                ? String.format("%,.0f", cd.getValue().getGoalAmount()) : "—"));

        colStatus.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getStatus() != null
                ? cd.getValue().getStatus().name() : "—"));

        // Raised: look up from progressCache by campaign ID
        if (colRaised != null) colRaised.setCellValueFactory(cd -> {
            Map<String, Object> p = progressCache.get(cd.getValue().getId());
            if (p == null) return new SimpleStringProperty("0");
            Object raised = p.get("raised");
            if (raised == null) return new SimpleStringProperty("0");
            try {
                return new SimpleStringProperty(String.format("%,.0f", new BigDecimal(raised.toString())));
            } catch (Exception e) {
                return new SimpleStringProperty("0");
            }
        });

        // Percent: look up from progressCache by campaign ID
        if (colPercent != null) colPercent.setCellValueFactory(cd -> {
            Map<String, Object> p = progressCache.get(cd.getValue().getId());
            if (p == null) return new SimpleStringProperty("0%");
            Object pct = p.get("percent");
            return new SimpleStringProperty(pct != null ? pct + "%" : "0%");
        });

        loadCampaigns();
    }

    private void loadCampaigns() {
        // Rebuild progress cache keyed by campaign ID (not title — titles can be null/duplicate)
        progressCache.clear();
        try {
            for (Map<String, Object> row : analyticsDAO.getCauseProgress()) {
                String id = row.get("campaignId") != null ? row.get("campaignId").toString() : null;
                if (id != null) {
                    progressCache.put(id, row);
                }
            }
        } catch (Exception e) {
            System.err.println("Progress cache load error: " + e.getMessage());
        }
        campaignList.setAll(campaignDAO.getAllCampaigns());
        causeTable.setItems(campaignList);
    }

    @FXML
    public void openAddDialog() {
        Campaign newCamp = showCampaignDialog("Add New Cause", null);
        if (newCamp != null) {
            if (newCamp.getTitle() == null || newCamp.getTitle().trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Cause title cannot be empty.", ButtonType.OK).showAndWait();
                return;
            }
            boolean success = campaignDAO.addCampaign(newCamp);
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Cause added successfully!", ButtonType.OK).showAndWait();
                loadCampaigns();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to add Cause. Check database.", ButtonType.OK).showAndWait();
            }
        }
    }

    @FXML
    public void openEditDialog() {
        Campaign selected = causeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a cause to edit.", ButtonType.OK).showAndWait();
            return;
        }
        Campaign updated = showCampaignDialog("Edit Cause", selected);
        if (updated != null) {
            if (updated.getTitle() == null || updated.getTitle().trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Cause title cannot be empty.", ButtonType.OK).showAndWait();
                return;
            }
            boolean success = campaignDAO.updateCampaign(updated);
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Cause updated successfully!", ButtonType.OK).showAndWait();
                loadCampaigns();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to update Cause.", ButtonType.OK).showAndWait();
            }
        }
    }

    @FXML
    public void deleteCause() {
        Campaign selected = causeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a cause to delete.", ButtonType.OK).showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete cause \"" + selected.getTitle() + "\"?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            boolean success = campaignDAO.deleteCampaign(selected.getId());
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Cause deleted successfully!", ButtonType.OK).showAndWait();
                loadCampaigns();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to delete cause. It may have linked donations or schedules.", ButtonType.OK).showAndWait();
            }
        }
    }

    @FXML
    public void allocateFund() {
        Campaign selected = causeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a cause to allocate funds to.", ButtonType.OK).showAndWait();
            return;
        }
        List<Fund> funds = new FundDAO().getAllFunds();
        if (funds.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No source funds found. Add funds first.", ButtonType.OK).showAndWait();
            return;
        }

        Dialog<Allocation> dialog = new Dialog<>();
        dialog.setTitle("Allocate Funds to Cause");
        dialog.setHeaderText("Allocating to: " + selected.getTitle());
        ButtonType allocBtn = new ButtonType("Allocate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(allocBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(420);

        ComboBox<Fund> cbFund = new ComboBox<>(FXCollections.observableArrayList(funds));
        cbFund.setConverter(new javafx.util.StringConverter<Fund>() {
            public String toString(Fund f) { return f == null ? "" : f.getName(); }
            public Fund fromString(String s) { return null; }
        });
        cbFund.setPromptText("Select Source Fund");
        cbFund.setPrefWidth(260);

        TextField tfAmount = new TextField(); tfAmount.setPromptText("Amount to Allocate");
        tfAmount.setPrefWidth(260);

        grid.add(new Label("Source Fund:"), 0, 0); grid.add(cbFund, 1, 0);
        grid.add(new Label("Amount:"),      0, 1); grid.add(tfAmount, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == allocBtn) {
                if (cbFund.getValue() == null) {
                    new Alert(Alert.AlertType.WARNING, "Please select a fund.", ButtonType.OK).showAndWait();
                    return null;
                }
                try {
                    java.math.BigDecimal amt = new java.math.BigDecimal(tfAmount.getText().trim());
                    if (amt.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                        new Alert(Alert.AlertType.WARNING, "Amount must be greater than zero.", ButtonType.OK).showAndWait();
                        return null;
                    }
                    Allocation a = new Allocation();
                    a.setFundId(cbFund.getValue().getId());
                    a.setAmount(amt);
                    a.setCreatedAt(java.time.LocalDateTime.now());
                    return a;
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Invalid amount. Enter a valid number.", ButtonType.OK).showAndWait();
                }
            }
            return null;
        });

        Allocation result = dialog.showAndWait().orElse(null);
        if (result != null) {
            try {
                boolean success = new AllocationDAO().addCampaignAllocation(
                    selected.getId(),
                    result.getFundId(),
                    result.getAmount()
                );
                if (success) {
                    new Alert(Alert.AlertType.INFORMATION, "Funds allocated successfully to " + selected.getTitle(), ButtonType.OK).showAndWait();
                    loadCampaigns();
                } else {
                    new Alert(Alert.AlertType.ERROR,
                        "Allocation failed.\n\nThis cause has no recorded donations yet.\n" +
                        "Please record at least one donation for \"" + selected.getTitle() + "\" before allocating funds.",
                        ButtonType.OK).showAndWait();
                }
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage(), ButtonType.OK).showAndWait();
            }
        }
    }

    private Campaign showCampaignDialog(String dialogTitle, Campaign existing) {
        Dialog<Campaign> dialog = new Dialog<>();
        dialog.setTitle(dialogTitle);
        dialog.setHeaderText(null);
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField tfTitle       = new TextField(); tfTitle.setPromptText("Cause Title");
        TextField tfGoal        = new TextField(); tfGoal.setPromptText("Goal Amount (e.g. 100000)");
        TextField tfDescription = new TextField(); tfDescription.setPromptText("Short description (optional)");
        ComboBox<String> cbStatus = new ComboBox<>(FXCollections.observableArrayList("active", "paused", "completed", "draft"));

        if (existing != null) {
            tfTitle.setText(existing.getTitle() != null ? existing.getTitle() : "");
            tfGoal.setText(existing.getGoalAmount() != null ? existing.getGoalAmount().toPlainString() : "");
            tfDescription.setText(existing.getDescription() != null ? existing.getDescription() : "");
            cbStatus.setValue(existing.getStatus() != null ? existing.getStatus().name() : "active");
        } else {
            cbStatus.setValue("active");
        }

        grid.add(new Label("Title: *"),      0, 0); grid.add(tfTitle,       1, 0);
        grid.add(new Label("Goal Amount:"),  0, 1); grid.add(tfGoal,        1, 1);
        grid.add(new Label("Description:"),  0, 2); grid.add(tfDescription, 1, 2);
        grid.add(new Label("Status:"),       0, 3); grid.add(cbStatus,      1, 3);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                String titleText = tfTitle.getText().trim();
                if (titleText.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Cause title is required.", ButtonType.OK).showAndWait();
                    return null;
                }
                Campaign c = (existing == null) ? new Campaign() : existing;
                c.setTitle(titleText);
                c.setDescription(tfDescription.getText().trim().isEmpty() ? null : tfDescription.getText().trim());
                try {
                    String goalStr = tfGoal.getText().trim();
                    if (!goalStr.isEmpty()) {
                        java.math.BigDecimal goal = new java.math.BigDecimal(goalStr);
                        if (goal.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                            new Alert(Alert.AlertType.WARNING, "Goal amount must be greater than zero.", ButtonType.OK).showAndWait();
                            return null;
                        }
                        c.setGoalAmount(goal);
                    }
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.WARNING, "Invalid goal amount. Enter a valid number.", ButtonType.OK).showAndWait();
                    return null;
                }
                c.setStatus(Campaign.Status.valueOf(cbStatus.getValue()));
                return c;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }
}
