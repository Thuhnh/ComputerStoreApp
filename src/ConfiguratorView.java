import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ConfiguratorView {

    private VBox view;
    private Map<String, Product> selectedComponents;
    private GridPane slotsGrid;
    private Button btnAddAllToCart;
    private Label statusLabel;

    private final String[] REQUIRED_TYPES = {
            "Processor", "Motherboard", "VideoCard", "RAM", "SSD", "PSU", "Cooling"
    };

    public ConfiguratorView() {
        selectedComponents = new HashMap<>();
        build();
    }

    public VBox getView() {
        return view;
    }

    public void addProduct(Product product) {
        String type = product.getType();
        if (type == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Этот товар нельзя добавить в конфигуратор");
            alert.showAndWait();
            return;
        }

        if (selectedComponents.containsKey(type)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Товар этого типа уже выбран. Сначала удалите его");
            alert.showAndWait();
            return;
        }

        selectedComponents.put(type, product);
        updateSlot(type, product);
        updateButtonState();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(product.getName() + " добавлен в конфигуратор");
        alert.showAndWait();
    }

    private void build() {
        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setFillWidth(true);

        Label title = new Label("КОНФИГУРАТОР ПК");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        slotsGrid = new GridPane();
        slotsGrid.setHgap(20);
        slotsGrid.setVgap(20);
        slotsGrid.setPadding(new Insets(10));
        slotsGrid.setAlignment(Pos.CENTER);

        String[][] components = {
                {"Процессор", "Processor"},
                {"Материнская плата", "Motherboard"},
                {"Видеокарта", "VideoCard"},
                {"Оперативная память", "RAM"},
                {"Накопитель", "SSD"},
                {"Блок питания", "PSU"},
                {"Охлаждение", "Cooling"}
        };

        int row = 0;
        int col = 0;
        for (String[] comp : components) {
            ComponentSlot slot = new ComponentSlot(comp[0], comp[1]);
            slotsGrid.add(slot, col, row);
            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }

        statusLabel = new Label("Заполните все компоненты (7/7)");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ff9800;");
        statusLabel.setAlignment(Pos.CENTER);

        btnAddAllToCart = new Button("Добавить сборку в корзину");
        btnAddAllToCart.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8;");
        btnAddAllToCart.setMaxWidth(Double.MAX_VALUE);
        btnAddAllToCart.setDisable(true);
        btnAddAllToCart.setOnAction(e -> addAllToCart());

        view.getChildren().addAll(title, slotsGrid, statusLabel, btnAddAllToCart);
    }

    private void updateButtonState() {
        int filled = 0;
        for (String type : REQUIRED_TYPES) {
            if (selectedComponents.containsKey(type)) {
                filled++;
            }
        }

        boolean allFilled = (filled == REQUIRED_TYPES.length);
        btnAddAllToCart.setDisable(!allFilled);

        if (allFilled) {
            statusLabel.setText("Сборка готова! Можете добавить в корзину");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        } else {
            statusLabel.setText("Заполните все компоненты (" + filled + "/" + REQUIRED_TYPES.length + ")");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ff9800;");
        }
    }

    private void updateSlot(String type, Product product) {
        for (javafx.scene.Node node : slotsGrid.getChildren()) {
            if (node instanceof ComponentSlot) {
                ComponentSlot slot = (ComponentSlot) node;
                if (slot.getComponentType().equals(type)) {
                    slot.setSelectedProduct(product);
                    break;
                }
            }
        }
    }

    private void addAllToCart() {
        for (Product p : selectedComponents.values()) {
            DatabaseHelper.getInstance().addToCart(p.getId(), 1);
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Все компоненты добавлены в корзину");
        alert.showAndWait();
    }

    private class ComponentSlot extends VBox {

        private String componentType;
        private Product selectedProduct;
        private Label productNameLabel;
        private Label productPriceLabel;
        private ImageView productImage;
        private Button removeBtn;

        public ComponentSlot(String componentName, String componentType) {
            this.componentType = componentType;

            setAlignment(Pos.CENTER);
            setPrefWidth(200);
            setMinHeight(220);
            setStyle("-fx-background-color: #fafafa; -fx-border-color: #ddd; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 10;");

            Label titleLabel = new Label(componentName);
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            productImage = new ImageView();
            productImage.setFitWidth(80);
            productImage.setFitHeight(80);
            productImage.setPreserveRatio(true);
            productImage.setStyle("-fx-background-color: #eee; -fx-background-radius: 8;");

            productNameLabel = new Label("Не выбран");
            productNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            productNameLabel.setWrapText(true);
            productNameLabel.setAlignment(Pos.CENTER);

            productPriceLabel = new Label("");
            productPriceLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

            removeBtn = new Button("Удалить");
            removeBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 10;");
            removeBtn.setMaxWidth(Double.MAX_VALUE);
            removeBtn.setVisible(false);
            removeBtn.setOnAction(e -> {
                if (selectedProduct != null) {
                    selectedComponents.remove(componentType);
                    setSelectedProduct(null);
                    updateButtonState();
                }
            });

            getChildren().addAll(titleLabel, productImage, productNameLabel, productPriceLabel, removeBtn);
        }

        public String getComponentType() {
            return componentType;
        }

        public void setSelectedProduct(Product product) {
            this.selectedProduct = product;
            if (product != null) {
                productNameLabel.setText(product.getName());
                productPriceLabel.setText(product.getPrice() + " руб.");

                if (product.getImage() != null && !product.getImage().isEmpty()) {
                    String resourcePath = "/images/products/" + product.getImage();
                    InputStream inputStream = getClass().getResourceAsStream(resourcePath);
                    if (inputStream == null) {
                        resourcePath = "/images/categories/" + product.getImage();
                        inputStream = getClass().getResourceAsStream(resourcePath);
                    }
                    if (inputStream != null) {
                        Image img = new Image(inputStream);
                        productImage.setImage(img);
                    } else {
                        productImage.setImage(null);
                    }
                } else {
                    productImage.setImage(null);
                }
                removeBtn.setVisible(true);
            } else {
                productNameLabel.setText("Не выбран");
                productPriceLabel.setText("");
                productImage.setImage(null);
                removeBtn.setVisible(false);
            }
        }
    }
}