package dms.controllers;

import dao.ImpactReportDAO;
import dao.CampaignDAO;
import model.Campaign;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ImpactReportsController implements Initializable {

    @FXML private ComboBox<Campaign> causeCombo;
    @FXML private TextField          fundsField;
    @FXML private TextArea           summaryArea;

    @FXML private TableView<Map<String, Object>>          reportTable;
    @FXML private TableColumn<Map<String, Object>, String> colCauseTitle;
    @FXML private TableColumn<Map<String, Object>, String> colSummary;
    @FXML private TableColumn<Map<String, Object>, String> colFunds;
    @FXML private TableColumn<Map<String, Object>, String> colDate;

    private final ImpactReportDAO reportDAO  = new ImpactReportDAO();
    private final CampaignDAO     campaignDAO = new CampaignDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colCauseTitle.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue().get("campaignTitle"))));
        // "summary" key used by submitted reports; fallback to status for campaign summary rows
        colSummary.setCellValueFactory(cd -> {
            Object s = cd.getValue().get("summary");
            if (s != null && !s.toString().trim().isEmpty()) return new SimpleStringProperty(s.toString());
            Object status = cd.getValue().get("status");
            Object pct    = cd.getValue().get("progressPct");
            String auto   = (status != null ? status.toString() : "—");
            if (pct != null) auto += "  (" + pct + "% funded)";
            return new SimpleStringProperty(auto);
        });
        colFunds.setCellValueFactory(cd -> {
            Object f = cd.getValue().get("fundsUsed");
            if (f == null) f = cd.getValue().get("raisedAmount");
            String display = f != null ? "PKR " + String.format("%,.0f", new BigDecimal(f.toString())) : "0";
            return new SimpleStringProperty(display);
        });
        colDate.setCellValueFactory(cd -> {
            // submitted reports have "reportedAt"; campaign summary rows have "startDate"
            Object val = cd.getValue().get("reportedAt");
            if (val == null) val = cd.getValue().get("startDate");
            if (val == null) return new SimpleStringProperty("—");
            if (val instanceof java.time.LocalDateTime) {
                return new SimpleStringProperty(
                    ((java.time.LocalDateTime) val).format(
                        java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
            }
            if (val instanceof java.time.LocalDate) {
                return new SimpleStringProperty(
                    ((java.time.LocalDate) val).format(
                        java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")));
            }
            return new SimpleStringProperty(val.toString());
        });

        // Typed combo with StringConverter — shows campaign title, not "uuid - title"
        List<Campaign> campaigns = campaignDAO.getAllCampaigns();
        causeCombo.setItems(FXCollections.observableArrayList(campaigns));
        causeCombo.setConverter(new javafx.util.StringConverter<Campaign>() {
            public String toString(Campaign c) {
                return c == null ? "" : (c.getTitle() != null ? c.getTitle() : "(Untitled)");
            }
            public Campaign fromString(String s) { return null; }
        });
        causeCombo.setPromptText("Select a campaign...");

        loadReports();
    }

    private void loadReports() {
        try {
            java.util.List<java.util.Map<String, Object>> submitted = reportDAO.getSubmittedReports();
            if (submitted != null && !submitted.isEmpty()) {
                reportTable.setItems(FXCollections.observableArrayList(submitted));
            } else {
                // Fall back to showing all campaigns with their donation summaries
                reportTable.setItems(FXCollections.observableArrayList(reportDAO.getAllReports()));
            }
            reportTable.setPlaceholder(new Label("No impact reports available."));
        } catch (Exception e) {
            System.err.println("Report load error: " + e.getMessage());
            try {
                reportTable.setItems(FXCollections.observableArrayList(reportDAO.getAllReports()));
            } catch (Exception ex) {
                System.err.println("Fallback report load error: " + ex.getMessage());
            }
        }
    }

    @FXML
    public void submitReport() {
        if (causeCombo.getValue() == null) {
            alert("Please select a cause/campaign.");
            return;
        }
        if (summaryArea.getText().trim().isEmpty()) {
            alert("Please enter an impact summary.");
            return;
        }
        if (fundsField.getText().trim().isEmpty()) {
            alert("Please enter the funds utilized.");
            return;
        }

        BigDecimal funds;
        try {
            funds = new BigDecimal(fundsField.getText().trim());
            if (funds.compareTo(BigDecimal.ZERO) < 0) {
                alert("Funds utilized cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            alert("Invalid funds amount. Please enter a valid number (e.g. 15000).");
            return;
        }

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("campaignId", causeCombo.getValue().getId());
            data.put("summary",    summaryArea.getText().trim());
            data.put("fundsUsed",  funds);
            data.put("reportedBy", null);

            boolean success = reportDAO.submitReport(data);
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Impact report submitted successfully!", ButtonType.OK).showAndWait();
                summaryArea.clear();
                fundsField.clear();
                causeCombo.setValue(null);
                loadReports();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to save report. Check database connection.", ButtonType.OK).showAndWait();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    private String str(Object val) {
        return val != null ? val.toString() : "—";
    }
}
