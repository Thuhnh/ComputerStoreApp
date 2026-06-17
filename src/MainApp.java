import javafx.application.Application;
import javafx.stage.Stage;


public class MainApp extends Application {

    public static int userId = -1;
    public static String userRole = "";
    public static String userName = "";

    @Override
    public void start(Stage stage) {
        LoginWindow loginWindow = new LoginWindow(stage);
        loginWindow.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}