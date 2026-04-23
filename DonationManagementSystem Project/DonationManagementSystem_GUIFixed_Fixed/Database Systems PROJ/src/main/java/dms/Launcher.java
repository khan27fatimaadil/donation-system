package dms;

/**
 * Launcher class — avoids the "JavaFX runtime components are missing" error
 * that Eclipse throws when running a class that directly extends Application.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
