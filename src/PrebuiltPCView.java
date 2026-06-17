import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import java.io.InputStream;

public class PrebuiltPCView {

    private VBox view;
    private Runnable onCartUpdate;

    public PrebuiltPCView(Runnable onCartUpdate) {
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

        Label title = new Label("ГОТОВЫЕ СБОРКИ ПК");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label desc = new Label("Готовые компьютеры, собранные и протестированные");
        desc.setStyle("-fx-text-fill: #666;");

        TilePane productsGrid = new TilePane();
        productsGrid.setHgap(20);
        productsGrid.setVgap(20);
        productsGrid.setPrefColumns(3);
        productsGrid.setPadding(new Insets(10));

        var prebuiltPCs = DatabaseHelper.getInstance().getPrebuiltPCs();
        for (Product p : prebuiltPCs) {
            productsGrid.getChildren().add(createPCCard(p));
        }

        if (prebuiltPCs.isEmpty()) {
            Label empty = new Label("Готовые сборки временно отсутствуют");
            empty.setStyle("-fx-text-fill: #888; -fx-font-size: 16px;");
            productsGrid.getChildren().add(empty);
        }

        ScrollPane scrollPane = new ScrollPane(productsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        view.getChildren().addAll(title, desc, scrollPane);
    }

    private VBox createPCCard(Product pc) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(280);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(260);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-color: #eee; -fx-background-radius: 8;");

        if (pc.getImage() != null && !pc.getImage().isEmpty()) {
            String resourcePath = "/images/products/" + pc.getImage();
            InputStream inputStream = getClass().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                resourcePath = "/images/categories/" + pc.getImage();
                inputStream = getClass().getResourceAsStream(resourcePath);
            }
            if (inputStream != null) {
                Image img = new Image(inputStream);
                imageView.setImage(img);
            } else {
                imageView.setImage(createPlaceholderImage());
            }
        } else {
            imageView.setImage(createPlaceholderImage());
        }

        Label nameLabel = new Label(pc.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label(pc.getPrice() + " руб.");
        priceLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label stockLabel = new Label(pc.getStock() > 0 ? "В наличии: " + pc.getStock() : "Нет в наличии");
        stockLabel.setStyle("-fx-font-size: 12px;");

        Label componentsTitle = new Label("Состав:");
        componentsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Label componentsText = new Label(pc.getSpecifications() != null ? pc.getSpecifications() : "Не указан");
        componentsText.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        componentsText.setWrapText(true);

        Button buyBtn = new Button("В корзину");
        buyBtn.setMaxWidth(Double.MAX_VALUE);
        buyBtn.setDisable(pc.getStock() <= 0);
        buyBtn.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 0;");
        buyBtn.setOnAction(e -> {
            DatabaseHelper.getInstance().addToCart(pc.getId(), 1);
            if (onCartUpdate != null) onCartUpdate.run();
            buyBtn.setText("Добавлено");
            buyBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
            pause.setOnFinished(event -> {
                buyBtn.setText("В корзину");
                buyBtn.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
            });
            pause.play();
        });

        card.getChildren().addAll(imageView, nameLabel, priceLabel, stockLabel, componentsTitle, componentsText, buyBtn);

        card.setOnMouseEntered(e -> card.setStyle("-fx-border-color: #0078d4; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;"));

        return card;
    }

    private Image createPlaceholderImage() {
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(260, 160);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.LIGHTGRAY);
        gc.fillRect(0, 0, 260, 160);
        gc.setFill(javafx.scene.paint.Color.GRAY);
        gc.fillText("Нет фото", 100, 80);
        return canvas.snapshot(null, null);
    }
}