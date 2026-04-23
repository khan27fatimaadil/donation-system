package dms.controllers;

import dms.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * MainController — manages the persistent shell (title bar + sidebar).
 *
 * Fix #15: Active navigation state is now tracked via MainApp.setActiveNav()
 * so that the correct button is always highlighted regardless of how many
 * times a screen is visited or re-loaded.
 */
public class MainController implements Initializable {

    @FXML private HBox       titleBar;
    @FXML private StackPane  contentArea;

    @FXML private Button btnDashboard;
    @FXML private Button btnDonors;
    @FXML private Button btnCauses;
    @FXML private Button btnNewDonation;
    @FXML private Button btnSchedules;
    @FXML private Button btnImpact;
    @FXML private Button btnAnalytics;

    private List<Button> navButtons;
    private double dragOffsetX, dragOffsetY;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        navButtons = Arrays.asList(
                btnDashboard, btnDonors, btnCauses, btnNewDonation,
                btnSchedules, btnImpact, btnAnalytics
        );

        // Give MainApp references it needs for navigate() and setActiveNav()
        MainApp.setContentArea(contentArea);
        MainApp.setNavButtons(navButtons);

        // Make the title bar draggable
        titleBar.setOnMousePressed(e -> {
            dragOffsetX = e.getSceneX();
            dragOffsetY = e.getSceneY();
        });
        titleBar.setOnMouseDragged(e -> {
            Stage stage = getStage();
            if (stage != null) {
                stage.setX(e.getScreenX() - dragOffsetX);
                stage.setY(e.getScreenY() - dragOffsetY);
            }
        });

        navDashboard();
    }

    // ── Window controls ───────────────────────────────────────────────────────
    @FXML public void minimize()  { Stage s = getStage(); if (s != null) s.setIconified(true); }
    @FXML public void toggleMax() { Stage s = getStage(); if (s != null) s.setMaximized(!s.isMaximized()); }
    @FXML public void closeApp()  { javafx.application.Platform.exit(); }

    // ── Navigation ─────────────────────────────────────────────────────────────
    // Fix #15: every nav call delegates to MainApp.navigate() which also calls
    // setActiveNav() so the highlight is always consistent.
    @FXML public void navDashboard()   { MainApp.navigate("dashboard.fxml",      btnDashboard); }
    @FXML public void navDonors()      { MainApp.navigate("donors.fxml",         btnDonors); }
    @FXML public void navCauses()      { MainApp.navigate("causes.fxml",         btnCauses); }
    @FXML public void navNewDonation() { MainApp.navigate("donation_entry.fxml", btnNewDonation); }
    @FXML public void navSchedules()   { MainApp.navigate("schedules.fxml",      btnSchedules); }
    @FXML public void navImpact()      { MainApp.navigate("impact_reports.fxml", btnImpact); }
    @FXML public void navAnalytics()   { MainApp.navigate("analytics.fxml",      btnAnalytics); }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Stage getStage() {
        if (contentArea != null && contentArea.getScene() != null) {
            return (Stage) contentArea.getScene().getWindow();
        }
        return null;
    }
}
