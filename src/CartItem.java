public class CartItem {
    private int productId, price, quantity;
    private String name;

    public CartItem(int productId, String name, int price, int quantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getProductId() { return productId; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public int getTotal() { return price * quantity; }
}