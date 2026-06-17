public class Product {
    private int id, price, stock;
    private String name, category, type, image, specifications;

    public Product(int id, String name, int price, int stock, String category, String type, String image, String specifications) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.type = type;
        this.image = image;
        this.specifications = specifications;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getStock() { return stock; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public String getImage() { return image; }
    public String getSpecifications() { return specifications; }
}