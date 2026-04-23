package dms.controllers;

import dao.AnalyticsDAO;
import dao.DonationDAO;
import dao.DonorDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Donation;
import model.Donor;

import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label lblTotalDonors;
    @FXML private Label lblActiveCauses;
    @FXML private Label lblTotalRaised;
    @FXML private Label lblTotalDonations;

    @FXML private TableView<Map<String, Object>>          recentTable;
    @FXML private TableColumn<Map<String, Object>, String> colRDonor;
    @FXML private TableColumn<Map<String, Object>, String> colRAmount;
    @FXML private TableColumn<Map<String, Object>, String> colRCause;
    @FXML private TableColumn<Map<String, Object>, String> colRDate;
    @FXML private TableColumn<Map<String, Object>, String> colRStatus;

    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupRecentTable();
        loadStats();
    }

    private void setupRecentTable() {
        if (recentTable == null) return;
        colRDonor.setCellValueFactory(cd  -> new SimpleStringProperty(str(cd.getValue().get("donor"))));
        colRAmount.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue().get("amount"))));
        colRCause.setCellValueFactory(cd  -> new SimpleStringProperty(str(cd.getValue().get("cause"))));
        colRDate.setCellValueFactory(cd   -> new SimpleStringProperty(str(cd.getValue().get("date"))));
        colRStatus.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue().get("status"))));
    }

    private void loadStats() {
        try {
            Map<String, Object> summary = analyticsDAO.getOverallSummary();
            if (summary != null && !summary.isEmpty()) {
                lblTotalDonors.setText(fmt(summary.get("totalDonors")));
                lblActiveCauses.setText(fmt(summary.get("totalCampaigns")));
                Object raised = summary.get("totalRaised");
                lblTotalRaised.setText(raised != null
                    ? "PKR " + String.format("%,.0f", ((BigDecimal) raised)) : "PKR 0");
                lblTotalDonations.setText(fmt(summary.get("totalDonations")));
            }
        } catch (Exception e) {
            System.err.println("Dashboard stats error: " + e.getMessage());
        }

        try {
            if (recentTable != null) {
                List<Map<String, Object>> recent = analyticsDAO.getRecentDonations(8);
                recentTable.setItems(FXCollections.observableArrayList(recent));
                recentTable.setPlaceholder(new Label("No donations recorded yet."));
            }
        } catch (Exception e) {
            System.err.println("Recent donations error: " + e.getMessage());
        }
    }

    private String fmt(Object val) {
        return val != null ? String.valueOf(val) : "0";
    }

    private String str(Object val) {
        return val != null ? val.toString() : "—";
    }
}
