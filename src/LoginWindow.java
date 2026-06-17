import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginWindow {

    private Stage stage;

    public LoginWindow(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f5f5;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(30));
        grid.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label title = new Label("Вход в систему");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        grid.add(title, 0, 0, 2, 1);

        grid.add(new Label("Логин:"), 0, 1);
        TextField txtLogin = new TextField();
        txtLogin.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333333; -fx-background-radius: 8; -fx-padding: 8;");
        grid.add(txtLogin, 1, 1);

        grid.add(new Label("Пароль:"), 0, 2);
        PasswordField txtPass = new PasswordField();
        txtPass.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333333; -fx-background-radius: 8; -fx-padding: 8;");
        grid.add(txtPass, 1, 2);

        Button btnLogin = new Button("Войти");
        btnLogin.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 8;");
        btnLogin.setCursor(javafx.scene.Cursor.HAND);

        Button btnRegister = new Button("Регистрация");
        btnRegister.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 8;");
        btnRegister.setCursor(javafx.scene.Cursor.HAND);

        Label lblMessage = new Label();
        lblMessage.setStyle("-fx-text-fill: #d32f2f;");

        grid.add(btnLogin, 0, 3);
        grid.add(btnRegister, 1, 3);
        grid.add(lblMessage, 0, 4, 2, 1);

        root.getChildren().add(grid);

        btnLogin.setOnAction(e -> {
            String login = txtLogin.getText().trim();
            String pass = txtPass.getText().trim();

            if (login.isEmpty() || pass.isEmpty()) {
                lblMessage.setText("Введите логин и пароль");
                return;
            }

            if (DatabaseHelper.getInstance().login(login, pass) == 1) {
                MainWindow mainWindow = new MainWindow(stage);
                mainWindow.show();
            } else {
                lblMessage.setText("Неверный логин или пароль");
                txtPass.clear();
            }
        });

        btnRegister.setOnAction(e -> showRegisterDialog());

        Scene scene = new Scene(root, 500, 480);
        stage.setScene(scene);
        stage.setTitle("Компьютерный магазин");
        stage.show();
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }


    private void showRegisterDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Регистрация");
        dialog.setHeaderText("Создание нового аккаунта");
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefWidth(550);

        ButtonType okButtonType = new ButtonType("Зарегистрироваться", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 15;");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-background-radius: 8; -fx-padding: 8 15;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #ffffff;");

        TextField txtLogin = new TextField();
        txtLogin.setPromptText("Логин (мин. 5 символов)");
        txtLogin.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333333; -fx-background-radius: 8; -fx-padding: 8;");
        txtLogin.setPrefWidth(300);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Пароль (мин. 8 символов)");
        txtPass.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333333; -fx-background-radius: 8; -fx-padding: 8;");

        PasswordField txtConfirmPass = new PasswordField();
        txtConfirmPass.setPromptText("Подтверждение пароля");
        txtConfirmPass.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333333; -fx-background-radius: 8; -fx-padding: 8;");

        TextField txtName = new TextField();
        txtName.setPromptText("ФИО");
        txtName.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333333; -fx-background-radius: 8; -fx-padding: 8;");

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 12px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(350);

        grid.add(new Label("Логин:"), 0, 0);
        grid.add(txtLogin, 1, 0);
        grid.add(new Label("Пароль:"), 0, 1);
        grid.add(txtPass, 1, 1);
        grid.add(new Label("Подтверждение:"), 0, 2);
        grid.add(txtConfirmPass, 1, 2);
        grid.add(new Label("ФИО:"), 0, 3);
        grid.add(txtName, 1, 3);
        grid.add(lblError, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color: #ffffff;");

        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String login = txtLogin.getText().trim();
            String pass = txtPass.getText().trim();
            String confirmPass = txtConfirmPass.getText().trim();
            String name = txtName.getText().trim();

            if (login.isEmpty() || pass.isEmpty() || name.isEmpty()) {
                lblError.setText("Заполните все поля!");
                event.consume();
                return;
            }

            if (login.length() < 5) {
                lblError.setText("Логин должен быть минимум 5 символов!");
                event.consume();
                return;
            }

            if (pass.length() < 8) {
                lblError.setText("Пароль должен быть минимум 8 символов!");
                event.consume();
                return;
            }

            if (!pass.matches("[a-zA-Z0-9!@#$%^&*]+")) {
                lblError.setText("Пароль только латиница, цифры и символы !@#$%^&*");
                event.consume();
                return;
            }

            if (!pass.equals(confirmPass)) {
                lblError.setText("Пароли не совпадают!");
                event.consume();
                return;
            }

            if (DatabaseHelper.getInstance().register(login, pass, name)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setHeaderText(null);
                alert.setContentText("Регистрация прошла успешно! Теперь войдите.");
                alert.showAndWait();
                dialog.close();
            } else {
                lblError.setText("Ошибка: пользователь с таким логином уже существует!");
                event.consume();
            }
        });

        dialog.showAndWait();
    }
}