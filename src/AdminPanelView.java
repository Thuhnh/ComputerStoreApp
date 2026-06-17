import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AdminPanelView {

    private VBox view;
    private TableView<Product> productTable;
    private ObservableList<Product> productList;

    public AdminPanelView() {
        build();
        loadProducts();
    }

    public VBox getView() {
        return view;
    }

    private void build() {
        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setFillWidth(true);
        VBox.setVgrow(view, Priority.ALWAYS);

        Label title = new Label("УПРАВЛЕНИЕ ТОВАРАМИ");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        productTable = new TableView<>();
        productTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<Product, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<Product, String> colName = new TableColumn<>("Название");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(200);

        TableColumn<Product, Integer> colPrice = new TableColumn<>("Цена");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setPrefWidth(80);

        TableColumn<Product, Integer> colStock = new TableColumn<>("В наличии");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock.setPrefWidth(80);

        TableColumn<Product, String> colCategory = new TableColumn<>("Категория");
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCategory.setPrefWidth(120);

        TableColumn<Product, String> colImage = new TableColumn<>("Картинка");
        colImage.setCellValueFactory(new PropertyValueFactory<>("image"));
        colImage.setPrefWidth(100);

        productTable.getColumns().addAll(colId, colName, colPrice, colStock, colCategory, colImage);
        VBox.setVgrow(productTable, Priority.ALWAYS);

        HBox buttonBox = new HBox(10);

        Button btnAdd = new Button("Добавить товар");
        btnAdd.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
        btnAdd.setOnAction(e -> showAddProductDialog());

        Button btnEdit = new Button("Редактировать");
        btnEdit.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        btnEdit.setOnAction(e -> showEditProductDialog());

        Button btnDelete = new Button("Удалить");
        btnDelete.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
        btnDelete.setOnAction(e -> deleteProduct());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonBox.getChildren().addAll(btnAdd, btnEdit, btnDelete, spacer);
        view.getChildren().addAll(title, productTable, buttonBox);
    }

    private void loadProducts() {
        productList = FXCollections.observableArrayList(DatabaseHelper.getInstance().getAllProductsForSearch());
        productTable.setItems(productList);
        System.out.println("Загружено товаров: " + productList.size());
    }

    private void showAddProductDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Добавление товара");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtName = new TextField();
        txtName.setPromptText("Название");
        TextField txtPrice = new TextField();
        txtPrice.setPromptText("Цена");
        TextField txtStock = new TextField();
        txtStock.setPromptText("Количество");
        ComboBox<String> cmbCategory = new ComboBox<>();
        cmbCategory.getItems().addAll("Процессоры", "Видеокарты", "Материнские платы", "Оперативная память", "Накопители", "Блоки питания", "Охлаждение", "Мониторы", "Мыши", "Клавиатуры", "Наушники", "Корпуса");
        cmbCategory.setPromptText("Категория");

        Label imageLabel = new Label("Картинка не выбрана");
        Button btnChooseImage = new Button("Выбрать картинку");
        final String[] selectedImageName = {null};
        final String[] selectedImagePath = {null};

        btnChooseImage.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp")
            );
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                selectedImageName[0] = file.getName();
                selectedImagePath[0] = file.getAbsolutePath();
                imageLabel.setText(selectedImageName[0]);
            }
        });

        grid.add(new Label("Название:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Цена:"), 0, 1);
        grid.add(txtPrice, 1, 1);
        grid.add(new Label("Количество:"), 0, 2);
        grid.add(txtStock, 1, 2);
        grid.add(new Label("Категория:"), 0, 3);
        grid.add(cmbCategory, 1, 3);
        grid.add(new Label("Картинка:"), 0, 4);
        grid.add(btnChooseImage, 1, 4);
        grid.add(imageLabel, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String name = txtName.getText();
                    int price = Integer.parseInt(txtPrice.getText());
                    int stock = Integer.parseInt(txtStock.getText());
                    String category = cmbCategory.getValue();

                    if (name.isEmpty() || category == null) {
                        showAlert("Заполните все поля!");
                        return null;
                    }

                    int catId = getCategoryId(category);
                    String type = getTypeByCategory(category);
                    String imageName = selectedImageName[0] != null ? selectedImageName[0] : "";

                    if (selectedImagePath[0] != null) {
                        try {
                            File destDir = new File("resources/images/products/");
                            destDir.mkdirs();
                            File destFile = new File(destDir, imageName);
                            Files.copy(new File(selectedImagePath[0]).toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception ex) {
                            System.out.println("Ошибка копирования: " + ex.getMessage());
                        }
                    }

                    DatabaseHelper.getInstance().addProduct(name, price, stock, catId, type, imageName);
                    loadProducts();
                    showAlert("Товар добавлен!");
                } catch (NumberFormatException ex) {
                    showAlert("Цена и количество должны быть числами!");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
    private void showEditProductDialog() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите товар!");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Редактирование товара");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtName = new TextField(selected.getName());
        TextField txtPrice = new TextField(String.valueOf(selected.getPrice()));
        TextField txtStock = new TextField(String.valueOf(selected.getStock()));
        ComboBox<String> cmbCategory = new ComboBox<>();
        cmbCategory.getItems().addAll("Процессоры", "Видеокарты", "Материнские платы", "Оперативная память", "Накопители", "Блоки питания", "Охлаждение", "Мониторы", "Мыши", "Клавиатуры", "Наушники", "Корпуса");
        cmbCategory.setValue(selected.getCategory());

        grid.add(new Label("Название:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Цена:"), 0, 1);
        grid.add(txtPrice, 1, 1);
        grid.add(new Label("Количество:"), 0, 2);
        grid.add(txtStock, 1, 2);
        grid.add(new Label("Категория:"), 0, 3);
        grid.add(cmbCategory, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String name = txtName.getText();
                    int price = Integer.parseInt(txtPrice.getText());
                    int stock = Integer.parseInt(txtStock.getText());
                    String category = cmbCategory.getValue();

                    int catId = getCategoryId(category);
                    DatabaseHelper.getInstance().updateProduct(selected.getId(), name, price, stock, catId);
                    loadProducts();
                    showAlert("Товар обновлён!");
                } catch (NumberFormatException ex) {
                    showAlert("Цена и количество должны быть числами!");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void deleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите товар!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить товар \"" + selected.getName() + "\"?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                DatabaseHelper.getInstance().deleteProduct(selected.getId());
                loadProducts();
                showAlert("Товар удалён!");
            }
        });
    }

    private int getCategoryId(String name) {
        switch (name) {
            case "Процессоры": return 1;
            case "Видеокарты": return 2;
            case "Материнские платы": return 3;
            case "Оперативная память": return 4;
            case "Накопители": return 5;
            case "Блоки питания": return 6;
            case "Охлаждение": return 7;
            case "Мониторы": return 8;
            case "Мыши": return 9;
            case "Клавиатуры": return 10;
            case "Наушники": return 11;
            case "Корпуса": return 12;
            default: return 1;
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static class OrderInfo {
        private int orderId;
        private String userName;
        private String date;
        private int total;
        private String status;

        public OrderInfo(int orderId, String userName, String date, int total, String status) {
            this.orderId = orderId;
            this.userName = userName;
            this.date = date;
            this.total = total;
            this.status = status;
        }

        public int getOrderId() { return orderId; }
        public String getUserName() { return userName; }
        public String getDate() { return date; }
        public int getTotal() { return total; }
        public String getStatus() { return status; }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    private String getTypeByCategory(String category) {
        switch (category) {
            case "Процессоры": return "Processor";
            case "Видеокарты": return "VideoCard";
            case "Материнские платы": return "Motherboard";
            case "Оперативная память": return "RAM";
            case "Накопители": return "SSD";
            case "Блоки питания": return "PSU";
            case "Охлаждение": return "Cooling";
            case "Мониторы": return "Monitor";
            case "Мыши": return "Mouse";
            case "Клавиатуры": return "Keyboard";
            case "Наушники": return "Headset";
            case "Корпуса": return "Case";
            default: return "";
        }
    }

}