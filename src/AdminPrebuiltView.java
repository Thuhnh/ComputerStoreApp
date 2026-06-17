import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class AdminPrebuiltView {

    private VBox view;
    private TableView<Product> prebuiltTable;
    private ObservableList<Product> prebuiltList;

    public AdminPrebuiltView() {
        build();
        loadPrebuilt();
    }

    public VBox getView() {
        return view;
    }

    private void build() {
        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setFillWidth(true);
        VBox.setVgrow(view, Priority.ALWAYS);

        Label title = new Label("УПРАВЛЕНИЕ ГОТОВЫМИ СБОРКАМИ");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        prebuiltTable = new TableView<>();
        prebuiltTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

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

        TableColumn<Product, String> colComponents = new TableColumn<>("Комплектующие");
        colComponents.setCellValueFactory(new PropertyValueFactory<>("specifications"));
        colComponents.setPrefWidth(250);

        prebuiltTable.getColumns().addAll(colId, colName, colPrice, colStock, colComponents);
        VBox.setVgrow(prebuiltTable, Priority.ALWAYS);

        HBox buttonBox = new HBox(10);
        Button btnAdd = new Button("Добавить сборку");
        btnAdd.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
        btnAdd.setOnAction(e -> showAddPrebuiltDialog());

        Button btnEdit = new Button("Редактировать");
        btnEdit.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        btnEdit.setOnAction(e -> showEditPrebuiltDialog());

        Button btnDelete = new Button("Удалить");
        btnDelete.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
        btnDelete.setOnAction(e -> deletePrebuilt());

        buttonBox.getChildren().addAll(btnAdd, btnEdit, btnDelete);

        view.getChildren().addAll(title, prebuiltTable, buttonBox);
    }

    private void loadPrebuilt() {
        prebuiltList = FXCollections.observableArrayList(DatabaseHelper.getInstance().getPrebuiltPCs());
        prebuiltTable.setItems(prebuiltList);
    }

    private void showAddPrebuiltDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Добавление готовой сборки");
        dialog.setHeaderText(null);
        dialog.setResizable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtName = new TextField();
        txtName.setPromptText("Название сборки");
        TextField txtPrice = new TextField();
        txtPrice.setPromptText("Цена");
        TextField txtStock = new TextField();
        txtStock.setPromptText("Количество");

        Label componentsLabel = new Label("Комплектующие:");
        componentsLabel.setStyle("-fx-font-weight: bold;");

        VBox componentsBox = new VBox(5);
        List<ComboBox<String>> componentSelectors = new ArrayList<>();

        String[] componentTypes = {"Процессор", "Материнская плата", "Видеокарта", "Оперативная память", "Накопитель", "Блок питания", "Охлаждение"};
        String[] typeFilters = {"Processor", "Motherboard", "VideoCard", "RAM", "SSD", "PSU", "Cooling"};

        for (int i = 0; i < componentTypes.length; i++) {
            HBox row = new HBox(10);
            Label typeLabel = new Label(componentTypes[i] + ":");
            typeLabel.setPrefWidth(120);

            ComboBox<String> combo = new ComboBox<>();
            combo.setPromptText("Выберите " + componentTypes[i].toLowerCase());
            combo.setPrefWidth(300);

            var products = DatabaseHelper.getInstance().getProductsByType(typeFilters[i]);
            for (Product p : products) {
                combo.getItems().add(p.getName() + " (" + p.getPrice() + " руб.)");   }

            componentSelectors.add(combo);
            row.getChildren().addAll(typeLabel, combo);
            componentsBox.getChildren().add(row);
        }

        ScrollPane componentsScroll = new ScrollPane(componentsBox);
        componentsScroll.setFitToWidth(true);
        componentsScroll.setPrefHeight(250);

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
        grid.add(componentsLabel, 0, 3);
        grid.add(componentsScroll, 1, 3);
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

                    if (name.isEmpty()) {
                        showAlert("Введите название!");
                        return null;
                    }

                    StringBuilder components = new StringBuilder();
                    for (int i = 0; i < componentSelectors.size(); i++) {
                        String selected = componentSelectors.get(i).getValue();
                        if (selected != null && !selected.isEmpty()) {
                            if (components.length() > 0) components.append("\n");
                            String cleanName = selected.replaceFirst("^\\d+ - ", "");
                            components.append(componentTypes[i]).append(": ").append(cleanName);
                        }
                    }

                    String imageName = "";
                    if (selectedImagePath[0] != null) {
                        imageName = selectedImageName[0];
                        try {
                            File destDir = new File("resources/images/products/");
                            destDir.mkdirs();
                            File destFile = new File(destDir, imageName);
                            Files.copy(new File(selectedImagePath[0]).toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception ex) {
                            System.out.println("Ошибка копирования: " + ex.getMessage());
                        }
                    }

                    DatabaseHelper.getInstance().addPrebuiltPC(name, price, stock, components.toString(), imageName);
                    loadPrebuilt();
                    showAlert("Сборка добавлена!");
                } catch (NumberFormatException ex) {
                    showAlert("Цена и количество должны быть числами!");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showEditPrebuiltDialog() {
        Product selected = prebuiltTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите сборку!");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Редактирование сборки");
        dialog.setHeaderText(null);
        dialog.setResizable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtName = new TextField(selected.getName());
        TextField txtPrice = new TextField(String.valueOf(selected.getPrice()));
        TextField txtStock = new TextField(String.valueOf(selected.getStock()));
        TextArea txtComponents = new TextArea(selected.getSpecifications());
        txtComponents.setPrefRowCount(5);
        txtComponents.setPrefWidth(400);

        grid.add(new Label("Название:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Цена:"), 0, 1);
        grid.add(txtPrice, 1, 1);
        grid.add(new Label("Количество:"), 0, 2);
        grid.add(txtStock, 1, 2);
        grid.add(new Label("Комплектующие:"), 0, 3);
        grid.add(txtComponents, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String name = txtName.getText();
                    int price = Integer.parseInt(txtPrice.getText());
                    int stock = Integer.parseInt(txtStock.getText());
                    String components = txtComponents.getText();

                    DatabaseHelper.getInstance().updatePrebuiltPC(selected.getId(), name, price, stock, components);
                    loadPrebuilt();
                    showAlert("Сборка обновлена!");
                } catch (NumberFormatException ex) {
                    showAlert("Цена и количество должны быть числами!");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void deletePrebuilt() {
        Product selected = prebuiltTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите сборку!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить сборку \"" + selected.getName() + "\"?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                DatabaseHelper.getInstance().deleteProduct(selected.getId());
                loadPrebuilt();
                showAlert("Сборка удалена!");
            }
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}