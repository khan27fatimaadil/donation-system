package dms.controllers;

import dao.AnalyticsDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class AnalyticsController implements Initializable {

    @FXML private BarChart<String, Number> monthlyBarChart;
    @FXML private PieChart causePieChart;
    @FXML private VBox printableArea;

    @FXML private TableView<Map<String, Object>> donorTable, causeTable, recurTable;
    @FXML private TableColumn<Map<String, Object>, String> colDName, colDTotal, colDCauses;
    @FXML private TableColumn<Map<String, Object>, String> colCTitle, colCGoal, colCRaised, colCPct;
    @FXML private TableColumn<Map<String, Object>, Double> colCProgress;
    @FXML private TableColumn<Map<String, Object>, String> colRDonor, colRCause, colRFreq, colRDue;
    @FXML private Label lblDonorCount;

    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();
    private static final String[] MONTH_NAMES = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Disable animations up front before any data is loaded
        monthlyBarChart.setAnimated(false);
        causePieChart.setAnimated(false);

        setupTables();

        // Load data on a background thread to avoid blocking the FX thread
        loadAnalyticsAsync();
    }

    private void setupTables() {
        // REMOVED: Dummy series data that was causing thin bars and conflicts
        
        // Top Donors
        colDName.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue().get("name"))));
        colDTotal.setCellValueFactory(cd -> new SimpleStringProperty(formatMoney(cd.getValue().get("total"))));
        colDCauses.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue().get("donationCount"))));

        // Cause Progress
        colCTitle.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue().get("title"))));
        colCGoal.setCellValueFactory(cd -> new SimpleStringProperty(formatMoney(cd.getValue().get("goal"))));
        colCRaised.setCellValueFactory(cd -> new SimpleStringProperty(formatMoney(cd.getValue().get("raised"))));
        colCPct.setCellValueFactory(cd -> {
            Object pct = cd.getValue().get("percent");
            return new SimpleStringProperty(pct != null ? pct + "%" : "0%");
        });

        colCProgress.setCellValueFactory(cd -> {
            Object pct = cd.getValue().get("percent");
            double val = 0.0;
            if (pct != null) {
                try { val = Double.parseDouble(pct.toString()) / 100.0; } catch (Exception ignored) {}
            }
            return new SimpleDoubleProperty(Math.min(val, 1.0)).asObject();
        });

        colCProgress.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setGraphic(null); return; }
                StackPane pane = new StackPane();
                Rectangle bg = new Rectangle(140, 14);
                bg.setArcWidth(7); bg.setArcHeight(7);
                bg.setFill(Color.web("#e2e8f0"));
                Rectangle fill = new Rectangle(140 * value, 14);
                fill.setArcWidth(7); fill.setArcHeight(7);
                fill.setFill(value >= 1.0 ? Color.web("#3fb950") : Color.web("#2563eb"));
                pane.getChildren().addAll(bg, fill);
                pane.setAlignment(Pos.CENTER_LEFT);
                setGraphic(pane);
            }
        });

        // Recurring
        colRDonor.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue().get("donor"))));
        colRCause.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue().get("cause"))));
        colRFreq.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue().get("frequency"))));
        colRDue.setCellValueFactory(cd -> {
            Object due = cd.getValue().get("nextDue");
            return new SimpleStringProperty(due != null ? due.toString() : "—");
        });
    }

    @FXML
    public void refreshData() {
        loadAnalyticsAsync();
    }

    private void loadAnalyticsAsync() {
        // Clear charts immediately on FX thread
        monthlyBarChart.getData().clear();
        causePieChart.getData().clear();

        Thread bgThread = new Thread(() -> {
            try {
                // Fetch data on background thread
                int year = LocalDate.now().getYear();
                List<Map<String, Object>> monthly   = analyticsDAO.getDonationsByMonth(year);
                List<Map<String, Object>> causes    = analyticsDAO.getCauseProgress();
                List<Map<String, Object>> donors    = analyticsDAO.getTopDonors();
                List<Map<String, Object>> recurring = analyticsDAO.getUpcomingRecurring();

                // UPDATED: Build bar chart series by only adding months that HAVE data
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Monthly Contributions");
                Map<Integer, BigDecimal> monthMap = new HashMap<>();
                for (Map<String, Object> row : monthly) {
                    monthMap.put((Integer) row.get("month"), (BigDecimal) row.get("total"));
                }
                
                for (int m = 1; m <= 12; m++) {
                    BigDecimal total = monthMap.getOrDefault(m, BigDecimal.ZERO);
                    // Only add the month to the chart if it has a donation total > 0
                    if (total.compareTo(BigDecimal.ZERO) > 0) {
                        series.getData().add(new XYChart.Data<>(MONTH_NAMES[m - 1], total));
                    }
                }

                // Build pie chart data off-thread
                List<PieChart.Data> pieData = new ArrayList<>();
                for (Map<String, Object> row : causes) {
                    BigDecimal raised = (BigDecimal) row.get("raised");
                    if (raised != null && raised.doubleValue() > 0) {
                        pieData.add(new PieChart.Data(str(row.get("title")), raised.doubleValue()));
                    }
                }
                if (pieData.isEmpty()) {
                    pieData.add(new PieChart.Data("No Data", 1));
                }

                final List<Map<String, Object>> finalCauses    = causes;
                final List<Map<String, Object>> finalDonors    = donors;
                final List<Map<String, Object>> finalRecurring = recurring;

                // Push all UI updates to the FX thread
                Platform.runLater(() -> {
                    // Bar Chart
                    monthlyBarChart.getData().add(series);
                    Platform.runLater(() -> {
                        for (XYChart.Data<String, Number> d : series.getData()) {
                            if (d.getNode() != null) {
                                d.getNode().setStyle("-fx-bar-fill: #2563eb;");
                            }
                        }
                    });

                    // Pie Chart
                    causePieChart.setData(FXCollections.observableArrayList(pieData));
                    causePieChart.setLegendSide(Side.RIGHT);
                    causePieChart.setLabelsVisible(true);

                    // Tables
                    donorTable.setItems(FXCollections.observableArrayList(finalDonors));
                    if (lblDonorCount != null) {
                        lblDonorCount.setText("Showing top " + finalDonors.size() + " donors");
                    }
                    causeTable.setItems(FXCollections.observableArrayList(finalCauses));
                    recurTable.setItems(FXCollections.observableArrayList(finalRecurring));
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        bgThread.setDaemon(true);
        bgThread.setName("analytics-loader");
        bgThread.start();
    }
    
    @FXML
    public void handlePrint() {
        javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
        
        if (job != null && job.showPrintDialog(printableArea.getScene().getWindow())) {
            javafx.print.PageLayout pageLayout = job.getJobSettings().getPageLayout();
            
            double printableWidth = pageLayout.getPrintableWidth();
            double nodeWidth = printableArea.getBoundsInParent().getWidth();
            double scale = printableWidth / nodeWidth;
            
            javafx.scene.transform.Scale scaleTransform = new javafx.scene.transform.Scale(scale, scale);
            printableArea.getTransforms().add(scaleTransform);

            boolean success = job.printPage(printableArea);
            
            if (success) {
                job.endJob();
            }

            printableArea.getTransforms().remove(scaleTransform);
        }
    }

    private String formatMoney(Object val) {
        if (val == null) return "0";
        try { return String.format("%,.0f", new BigDecimal(val.toString())); }
        catch (Exception e) { return val.toString(); }
    }

    private String str(Object val) { return val != null ? val.toString() : "—"; }
}