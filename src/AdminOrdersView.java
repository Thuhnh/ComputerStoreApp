import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdminOrdersView {

    private VBox view;

    public AdminOrdersView() {
        build();
    }

    public VBox getView() {
        return view;
    }

    private void build() {
        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setFillWidth(true);
        VBox.setVgrow(view, Priority.ALWAYS);

        Label title = new Label("УПРАВЛЕНИЕ ЗАКАЗАМИ");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TableView<AdminPanelView.OrderInfo> ordersTable = new TableView<>();
        ordersTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<AdminPanelView.OrderInfo, Integer> colId = new TableColumn<>("№ заказа");
        colId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colId.setPrefWidth(80);

        TableColumn<AdminPanelView.OrderInfo, String> colUser = new TableColumn<>("Пользователь");
        colUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colUser.setPrefWidth(150);

        TableColumn<AdminPanelView.OrderInfo, String> colDate = new TableColumn<>("Дата");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setPrefWidth(150);

        TableColumn<AdminPanelView.OrderInfo, Integer> colTotal = new TableColumn<>("Сумма");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setPrefWidth(100);

        TableColumn<AdminPanelView.OrderInfo, String> colStatus = new TableColumn<>("Статус");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(120);

        TableColumn<AdminPanelView.OrderInfo, Void> colAction = new TableColumn<>("Действие");
        colAction.setPrefWidth(150);

        ordersTable.getColumns().addAll(colId, colUser, colDate, colTotal, colStatus, colAction);
        VBox.setVgrow(ordersTable, Priority.ALWAYS);

        ObservableList<AdminPanelView.OrderInfo> orders = FXCollections.observableArrayList(DatabaseHelper.getInstance().getAllOrders());
        ordersTable.setItems(orders);

        colAction.setCellFactory(param -> new TableCell<>() {
            private final ComboBox<String> statusCombo = new ComboBox<>();
            {
                statusCombo.getItems().addAll("Новый", "В обработке", "Собран", "Доставлен", "Выдан", "Отменён");
                statusCombo.setStyle("-fx-background-color: #d6d6d6; -fx-text-fill: white;");
                statusCombo.setOnAction(e -> {
                    AdminPanelView.OrderInfo order = getTableView().getItems().get(getIndex());
                    String newStatus = statusCombo.getValue();
                    if (newStatus != null && !newStatus.equals(order.getStatus())) {
                        DatabaseHelper.getInstance().updateOrderStatus(order.getOrderId(), newStatus);
                        order.setStatus(newStatus);
                        getTableView().refresh();
                        showAlert("Статус заказа №" + order.getOrderId() + " изменён на \"" + newStatus + "\"");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    AdminPanelView.OrderInfo order = getTableView().getItems().get(getIndex());
                    statusCombo.setValue(order.getStatus());
                    setGraphic(statusCombo);
                }
            }
        });

        view.getChildren().addAll(title, ordersTable);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}