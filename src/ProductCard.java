import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.InputStream;
import java.util.List;
import java.io.File;

public class ProductCard extends VBox {

    private Product product;
    private Runnable onAddToCart;
    private Label quantityLabel;
    private int currentQuantity = 1;

    public ProductCard(Product product, Runnable onAddToCart) {
        this.product = product;
        this.onAddToCart = onAddToCart;

        setSpacing(8);
        setPadding(new Insets(10));
        setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        setPrefWidth(220);
        setMinWidth(200);

        // Картинка
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        if (product.getImage() != null && !product.getImage().isEmpty()) {
            String basePath = "resources/images/products/";
            File file = new File(basePath + product.getImage());

            if (file.exists()) {
                Image img = new Image(file.toURI().toString());
                imageView.setImage(img);
                imageView.setStyle("-fx-background-radius: 12; -fx-border-radius: 12;");
            } else {
                File catFile = new File("resources/images/categories/" + product.getImage());
                if (catFile.exists()) {
                    Image img = new Image(catFile.toURI().toString());
                    imageView.setImage(img);
                    imageView.setStyle("-fx-background-radius: 12; -fx-border-radius: 12;");
                } else {
                    imageView.setStyle("-fx-background-color: #ccc; -fx-background-radius: 12;");
                }
            }
        } else {
            imageView.setStyle("-fx-background-color: #ccc; -fx-background-radius: 12;");
        }

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        nameLabel.setWrapText(true);

        double rating = DatabaseHelper.getInstance().getProductRating(product.getId());
        int reviewCount = DatabaseHelper.getInstance().getReviewCount(product.getId());
        Label ratingLabel = new Label(String.format("⭐ %.1f (%d отзывов)", rating, reviewCount));
        ratingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff9800; -fx-cursor: hand;");
        ratingLabel.setOnMouseClicked(e -> showReviewsDialog());

        Label priceLabel = new Label(product.getPrice() + " руб.");
        priceLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

        Label stockLabel = new Label(product.getStock() > 0 ? "В наличии: " + product.getStock() : "Нет в наличии");

        HBox quantityBox = new HBox(10);
        quantityBox.setAlignment(Pos.CENTER);

        Button minusBtn = new Button("-");
        minusBtn.setOnAction(e -> {
            if (currentQuantity > 1) {
                currentQuantity--;
                quantityLabel.setText(String.valueOf(currentQuantity));
            }
        });

        quantityLabel = new Label("1");

        Button plusBtn = new Button("+");
        plusBtn.setOnAction(e -> {
            if (currentQuantity < product.getStock()) {
                currentQuantity++;
                quantityLabel.setText(String.valueOf(currentQuantity));
            }
        });

        quantityBox.getChildren().addAll(minusBtn, quantityLabel, plusBtn);

        Button cartBtn = new Button("В корзину");
        cartBtn.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 10;");
        cartBtn.setMaxWidth(Double.MAX_VALUE);
        cartBtn.setDisable(product.getStock() <= 0);
        cartBtn.setOnAction(e -> {
            DatabaseHelper.getInstance().addToCart(product.getId(), currentQuantity);
            currentQuantity = 1;
            quantityLabel.setText("1");
            if (onAddToCart != null) onAddToCart.run();
            cartBtn.setText("Добавлено");
            cartBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
            pause.setOnFinished(event -> {
                cartBtn.setText("В корзину");
                cartBtn.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
            });
            pause.play();
        });

        Button configBtn = new Button("В конфигуратор");
        configBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 10;");
        configBtn.setMaxWidth(Double.MAX_VALUE);
        configBtn.setDisable(product.getStock() <= 0);
        configBtn.setOnAction(e -> {
            ConfiguratorView configView = new ConfiguratorView();
            configView.addProduct(product);

            Stage stage = (Stage) getScene().getWindow();
            BorderPane root = (BorderPane) stage.getScene().getRoot();
            VBox center = (VBox) root.getCenter();
            center.getChildren().clear();
            center.getChildren().add(configView.getView());
        });

        getChildren().addAll(imageView, nameLabel, ratingLabel, priceLabel, stockLabel, quantityBox, cartBtn, configBtn);

        setOnMouseEntered(e -> setStyle("-fx-border-color: #0078d4; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;"));
        setOnMouseExited(e -> setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;"));
    }

    private void showReviewsDialog() {
        List<DatabaseHelper.ProductReview> reviews = DatabaseHelper.getInstance().getProductReviews(product.getId());
        if (reviews.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Пока нет отзывов на этот товар");
            alert.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Отзывы о товаре: " + product.getName());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setStyle("-fx-background-color: #1e1e1e;");

        for (DatabaseHelper.ProductReview review : reviews) {
            VBox reviewBox = new VBox(5);
            reviewBox.setStyle("-fx-background-color: #2d2d2d; -fx-background-radius: 8; -fx-padding: 10;");

            String stars = "";
            for (int i = 0; i < review.getRating(); i++) stars += "⭐";

            Label ratingLabel = new Label(stars + " " + review.getUserName());
            ratingLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ff9800;");

            Label commentLabel = new Label(review.getComment());
            commentLabel.setStyle("-fx-text-fill: #ccc;");
            commentLabel.setWrapText(true);

            Label dateLabel = new Label(review.getDate());
            dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");

            reviewBox.getChildren().addAll(ratingLabel, commentLabel, dateLabel);

            if (review.getImage() != null && !review.getImage().isEmpty()) {
                ImageView reviewImage = new ImageView();
                reviewImage.setFitWidth(200);
                reviewImage.setFitHeight(150);
                reviewImage.setPreserveRatio(true);
                String imagePath = "/images/reviews/" + review.getImage();
                InputStream inputStream = getClass().getResourceAsStream(imagePath);
                if (inputStream != null) {
                    reviewImage.setImage(new Image(inputStream));
                    reviewBox.getChildren().add(reviewImage);
                }
            }

            vbox.getChildren().add(reviewBox);
        }

        ScrollPane scroll = new ScrollPane(vbox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #1e1e1e;");

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: #1e1e1e;");
        dialog.setResizable(true);

        dialog.showAndWait();
    }
}