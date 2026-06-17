import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class CartView {

    private Runnable onCartUpdate;
    private VBox view;
    private VBox cartItemsContainer;
    private Label totalLabel;
    private Label itemsCountLabel;
    private List<CartItem> cartItems;

    public CartView(Runnable onCartUpdate) {
        this.onCartUpdate = onCartUpdate;
        build();
    }

    public VBox getView() {
        return view;
    }

    private void build() {
        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setStyle("-fx-background-color: #f5f5f5;");

        Label title = new Label("КОРЗИНА");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        itemsCountLabel = new Label("0 товаров");
        itemsCountLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(title, itemsCountLabel);

        cartItemsContainer = new VBox(10);
        cartItemsContainer.setPadding(new Insets(10));
        cartItemsContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        ScrollPane scrollPane = new ScrollPane(cartItemsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox bottomPanel = new VBox(10);
        bottomPanel.setPadding(new Insets(15));
        bottomPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        totalLabel = new Label("Итого: 0 руб.");
        totalLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        Button btnOrder = new Button("Оформить заказ");
        btnOrder.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 8;");
        btnOrder.setMaxWidth(Double.MAX_VALUE);
        btnOrder.setCursor(javafx.scene.Cursor.HAND);
        btnOrder.setOnAction(e -> checkout());

        bottomPanel.getChildren().addAll(totalLabel, btnOrder);

        view.getChildren().addAll(headerBox, scrollPane, bottomPanel);

        refreshCart();
    }

    private void refreshCart() {
        cartItems = DatabaseHelper.getInstance().getCart();
        cartItemsContainer.getChildren().clear();

        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotal();
            addCartItemRow(item);
        }

        itemsCountLabel.setText(getItemCountText(cartItems.size()));
        totalLabel.setText(String.format("Итого: %,d руб.", total));

        if (cartItems.isEmpty()) {
            Label emptyLabel = new Label("Корзина пуста");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 16px;");
            emptyLabel.setAlignment(Pos.CENTER);
            cartItemsContainer.getChildren().add(emptyLabel);
        }
    }

    private String getItemCountText(int count) {
        if (count % 10 == 1 && count % 100 != 11) return count + " товар";
        if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) return count + " товара";
        return count + " товаров";
    }

    private void addCartItemRow(CartItem item) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 10; -fx-border-color: #eee; -fx-border-width: 0 0 1 0;");

        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        nameLabel.setPrefWidth(250);

        Label priceLabel = new Label(String.format("%,d руб.", item.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #666;");
        priceLabel.setPrefWidth(80);

        HBox quantityBox = new HBox(5);
        quantityBox.setAlignment(Pos.CENTER);

        Button minusBtn = new Button("-");
        minusBtn.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-font-weight: bold; -fx-min-width: 30; -fx-background-radius: 5;");
        minusBtn.setCursor(javafx.scene.Cursor.HAND);

        Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
        qtyLabel.setStyle("-fx-min-width: 30; -fx-alignment: center; -fx-text-fill: #333;");

        Button plusBtn = new Button("+");
        plusBtn.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-font-weight: bold; -fx-min-width: 30; -fx-background-radius: 5;");
        plusBtn.setCursor(javafx.scene.Cursor.HAND);

        final int productId = item.getProductId();
        final int currentQty = item.getQuantity();

        minusBtn.setOnAction(e -> {
            if (currentQty > 1) {
                DatabaseHelper.getInstance().updateCartQuantity(productId, currentQty - 1);
                refreshCart();
                if (onCartUpdate != null) onCartUpdate.run();
            } else {
                DatabaseHelper.getInstance().updateCartQuantity(productId, 0);
                refreshCart();
                if (onCartUpdate != null) onCartUpdate.run();
            }
        });

        plusBtn.setOnAction(e -> {
            DatabaseHelper.getInstance().updateCartQuantity(productId, currentQty + 1);
            refreshCart();
            if (onCartUpdate != null) onCartUpdate.run();
        });

        quantityBox.getChildren().addAll(minusBtn, qtyLabel, plusBtn);

        Label totalItemLabel = new Label(String.format("%,d руб.", item.getTotal()));
        totalItemLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        totalItemLabel.setPrefWidth(100);

        Button removeBtn = new Button("Удалить");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d32f2f; -fx-font-size: 12px; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            DatabaseHelper.getInstance().updateCartQuantity(productId, 0);
            refreshCart();
            if (onCartUpdate != null) onCartUpdate.run();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(nameLabel, priceLabel, quantityBox, totalItemLabel, spacer, removeBtn);
        cartItemsContainer.getChildren().add(row);
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            showAlert("Корзина пуста");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Оформление заказа");
        dialog.setHeaderText(null);
        dialog.setResizable(true);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        int total = cartItems.stream().mapToInt(CartItem::getTotal).sum();
        Label totalInfo = new Label(String.format("Сумма заказа: %,d руб.", total));
        totalInfo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label pickupLabel = new Label("Самовывоз (бесплатно)");
        pickupLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label addressLabel = new Label("г. Энгельс, ул.Плозадь свободы 17, 2 этаж");
        addressLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        addressLabel.setWrapText(true);

        Label paymentLabel = new Label("Способ оплаты:");
        paymentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ToggleGroup paymentGroup = new ToggleGroup();
        RadioButton sbpBtn = new RadioButton("Оплата через СБП");
        sbpBtn.setToggleGroup(paymentGroup);
        sbpBtn.setSelected(true);
        sbpBtn.setStyle("-fx-font-size: 13px;");

        Label sbpInfo = new Label("После нажатия кнопки оплаты вы перейдёте в приложение вашего банка для подтверждения платежа.");
        sbpInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        sbpInfo.setWrapText(true);

        VBox paymentBox = new VBox(5, sbpBtn, sbpInfo);

        content.getChildren().addAll(totalInfo, new Separator(), pickupLabel, addressLabel, new Separator(), paymentLabel, paymentBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Перейти к оплате");
        okButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Отмена");

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? ButtonType.OK : ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showPaymentSimulation(total);
            }
        });
    }

    private void showPaymentSimulation(int total) {
        Dialog<ButtonType> paymentDialog = new Dialog<>();
        paymentDialog.setTitle("Оплата через СБП");
        paymentDialog.setResizable(true);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: white;");

        Label title = new Label("Оплата заказа");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label amountLabel = new Label(String.format("Сумма к оплате: %,d руб.", total));
        amountLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        Label instruction = new Label(
                "1. Откройте приложение вашего банка\n" +
                        "2. Выберите раздел «Платежи» или «СБП»\n" +
                        "3. Нажмите «Оплатить по QR-коду»\n" +
                        "4. Отсканируйте QR-код\n" +
                        "5. Подтвердите платеж в приложении банка\n\n" +
                        "После успешной оплаты заказ будет автоматически оформлен."
        );
        instruction.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
        instruction.setWrapText(true);

        Label qrLabel = new Label(
                "┌─────────────────────┐\n" +
                        "│  █▀▀▀▀▀█ ▄ █▀▀▀▀▀█  │\n" +
                        "│  █ ███ █ ▀ █ ███ █  │\n" +
                        "│  █ ▀▀▀ █ ▄ █ ▀▀▀ █  │\n" +
                        "│  ▀▀▀▀▀▀▀ ▀ ▀▀▀▀▀▀▀  │\n" +
                        "│  █▄ ▄▀█▀▄█ ▀▄ ▀▄▀▄  │\n" +
                        "│  ▀█▄▄▀█▄▀▀▄▀▄█▀▀▄█  │\n" +
                        "│  █▀▀▀▀▀█ ▀ ▀▄▀▀█▀▀  │\n" +
                        "│  █ ███ █ ▄▄▀▀▄█▀▀▄  │\n" +
                        "│  █ ▀▀▀ █ ▀▄█▄ ▄▀▀▀  │\n" +
                        "│  ▀▀▀▀▀▀▀ ▀ ▀  ▀▀▀   │\n" +
                        "└─────────────────────┘"
        );
        qrLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 12px; -fx-text-fill: #333;");

        Button btnPaid = new Button("Я оплатил");
        btnPaid.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        btnPaid.setCursor(javafx.scene.Cursor.HAND);

        content.getChildren().addAll(title, amountLabel, qrLabel, instruction, btnPaid);

        paymentDialog.getDialogPane().setContent(content);
        paymentDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Button cancelBtn = (Button) paymentDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelBtn.setText("Отмена");

        btnPaid.setOnAction(e -> {
            int orderId = DatabaseHelper.getInstance().createOrder(total);
            if (orderId != -1) {
                for (CartItem item : cartItems) {
                    DatabaseHelper.getInstance().addOrderItem(orderId, item.getProductId(), item.getQuantity(), item.getPrice());
                    DatabaseHelper.getInstance().updateProductStock(item.getProductId(), item.getQuantity());
                }
                DatabaseHelper.getInstance().clearCart();
                refreshCart();
                if (onCartUpdate != null) onCartUpdate.run();

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Оплата прошла успешно!");
                success.setHeaderText(null);
                success.setContentText(String.format("Заказ №%d оформлен!\nТовары можно забрать по адресу самовывоза:\nг. Энгельс, ул. Ленина, д. 15", orderId));
                success.showAndWait();
                paymentDialog.close();
            }
        });

        paymentDialog.showAndWait();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}