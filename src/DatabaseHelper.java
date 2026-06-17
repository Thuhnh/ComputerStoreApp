import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private Connection conn;
    private static DatabaseHelper instance;

    public static DatabaseHelper getInstance() {
        if (instance == null) instance = new DatabaseHelper();
        return instance;
    }

    private DatabaseHelper() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(
                    "jdbc:sqlserver://localhost;databaseName=ComputerStore;encrypt=true;trustServerCertificate=true",
                    "sa", "12345"
            );
            System.out.println("✅ БД подключена");
        } catch (Exception e) {
            System.out.println("❌ Ошибка БД: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int login(String login, String pass) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT UserId, Role, FullName FROM Users " +
                            "WHERE Login = ? COLLATE SQL_Latin1_General_CP1_CS_AS " +
                            "AND Password = ? COLLATE SQL_Latin1_General_CP1_CS_AS"
            );
            ps.setString(1, login);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                MainApp.userId = rs.getInt("UserId");
                MainApp.userRole = rs.getString("Role");
                MainApp.userName = rs.getString("FullName");
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean register(String login, String pass, String name) {
        try {
            PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM Users WHERE Login = ?");
            check.setString(1, login);
            ResultSet rs = check.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                return false;
            }

            PreparedStatement ps = conn.prepareStatement("INSERT INTO Users (Login, Password, FullName, Role, LoyaltyPoints) VALUES (?, ?, ?, 'User', 0)");
            ps.setString(1, login);
            ps.setString(2, pass);
            ps.setString(3, name);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getCategories() {
        List<String> list = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT Name FROM Categories");
            while (rs.next()) list.add(rs.getString("Name"));
        } catch (Exception e) {}
        return list;
    }

    public List<Product> getProductsByCategory(String category) {
        List<Product> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT p.ProductId, p.Name, p.Price, p.Stock, p.Type, p.Image, p.Specifications, c.Name as Category " +
                            "FROM Products p JOIN Categories c ON p.CategoryId=c.CategoryId WHERE c.Name=?"
            );
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("ProductId"), rs.getString("Name"),
                        rs.getInt("Price"), rs.getInt("Stock"),
                        rs.getString("Category"), rs.getString("Type"),
                        rs.getString("Image"), rs.getString("Specifications")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> getProductsByType(String type) {
        List<Product> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT p.ProductId, p.Name, p.Price, p.Stock, p.Type, p.Image, p.Specifications, c.Name as Category " +
                            "FROM Products p JOIN Categories c ON p.CategoryId=c.CategoryId WHERE p.Type=?"
            );
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("ProductId"), rs.getString("Name"),
                        rs.getInt("Price"), rs.getInt("Stock"),
                        rs.getString("Category"), rs.getString("Type"),
                        rs.getString("Image"), rs.getString("Specifications")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> getPrebuiltPCs() {
        List<Product> list = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT ProductId, Name, Price, Stock, Specifications, Image FROM Products WHERE Type='PrebuiltPC'"
            );
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("ProductId"), rs.getString("Name"),
                        rs.getInt("Price"), rs.getInt("Stock"),
                        "Готовые ПК", "PrebuiltPC",
                        rs.getString("Image"), rs.getString("Specifications")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> getAllProductsForSearch() {
        List<Product> list = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT p.ProductId, p.Name, p.Price, p.Stock, p.Type, p.Image, p.Specifications, c.Name as Category " +
                            "FROM Products p JOIN Categories c ON p.CategoryId=c.CategoryId"
            );
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("ProductId"), rs.getString("Name"),
                        rs.getInt("Price"), rs.getInt("Stock"),
                        rs.getString("Category"), rs.getString("Type"),
                        rs.getString("Image"), rs.getString("Specifications")
                ));
            }
            stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void addToCart(int productId, int quantity) {
        try {
            PreparedStatement check = conn.prepareStatement("SELECT Quantity FROM Cart WHERE UserId=? AND ProductId=?");
            check.setInt(1, MainApp.userId);
            check.setInt(2, productId);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                int newQty = rs.getInt("Quantity") + quantity;
                PreparedStatement update = conn.prepareStatement("UPDATE Cart SET Quantity=? WHERE UserId=? AND ProductId=?");
                update.setInt(1, newQty);
                update.setInt(2, MainApp.userId);
                update.setInt(3, productId);
                update.executeUpdate();
            } else {
                PreparedStatement insert = conn.prepareStatement("INSERT INTO Cart (UserId, ProductId, Quantity) VALUES (?,?,?)");
                insert.setInt(1, MainApp.userId);
                insert.setInt(2, productId);
                insert.setInt(3, quantity);
                insert.executeUpdate();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<CartItem> getCart() {
        List<CartItem> list = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT c.ProductId, p.Name, p.Price, c.Quantity FROM Cart c JOIN Products p ON c.ProductId=p.ProductId WHERE c.UserId=" + MainApp.userId
            );
            while (rs.next()) {
                list.add(new CartItem(
                        rs.getInt("ProductId"),
                        rs.getString("Name"),
                        rs.getInt("Price"),
                        rs.getInt("Quantity")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void updateCartQuantity(int productId, int quantity) {
        try {
            if (quantity <= 0) {
                conn.createStatement().execute("DELETE FROM Cart WHERE UserId=" + MainApp.userId + " AND ProductId=" + productId);
            } else {
                PreparedStatement ps = conn.prepareStatement("UPDATE Cart SET Quantity=? WHERE UserId=? AND ProductId=?");
                ps.setInt(1, quantity);
                ps.setInt(2, MainApp.userId);
                ps.setInt(3, productId);
                ps.executeUpdate();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void clearCart() {
        try {
            conn.createStatement().execute("DELETE FROM Cart WHERE UserId=" + MainApp.userId);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public int createOrder(int total) {
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Orders (UserId, TotalAmount, Status) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, MainApp.userId);
            ps.setInt(2, total);
            ps.setString(3, "New");
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    public void addOrderItem(int orderId, int productId, int quantity, int price) {
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO OrderItems (OrderId, ProductId, Quantity, PriceAtTime) VALUES (?,?,?,?)");
            ps.setInt(1, orderId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.setInt(4, price);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void addProduct(String name, int price, int stock, int catId, String type, String image) {
        try {
            System.out.println("Добавляем товар: " + name);
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Products (Name, Price, Stock, CategoryId, Type, Image) VALUES (?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, name);
            ps.setInt(2, price);
            ps.setInt(3, stock);
            ps.setInt(4, catId);
            ps.setString(5, type);
            ps.setString(6, image);
            int rows = ps.executeUpdate();
            System.out.println("Добавлено строк: " + rows);
        } catch (Exception e) {
            System.out.println("Ошибка добавления: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateProduct(int id, String name, int price, int stock, int catId) {
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE Products SET Name=?, Price=?, Stock=?, CategoryId=? WHERE ProductId=?");
            ps.setString(1, name);
            ps.setInt(2, price);
            ps.setInt(3, stock);
            ps.setInt(4, catId);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void deleteProduct(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM Products WHERE ProductId=?");
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void addPrebuiltPC(String name, int price, int stock, String components, String image) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Products (Name, Price, Stock, CategoryId, Type, Specifications, Image) VALUES (?, ?, ?, 12, 'PrebuiltPC', ?, ?)"
            );
            ps.setString(1, name);
            ps.setInt(2, price);
            ps.setInt(3, stock);
            ps.setString(4, components);
            ps.setString(5, image);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void updatePrebuiltPC(int id, String name, int price, int stock, String components) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Products SET Name=?, Price=?, Stock=?, Specifications=? WHERE ProductId=? AND Type='PrebuiltPC'"
            );
            ps.setString(1, name);
            ps.setInt(2, price);
            ps.setInt(3, stock);
            ps.setString(4, components);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<AdminPanelView.OrderInfo> getAllOrders() {
        List<AdminPanelView.OrderInfo> list = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT o.OrderId, u.FullName as UserName, o.OrderDate, o.TotalAmount, o.Status " +
                            "FROM Orders o JOIN Users u ON o.UserId = u.UserId ORDER BY o.OrderDate DESC"
            );
            while (rs.next()) {
                list.add(new AdminPanelView.OrderInfo(
                        rs.getInt("OrderId"),
                        rs.getString("UserName"),
                        rs.getString("OrderDate"),
                        rs.getInt("TotalAmount"),
                        rs.getString("Status")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void updateOrderStatus(int orderId, String status) {
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE Orders SET Status=? WHERE OrderId=?");
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<OrderHistoryView.OrderInfo> getUserOrders() {
        List<OrderHistoryView.OrderInfo> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT OrderId, OrderDate, TotalAmount, Status FROM Orders WHERE UserId = ? ORDER BY OrderDate DESC"
            );
            ps.setInt(1, MainApp.userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int orderId = rs.getInt("OrderId");
                boolean canReview = !hasUserReviewedOrder(orderId);
                list.add(new OrderHistoryView.OrderInfo(
                        orderId,
                        rs.getString("OrderDate"),
                        rs.getInt("TotalAmount"),
                        rs.getString("Status"),
                        canReview && isOrderDelivered(orderId)
                ));
            }
        } catch (Exception e) {}
        return list;
    }

    private boolean isOrderDelivered(int orderId) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT Status FROM Orders WHERE OrderId = ?");
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String status = rs.getString("Status");
                return status.equals("Доставлен") || status.equals("Выдан");
            }
        } catch (Exception e) {}
        return false;
    }

    private boolean hasUserReviewedOrder(int orderId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Reviews r " +
                            "JOIN OrderItems oi ON r.ProductId = oi.ProductId " +
                            "WHERE oi.OrderId = ? AND r.UserId = ?"
            );
            ps.setInt(1, orderId);
            ps.setInt(2, MainApp.userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {}
        return false;
    }

    public List<Product> getOrderItems(int orderId) {
        List<Product> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT p.ProductId, p.Name, p.Price, p.Image FROM OrderItems oi " +
                            "JOIN Products p ON oi.ProductId = p.ProductId WHERE oi.OrderId = ?"
            );
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("ProductId"),
                        rs.getString("Name"),
                        rs.getInt("Price"),
                        0,
                        "",
                        "",
                        rs.getString("Image"),
                        ""
                ));
            }
        } catch (Exception e) {}
        return list;
    }

    public boolean addReview(int userId, int productId, int rating, String comment, String imagePath) {
        try {
            PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM Reviews WHERE UserId = ? AND ProductId = ?");
            check.setInt(1, userId);
            check.setInt(2, productId);
            ResultSet rs = check.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                return false;
            }

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Reviews (UserId, ProductId, Rating, Comment, ReviewDate, Image) VALUES (?, ?, ?, ?, GETDATE(), ?)"
            );
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setInt(3, rating);
            ps.setString(4, comment);
            ps.setString(5, imagePath);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getProductRating(int productId) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT AVG(Rating) as AvgRating FROM Reviews WHERE ProductId = ?");
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("AvgRating");
            }
        } catch (Exception e) {}
        return 0;
    }

    public List<ProductReview> getProductReviews(int productId) {
        List<ProductReview> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT r.Rating, r.Comment, r.ReviewDate, r.Image, u.FullName as UserName " +
                            "FROM Reviews r JOIN Users u ON r.UserId = u.UserId WHERE r.ProductId = ? ORDER BY r.ReviewDate DESC"
            );
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ProductReview(
                        rs.getInt("Rating"),
                        rs.getString("Comment"),
                        rs.getString("ReviewDate"),
                        rs.getString("Image"),
                        rs.getString("UserName")
                ));
            }
        } catch (Exception e) {}
        return list;
    }

    public static class ProductReview {
        private int rating;
        private String comment;
        private String date;
        private String image;
        private String userName;

        public ProductReview(int rating, String comment, String date, String image, String userName) {
            this.rating = rating;
            this.comment = comment;
            this.date = date;
            this.image = image;
            this.userName = userName;
        }

        public int getRating() { return rating; }
        public String getComment() { return comment; }
        public String getDate() { return date; }
        public String getImage() { return image; }
        public String getUserName() { return userName; }
    }

    public int getReviewCount(int productId) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Reviews WHERE ProductId = ?");
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {}
        return 0;
    }

    public void updateProductStock(int productId, int quantity) {
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE Products SET Stock = Stock - ? WHERE ProductId = ?");
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}