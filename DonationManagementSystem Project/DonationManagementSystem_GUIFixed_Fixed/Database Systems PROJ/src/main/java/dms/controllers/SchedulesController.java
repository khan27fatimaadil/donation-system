package dms.controllers;

import dao.RecurringPlanDAO;
import dao.DonorDAO;
import dao.CampaignDAO;
import model.RecurringPlan;
import model.Donor;
import model.Campaign;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SchedulesController implements Initializable {

    @FXML private TableView<RecurringPlan> scheduleTable;
    @FXML private TableColumn<RecurringPlan, String> colDonorName;
    @FXML private TableColumn<RecurringPlan, String> colCause;
    @FXML private TableColumn<RecurringPlan, String> colFrequency;
    @FXML private TableColumn<RecurringPlan, String> colNextDue;
    @FXML private TableColumn<RecurringPlan, String> colStatus;

    private final ObservableList<RecurringPlan> scheduleList = FXCollections.observableArrayList();
    private final RecurringPlanDAO planDAO     = new RecurringPlanDAO();
    private final DonorDAO         donorDAO    = new DonorDAO();
    private final CampaignDAO      campaignDAO = new CampaignDAO();

    // Caches for resolving IDs → names in table
    private List<Donor>    donorCache;
    private List<Campaign> campaignCache;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        donorCache    = donorDAO.getAllDonors();
        campaignCache = campaignDAO.getAllCampaigns();

        // Show donor name instead of raw donorId — use organizationName as fallback for org donors
        colDonorName.setCellValueFactory(cd -> {
            String donorId = cd.getValue().getDonorId();
            String name = donorCache.stream()
                .filter(d -> d.getId().equals(donorId))
                .map(d -> {
                    String full = ((d.getFirstName() != null ? d.getFirstName() : "")
                                + (d.getLastName()  != null ? " " + d.getLastName() : "")).trim();
                    if (!full.isEmpty()) return full;
                    if (d.getOrganizationName() != null && !d.getOrganizationName().trim().isEmpty())
                        return d.getOrganizationName().trim();
                    return d.getEmail() != null ? d.getEmail() : "Unknown Donor";
                })
                .findFirst().orElse("Unknown Donor");
            return new SimpleStringProperty(name);
        });

        // Show campaign title instead of raw campaignId
        colCause.setCellValueFactory(cd -> {
            String campId = cd.getValue().getCampaignId();
            String title = campaignCache.stream()
                .filter(c -> c.getId().equals(campId))
                .map(Campaign::getTitle)
                .findFirst().orElse(campId != null ? campId.substring(0, Math.min(8, campId.length())) + "…" : "—");
            return new SimpleStringProperty(title);
        });

        colFrequency.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getFrequency() != null ? cd.getValue().getFrequency().name() : "—"));

        colNextDue.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getNextChargeDate() != null ? cd.getValue().getNextChargeDate().toString() : "—"));

        colStatus.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getStatus() != null ? cd.getValue().getStatus().name() : "—"));

        loadSchedules();
    }

    private void loadSchedules() {
        scheduleList.setAll(planDAO.getAllSchedules());
        scheduleTable.setItems(scheduleList);
    }

    @FXML
    public void openAddDialog() {
        RecurringPlan p = showScheduleDialog("Add New Schedule", null);
        if (p != null) {
            boolean success = planDAO.addSchedule(p);
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Schedule added successfully!", ButtonType.OK).showAndWait();
                loadSchedules();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to add Schedule. Check database.", ButtonType.OK).showAndWait();
            }
        }
    }

    @FXML
    public void openEditDialog() {
        RecurringPlan selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a schedule to edit.", ButtonType.OK).showAndWait();
            return;
        }
        RecurringPlan updated = showScheduleDialog("Edit Schedule", selected);
        if (updated != null) {
            boolean success = planDAO.updateSchedule(updated);
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Schedule updated successfully!", ButtonType.OK).showAndWait();
                loadSchedules();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to update Schedule.", ButtonType.OK).showAndWait();
            }
        }
    }

    @FXML
    public void deleteSchedule() {
        RecurringPlan selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a schedule to delete.", ButtonType.OK).showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this recurring schedule?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            boolean success = planDAO.deleteSchedule(selected.getId());
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Schedule deleted successfully!", ButtonType.OK).showAndWait();
                loadSchedules();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to delete schedule.", ButtonType.OK).showAndWait();
            }
        }
    }

    @FXML
    public void toggleActive() {
        RecurringPlan selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a plan to toggle.", ButtonType.OK).showAndWait();
            return;
        }
        boolean success = planDAO.toggleActive(selected.getId());
        if (success) {
            new Alert(Alert.AlertType.INFORMATION, "Status toggled successfully!", ButtonType.OK).showAndWait();
            loadSchedules();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to toggle status.", ButtonType.OK).showAndWait();
        }
    }

    private RecurringPlan showScheduleDialog(String title, RecurringPlan existing) {
        Dialog<RecurringPlan> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(12);
        grid.setPadding(new Insets(25));

        // Typed combos with proper StringConverters
        ComboBox<Donor> cbDonor = new ComboBox<>(FXCollections.observableArrayList(donorCache));
        cbDonor.setConverter(new javafx.util.StringConverter<Donor>() {
            public String toString(Donor d) {
                if (d == null) return "";
                String full = ((d.getFirstName() != null ? d.getFirstName() : "")
                             + (d.getLastName()  != null ? " " + d.getLastName() : "")).trim();
                if (!full.isEmpty()) return full;
                if (d.getOrganizationName() != null && !d.getOrganizationName().trim().isEmpty())
                    return d.getOrganizationName().trim();
                return d.getEmail() != null ? d.getEmail() : "Unknown Donor";
            }
            public Donor fromString(String s) { return null; }
        });
        cbDonor.setPromptText("Select Donor...");

        ComboBox<Campaign> cbCause = new ComboBox<>(FXCollections.observableArrayList(campaignCache));
        cbCause.setConverter(new javafx.util.StringConverter<Campaign>() {
            public String toString(Campaign c) { return c == null ? "" : (c.getTitle() != null ? c.getTitle() : "(Untitled)"); }
            public Campaign fromString(String s) { return null; }
        });
        cbCause.setPromptText("Select Campaign...");

        ComboBox<String> cbFreq = new ComboBox<>(FXCollections.observableArrayList("weekly", "monthly", "quarterly", "annually"));
        DatePicker dpNext = new DatePicker();

        if (existing != null) {
            donorCache.stream().filter(d -> d.getId().equals(existing.getDonorId())).findFirst().ifPresent(cbDonor::setValue);
            campaignCache.stream().filter(c -> c.getId().equals(existing.getCampaignId())).findFirst().ifPresent(cbCause::setValue);
            cbFreq.setValue(existing.getFrequency() != null ? existing.getFrequency().name() : "monthly");
            if (existing.getNextChargeDate() != null) dpNext.setValue(existing.getNextChargeDate());
        } else {
            cbFreq.setValue("monthly");
        }

        grid.add(new Label("Donor: *"),         0, 0); grid.add(cbDonor, 1, 0);
        grid.add(new Label("Campaign: *"),       0, 1); grid.add(cbCause, 1, 1);
        grid.add(new Label("Frequency: *"),      0, 2); grid.add(cbFreq,  1, 2);
        grid.add(new Label("Next Due Date: *"), 0, 3); grid.add(dpNext,  1, 3);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                if (cbDonor.getValue() == null) {
                    new Alert(Alert.AlertType.WARNING, "Please select a donor.", ButtonType.OK).showAndWait();
                    return null;
                }
                if (cbCause.getValue() == null) {
                    new Alert(Alert.AlertType.WARNING, "Please select a campaign.", ButtonType.OK).showAndWait();
                    return null;
                }
                if (dpNext.getValue() == null) {
                    new Alert(Alert.AlertType.WARNING, "Please select a next due date.", ButtonType.OK).showAndWait();
                    return null;
                }
                RecurringPlan p = (existing == null) ? new RecurringPlan() : existing;
                p.setDonorId(cbDonor.getValue().getId());
                p.setCampaignId(cbCause.getValue().getId());
                p.setFrequency(RecurringPlan.Frequency.valueOf(cbFreq.getValue()));
                p.setNextChargeDate(dpNext.getValue());
                if (existing == null) p.setStatus(RecurringPlan.PlanStatus.active);
                return p;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }
}
