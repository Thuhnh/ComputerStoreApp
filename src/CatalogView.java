import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import java.io.InputStream;

public class CatalogView {

    private Runnable onCartUpdate;
    private VBox view;
    private VBox categoriesPanel;
    private VBox productsPanel;
    private TilePane productsGrid;
    private TilePane categoriesGrid;
    private Label categoryTitle;
    private TextField searchField;

    public CatalogView(Runnable onCartUpdate) {
        this.onCartUpdate = onCartUpdate;
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

        Label title = new Label("КАТАЛОГ ТОВАРОВ");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        HBox searchBox = new HBox(10);
        searchField = new TextField();
        searchField.setPromptText("Поиск товаров...");
        searchField.setPrefWidth(400);
        Button btnSearch = new Button("Найти");
        btnSearch.setOnAction(e -> searchProducts());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        searchBox.getChildren().addAll(spacer, searchField, btnSearch);

        categoriesPanel = new VBox(15);
        categoriesPanel.setFillWidth(true);
        VBox.setVgrow(categoriesPanel, Priority.ALWAYS);

        Label catLabel = new Label("КАТЕГОРИИ");
        catLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        categoriesGrid = new TilePane();
        categoriesGrid.setHgap(20);
        categoriesGrid.setVgap(20);
        categoriesGrid.setPadding(new Insets(10));
        categoriesGrid.setPrefColumns(3);
        categoriesGrid.setAlignment(Pos.CENTER);
        categoriesGrid.prefWidthProperty().bind(view.widthProperty().subtract(40));

        String[][] categories = {
                {"Процессоры", "cpu.png"},
                {"Видеокарты", "vid.png"},
                {"Материнские платы", "mat.png"},
                {"Оперативная память", "oper.png"},
                {"Накопители", "ssd.png"},
                {"Блоки питания", "bp.png"},
                {"Охлаждение", "col.png"},
                {"Мониторы", "mon.png"},
                {"Мыши", "msh.png"},
                {"Клавиатуры", "klav.png"},
                {"Наушники", "nau.png"},
                {"Корпуса", "corp.png"}
        };

        for (String[] cat : categories) {
            VBox categoryCard = createCategoryCard(cat[0], cat[1]);
            categoriesGrid.getChildren().add(categoryCard);
        }

        ScrollPane categoriesScroll = new ScrollPane(categoriesGrid);
        categoriesScroll.setFitToWidth(true);
        categoriesScroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(categoriesScroll, Priority.ALWAYS);

        categoriesPanel.getChildren().addAll(catLabel, categoriesScroll);

        productsPanel = new VBox(10);
        productsPanel.setFillWidth(true);
        productsPanel.setVisible(false);
        productsPanel.setManaged(false);
        VBox.setVgrow(productsPanel, Priority.ALWAYS);

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnBack = new Button("Назад к категориям");
        btnBack.setOnAction(e -> showCategories());

        categoryTitle = new Label();
        categoryTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        topBar.getChildren().addAll(btnBack, spacer2, categoryTitle);

        productsGrid = new TilePane();
        productsGrid.setHgap(20);
        productsGrid.setVgap(20);
        productsGrid.setPrefColumns(4);
        productsGrid.setPadding(new Insets(10));
        productsGrid.setAlignment(Pos.TOP_LEFT);

        ScrollPane productsScroll = new ScrollPane(productsGrid);
        productsScroll.setFitToWidth(true);
        productsScroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(productsScroll, Priority.ALWAYS);

        productsPanel.getChildren().addAll(topBar, productsScroll);

        view.getChildren().addAll(title, searchBox, categoriesPanel, productsPanel);
    }

    private VBox createCategoryCard(String categoryName, String imageFile) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefHeight(140);
        card.setMaxWidth(300);
        card.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

        String resourcePath = "/images/categories/" + imageFile;
        InputStream inputStream = getClass().getResourceAsStream(resourcePath);
        if (inputStream != null) {
            Image img = new Image(inputStream);
            imageView.setImage(img);
        } else {
            imageView.setStyle("-fx-background-color: #ccc;");
        }

        Label nameLabel = new Label(categoryName);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        card.getChildren().addAll(imageView, nameLabel);

        card.prefWidthProperty().bind(categoriesGrid.widthProperty().divide(3).subtract(15));

        card.setOnMouseClicked(e -> loadProductsByCategory(categoryName));
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #0078d4; -fx-border-radius: 12; -fx-background-radius: 12;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 12; -fx-background-radius: 12;"));

        return card;
    }

    private void loadProductsByCategory(String category) {
        productsGrid.getChildren().clear();
        categoryTitle.setText(category);

        var products = DatabaseHelper.getInstance().getProductsByCategory(category);
        for (Product p : products) {
            ProductCard card = new ProductCard(p, () -> {
                if (onCartUpdate != null) onCartUpdate.run();
            });
            productsGrid.getChildren().add(card);
        }

        if (products.isEmpty()) {
            Label empty = new Label("В этой категории пока нет товаров");
            productsGrid.getChildren().add(empty);
        }

        showProducts();
    }

    private void searchProducts() {
        String search = searchField.getText().trim();
        if (search.isEmpty()) return;

        productsGrid.getChildren().clear();
        categoryTitle.setText("Результаты поиска: " + search);

        var allProducts = DatabaseHelper.getInstance().getAllProductsForSearch();
        for (Product p : allProducts) {
            if (p.getName().toLowerCase().contains(search.toLowerCase())) {
                ProductCard card = new ProductCard(p, () -> {
                    if (onCartUpdate != null) onCartUpdate.run();
                });
                productsGrid.getChildren().add(card);
            }
        }

        if (productsGrid.getChildren().isEmpty()) {
            Label empty = new Label("Товары не найдены");
            productsGrid.getChildren().add(empty);
        }

        showProducts();
    }

    private void showCategories() {
        categoriesPanel.setVisible(true);
        categoriesPanel.setManaged(true);
        productsPanel.setVisible(false);
        productsPanel.setManaged(false);
    }

    private void showProducts() {
        categoriesPanel.setVisible(false);
        categoriesPanel.setManaged(false);
        productsPanel.setVisible(true);
        productsPanel.setManaged(true);
    }
}