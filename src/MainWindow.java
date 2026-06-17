import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainWindow {

    private Stage stage;
    private VBox contentArea;
    private Label cartCounterLabel;
    private static Stage configuratorStage;
    private static ConfiguratorView configuratorView;

    public MainWindow(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        BorderPane root = new BorderPane();

        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #2d2d2d;");

        Label logo = new Label("Computer Store");
        logo.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");

        HBox navButtons = new HBox(10);

        if (MainApp.userRole.equals("Admin")) {
            Button btnAdminProducts = new Button("Управление товарами");
            Button btnAdminPrebuilt = new Button("Готовые сборки");
            Button btnAdminOrders = new Button("Заказы");
            btnAdminProducts.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
            btnAdminPrebuilt.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
            btnAdminOrders.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
            navButtons.getChildren().addAll(btnAdminProducts, btnAdminPrebuilt, btnAdminOrders);

            btnAdminProducts.setOnAction(e -> showAdminProducts());
            btnAdminPrebuilt.setOnAction(e -> showAdminPrebuilt());
            btnAdminOrders.setOnAction(e -> showAdminOrders());
        } else {
            Button btnCatalog = new Button("Каталог");
            Button btnConfig = new Button("Конфигуратор");
            Button btnPrebuilt = new Button("Готовые ПК");
            Button btnCart = new Button("Корзина");
            Button btnHistory = new Button("История заказов");

            cartCounterLabel = new Label("0");
            cartCounterLabel.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 0 5 0 5;");
            HBox cartButtonBox = new HBox(5);
            cartButtonBox.setAlignment(Pos.CENTER);
            cartButtonBox.getChildren().addAll(btnCart, cartCounterLabel);

            btnCatalog.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
            btnConfig.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
            btnPrebuilt.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
            btnHistory.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
            btnCart.setStyle("-fx-background-color: #0bda51; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");

            navButtons.getChildren().addAll(btnCatalog, btnConfig, btnPrebuilt, cartButtonBox, btnHistory);

            btnCatalog.setOnAction(e -> showCatalog());
            btnConfig.setOnAction(e -> showConfigurator());
            btnPrebuilt.setOnAction(e -> showPrebuiltPCs());
            btnCart.setOnAction(e -> showCart());
            btnHistory.setOnAction(e -> showOrderHistory());
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label(MainApp.userName + " | " + MainApp.userRole);
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        Button btnLogout = new Button("Выход");
        btnLogout.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 12;");
        btnLogout.setOnAction(e -> logout());

        HBox rightBox = new HBox(15);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.getChildren().addAll(userLabel, btnLogout);

        topBar.getChildren().addAll(logo, navButtons, spacer, rightBox);

        contentArea = new VBox();
        contentArea.setPadding(new Insets(10));
        contentArea.setFillWidth(true);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        root.setTop(topBar);
        root.setCenter(contentArea);

        if (MainApp.userRole.equals("Admin")) {
            showAdminProducts();
        } else {
            showCatalog();
        }

        Scene scene = new Scene(root, 1200, 700);
        // ========== ПОДКЛЮЧАЕМ CSS ==========
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Computer Store - " + MainApp.userName);
        stage.setMaximized(true);
        stage.show();

        updateCartCounter();
    }

    private void updateCartCounter() {
        if (cartCounterLabel != null && !MainApp.userRole.equals("Admin")) {
            int count = DatabaseHelper.getInstance().getCart().size();
            cartCounterLabel.setText(String.valueOf(count));
        }
    }

    private void showCatalog() {
        contentArea.getChildren().clear();
        CatalogView catalogView = new CatalogView(this::updateCartCounter);
        contentArea.getChildren().add(catalogView.getView());
    }

    private void showConfigurator() {
        contentArea.getChildren().clear();
        ConfiguratorView configuratorView = new ConfiguratorView();
        contentArea.getChildren().add(configuratorView.getView());
    }

    private void showPrebuiltPCs() {
        contentArea.getChildren().clear();
        PrebuiltPCView prebuiltView = new PrebuiltPCView(this::updateCartCounter);
        contentArea.getChildren().add(prebuiltView.getView());
    }

    private void showCart() {
        contentArea.getChildren().clear();
        CartView cartView = new CartView(this::updateCartCounter);
        contentArea.getChildren().add(cartView.getView());
    }

    private void showOrderHistory() {
        contentArea.getChildren().clear();
        OrderHistoryView historyView = new OrderHistoryView();
        contentArea.getChildren().add(historyView.getView());
    }

    private void showAdminProducts() {
        contentArea.getChildren().clear();
        AdminPanelView adminPanel = new AdminPanelView();
        contentArea.getChildren().add(adminPanel.getView());
    }

    private void showAdminPrebuilt() {
        contentArea.getChildren().clear();
        AdminPrebuiltView adminPrebuilt = new AdminPrebuiltView();
        contentArea.getChildren().add(adminPrebuilt.getView());
    }

    private void showAdminOrders() {
        contentArea.getChildren().clear();
        AdminOrdersView adminOrders = new AdminOrdersView();
        contentArea.getChildren().add(adminOrders.getView());
    }

    public static void openConfiguratorWithProduct(Product product) {
        if (configuratorStage == null || !configuratorStage.isShowing()) {
            configuratorStage = new Stage();
            configuratorStage.setTitle("Конфигуратор ПК");
            configuratorStage.setWidth(800);
            configuratorStage.setHeight(600);
            configuratorView = new ConfiguratorView();
            Scene scene = new Scene(configuratorView.getView());
            configuratorStage.setScene(scene);
            configuratorStage.show();
        }
    }

    private void logout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы уверены, что хотите выйти?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                MainApp.userId = -1;
                MainApp.userRole = "";
                MainApp.userName = "";
                LoginWindow loginWindow = new LoginWindow(stage);
                loginWindow.show();
            }
        });
    }
}