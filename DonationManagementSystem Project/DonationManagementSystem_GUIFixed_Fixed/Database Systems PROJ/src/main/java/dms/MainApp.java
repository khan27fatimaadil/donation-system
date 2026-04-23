package dms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;

/**
 * MainApp — application entry point and central navigation hub.
 *
 * Fix #15: navigate() now also accepts the active Button so that the sidebar
 * highlight is always updated atomically with the content swap — eliminating
 * the previous race condition where state could be lost on back-navigation.
 */
public class MainApp extends Application {

    private static Stage        primaryStage;
    private static StackPane    contentArea;   // set by MainController.initialize()
    private static List<Button> navButtons;    // Fix #15: reference to all nav buttons

    public static void setContentArea(StackPane area) { contentArea = area; }

    /** Fix #15: give MainApp the list of sidebar buttons for highlight management. */
    public static void setNavButtons(List<Button> buttons) { navButtons = buttons; }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.initStyle(StageStyle.UNDECORATED);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_layout.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 760);
        String css  = getClass().getResource("/css/app.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    /**
     * Navigate to an FXML screen AND update the active sidebar button.
     * Always call this overload from controllers.
     *
     * Fix #15: active state is set here, inside the same method that swaps
     * content, so the two can never get out of sync.
     */
    public static void navigate(String fxmlFile, Button activeButton) {
        if (contentArea == null) return;
        try {
            // FIX: Use an instance of FXMLLoader and provide an empty ResourceBundle 
            // to prevent the "No resources specified" crash.
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/" + fxmlFile));
            
            // This line prevents the crash if line 81 of analytics.fxml uses a '%' key
            loader.setResources(java.util.ResourceBundle.getBundle("dms.dummy", 
                new java.util.ResourceBundle.Control() {
                    @Override
                    public java.util.ResourceBundle newBundle(String baseName, java.util.Locale locale, 
                        String format, ClassLoader loader, boolean reload) {
                        return new java.util.ListResourceBundle() {
                            @Override
                            protected Object[][] getContents() { return new Object[0][0]; }
                        };
                    }
                }));

            Parent content = loader.load();
            contentArea.getChildren().setAll(content);

            // Update sidebar highlight
            if (navButtons != null && activeButton != null) {
                for (Button b : navButtons) {
                    b.getStyleClass().remove("nav-btn-active");
                    if (!b.getStyleClass().contains("nav-btn")) {
                        b.getStyleClass().add("nav-btn");
                    }
                }
                if (!activeButton.getStyleClass().contains("nav-btn-active")) {
                    activeButton.getStyleClass().add("nav-btn-active");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Error alert logic remains the same...
        }
    }

    /** Legacy overload — retained so any existing call without a button still compiles. */
    public static void navigate(String fxmlFile) {
        navigate(fxmlFile, null);
    }

    public static Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) { launch(args); }
}
