import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class OrderHistoryView {

    private VBox view;

    public OrderHistoryView() {
        build();
    }

    public VBox getView() {
        return view;
    }

    private void build() {
        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setFillWidth(true);

        Label title = new Label("ИСТОРИЯ ЗАКАЗОВ");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TableView<OrderInfo> ordersTable = new TableView<>();
        ordersTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<OrderInfo, Integer> colId = new TableColumn<>("№ заказа");
        colId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colId.setPrefWidth(80);

        TableColumn<OrderInfo, String> colDate = new TableColumn<>("Дата");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setPrefWidth(150);

        TableColumn<OrderInfo, Integer> colTotal = new TableColumn<>("Сумма");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setPrefWidth(100);

        TableColumn<OrderInfo, String> colStatus = new TableColumn<>("Статус");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(120);

        TableColumn<OrderInfo, Button> colAction = new TableColumn<>("Действие");
        colAction.setCellValueFactory(new PropertyValueFactory<>("reviewBtn"));
        colAction.setPrefWidth(150);

        ordersTable.getColumns().addAll(colId, colDate, colTotal, colStatus, colAction);
        VBox.setVgrow(ordersTable, Priority.ALWAYS);

        ObservableList<OrderInfo> orders = FXCollections.observableArrayList(DatabaseHelper.getInstance().getUserOrders());
        ordersTable.setItems(orders);

        view.getChildren().addAll(title, ordersTable);
    }

    public static class OrderInfo {
        private int orderId;
        private String date;
        private int total;
        private String status;
        private Button reviewBtn;

        public OrderInfo(int orderId, String date, int total, String status, boolean canReview) {
            this.orderId = orderId;
            this.date = date;
            this.total = total;
            this.status = status;
            this.reviewBtn = new Button("Оставить отзыв");
            this.reviewBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            this.reviewBtn.setDisable(!canReview);
            this.reviewBtn.setOnAction(e -> showReviewDialog());
        }

        public int getOrderId() { return orderId; }
        public String getDate() { return date; }
        public int getTotal() { return total; }
        public String getStatus() { return status; }
        public Button getReviewBtn() { return reviewBtn; }

        private void showReviewDialog() {
            var products = DatabaseHelper.getInstance().getOrderItems(orderId);

            if (products.isEmpty()) {
                showAlert("В этом заказе нет товаров для отзыва");
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Оставить отзыв");
            dialog.setHeaderText("Выберите товар для отзыва");
            dialog.setResizable(true);

            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(10));
            vbox.setStyle("-fx-background-color: #1e1e1e;");

            Label productLabel = new Label("Товар:");
            productLabel.setStyle("-fx-text-fill: white;");
            ComboBox<Product> productCombo = new ComboBox<>();
            productCombo.getItems().addAll(products);
            productCombo.setPromptText("Выберите товар");
            productCombo.setCellFactory(lv -> new ListCell<Product>() {
                @Override
                protected void updateItem(Product item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName() + " - " + item.getPrice() + " руб.");
                }
            });
            productCombo.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white;");

            Label ratingLabel = new Label("Оценка:");
            ratingLabel.setStyle("-fx-text-fill: white;");
            ComboBox<Integer> ratingCombo = new ComboBox<>();
            ratingCombo.getItems().addAll(1, 2, 3, 4, 5);
            ratingCombo.setValue(5);
            ratingCombo.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white;");

            Label commentLabel = new Label("Комментарий:");
            commentLabel.setStyle("-fx-text-fill: white;");
            TextArea commentArea = new TextArea();
            commentArea.setPromptText("Ваш отзыв о товаре...");
            commentArea.setPrefRowCount(3);
            commentArea.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-prompt-text-fill: #888;");

            Label imageLabel = new Label("Фото не выбрано");
            imageLabel.setStyle("-fx-text-fill: #ccc; -fx-font-size: 11px;");
            Button btnChooseImage = new Button("Добавить фото (необязательно)");
            btnChooseImage.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-cursor: hand;");
            final String[] selectedImageName = {null};
            final String[] selectedImagePath = {null};

            btnChooseImage.setOnAction(ev -> {
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

            vbox.getChildren().addAll(
                    productLabel, productCombo,
                    ratingLabel, ratingCombo,
                    commentLabel, commentArea,
                    imageLabel, btnChooseImage
            );

            dialog.getDialogPane().setContent(vbox);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setStyle("-fx-background-color: #1e1e1e;");

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setText("Отправить отзыв");
            okButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-cursor: hand;");

            Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
            cancelButton.setText("Отмена");
            cancelButton.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-cursor: hand;");

            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    Product selectedProduct = productCombo.getValue();
                    if (selectedProduct == null) {
                        showError("Выберите товар!");
                        return null;
                    }
                    int rating = ratingCombo.getValue();
                    String comment = commentArea.getText();
                    if (comment.isEmpty()) {
                        showError("Введите комментарий!");
                        return null;
                    }

                    String imageName = selectedImageName[0] != null ? selectedImageName[0] : "";
                    if (selectedImagePath[0] != null) {
                        try {
                            File destDir = new File("resources/images/reviews/");
                            destDir.mkdirs();
                            File destFile = new File(destDir, imageName);
                            Files.copy(new File(selectedImagePath[0]).toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception ex) {
                            System.out.println("Ошибка копирования фото: " + ex.getMessage());
                        }
                    }

                    if (DatabaseHelper.getInstance().addReview(MainApp.userId, selectedProduct.getId(), rating, comment, imageName)) {
                        showAlert("Отзыв добавлен! Спасибо!");
                        reviewBtn.setDisable(true);
                        reviewBtn.setText("Отзыв оставлен");
                        reviewBtn.setStyle("-fx-background-color: #555; -fx-text-fill: #ccc;");
                    } else {
                        showError("Вы уже оставляли отзыв на этот товар");
                    }
                }
                return null;
            });

            dialog.showAndWait();
        }

        private void showAlert(String msg) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(msg);
            alert.showAndWait();
        }

        private void showError(String msg) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText(msg);
            alert.showAndWait();
        }
    }
}