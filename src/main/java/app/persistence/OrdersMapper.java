package app.persistence;

import app.entities.Order;
import app.exceptions.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrdersMapper {

    public static int addOrder(double carportWidth, double carportLength, double carportHeight, int userId, double shedWidth, double shedLength, String email, String orderDate, ConnectionPool connectionPool) throws DatabaseException{

        String sql = "INSERT INTO ordrene (material_cost, sales_price, carport_width, carport_length, carport_height, user_id, order_status, shed_width, shed_length, email, orderdate) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)){
                ps.setDouble(1, 0);
                ps.setDouble(2, 0);
                ps.setDouble(3, carportWidth);
                ps.setDouble(4, carportLength);
                ps.setDouble(5, carportHeight);
                ps.setInt(6, userId);
                ps.setString(7, "Creating");
                ps.setDouble(8, shedWidth);
                ps.setDouble(9, shedLength);
                ps.setString(10, email);
                ps.setString(11, orderDate);

                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys(); // order_id er autogenereret
                rs.next();
                return rs.getInt(1);
            } catch (SQLException e){
                e.printStackTrace();
            }
        } catch (SQLException e){
            throw new DatabaseException("Det lykkedes ikke at gemme ordren", e.getMessage());
        }
        return 0;
    }

    public static Order getOrderByEmail(String email, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "select * from public.ordrene where email=?";
        try
                (
                        Connection connection = connectionPool.getConnection();
                        PreparedStatement ps = connection.prepareStatement(sql)
                ) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                int orderId = rs.getInt("order_id");
                int carportLength = rs.getInt("carport_length");
                int carportWidth = rs.getInt("carport_width");
                int carportHeight = rs.getInt("carport_height");
                int materialCost = rs.getInt("material_cost");
                String status = rs.getString("order_status");
                int shedWidth = rs.getInt("shed_width");
                int shedLength = rs.getInt("shed_length");
                int salesPrice = rs.getInt("sales_price");
                String orderDate = rs.getString("orderdate");
                return new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, status, shedWidth, shedLength, email, orderDate);
            } else {
                throw new DatabaseException("Fejl i hentning af ordre!");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }

    public static Order getOrderByName(String userName, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "select * from public.ordrene where name=?";
        try
                (
                        Connection connection = connectionPool.getConnection();
                        PreparedStatement ps = connection.prepareStatement(sql)
                ) {
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                int orderId = rs.getInt("order_id");
                int carportLength = rs.getInt("carport_length");
                int carportWidth = rs.getInt("carport_width");
                int carportHeight = rs.getInt("carport_height");
                int materialCost = rs.getInt("material_cost");
                String status = rs.getString("order_status");
                int shedWidth = rs.getInt("shed_width");
                int shedLength = rs.getInt("shed_length");
                int salesPrice = rs.getInt("sales_price");
                String email = rs.getString("email");
                String orderDate = rs.getString("orderdate");
                return new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, status, shedWidth, shedLength, email, orderDate);
            } else {
                throw new DatabaseException("Fejl i hentning af ordre!");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }
    public static ArrayList<Order> getAllOrders(ConnectionPool connectionPool) throws DatabaseException {

        ArrayList<Order> orderList = new ArrayList<>();
        String sql = "SELECT * FROM ordrene";
        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ResultSet rs = ps.executeQuery();
                while (rs.next()){
                    int orderId = rs.getInt("order_id");
                    double materialCost = rs.getDouble("material_cost");
                    double salesPrice = rs.getDouble("sales_price");
                    double carportWidth = rs.getDouble("carport_width");
                    double carportLength = rs.getDouble("carport_length");
                    double carportHeight = rs.getDouble("carport_height");
                    int userId = rs.getInt("user_id");
                    String orderStatus = rs.getString("order_status");
                    double shedWidth = rs.getDouble("shed_width");
                    double shedLength = rs.getDouble("shed_length");
                    String email = rs.getString("email");
                    String orderDate = rs.getString("orderdate");
                    orderList.add(new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, orderStatus, shedWidth, shedLength, email, orderDate));
                }
            }
        }catch (SQLException e){
            throw new DatabaseException("Vi kunne ikke hente ordrelisten fra Databasen!", e.getMessage());
        }
        return orderList;
    }

    public static Order getOrderByOrderId(int orderId, ConnectionPool connectionPool) throws DatabaseException {

        Order order = null;
        String sql = "SELECT * FROM ordrene WHERE order_id = ?";

        try (Connection connection = connectionPool.getConnection() ){
            try (PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setInt(1,orderId);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {
                    double materialCost = rs.getDouble("material_cost");
                    double salesPrice = rs.getDouble("sales_price");
                    double carportWidth = rs.getDouble("carport_width");
                    double carportLength = rs.getDouble("carport_length");
                    double carportHeight = rs.getDouble("carport_height");
                    int userId = rs.getInt("user_id");
                    String orderStatus = rs.getString("order_status");
                    double shedWidth = rs.getDouble("shed_width");
                    double shedLength = rs.getDouble("shed_length");
                    String email = rs.getString("email");
                    String orderDate = rs.getString("orderdate");
                    order = new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, orderStatus, shedWidth, shedLength, email, orderDate);
                } else {
                    throw new DatabaseException("Der findes ikke ordre med det ordreId i databasen");
                }
            }
        } catch (SQLException e){
            throw new DatabaseException("Det lykkedes ikke at hente brugerens ordre ved at søge på ordreid", e.getMessage());
        }
        return order;
    }

    public static Order getOrderByUserId(int userId, ConnectionPool connectionPool) throws DatabaseException {

        Order order = null;
        String sql = "SELECT * FROM ordrene WHERE user_id = ?";

        try (Connection connection = connectionPool.getConnection() ){
            try (PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {
                    double materialCost = rs.getDouble("material_cost");
                    double salesPrice = rs.getDouble("sales_price");
                    double carportWidth = rs.getDouble("carport_width");
                    double carportLength = rs.getDouble("carport_length");
                    double carportHeight = rs.getDouble("carport_height");
                    int orderId = rs.getInt("order_id");
                    String orderStatus = rs.getString("order_status");
                    double shedWidth = rs.getDouble("shed_width");
                    double shedLength = rs.getDouble("shed_length");
                    String email = rs.getString("email");
                    String orderDate = rs.getString("orderdate");
                    order = new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, orderStatus, shedWidth, shedLength, email, orderDate);
                } else {
                    throw new DatabaseException("Der findes ikke en ordre med det userId i databasen");
                }
            }
        } catch (SQLException e){
            throw new DatabaseException("Det lykkedes ikke at hente brugerens ordre ved at søge på userid", e.getMessage());
        }
        return order;
    }

    public static void adjustSalesPrice(int orderId, double newSalesPrice, ConnectionPool connectionPool) throws DatabaseException{

        String sql = "UPDATE ordrene SET sales_price = ? WHERE order_id = ?";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
             ps.setDouble(1 ,newSalesPrice);
             ps.setInt(2,orderId);

             ps.executeUpdate();
            }
        } catch (SQLException e){
            throw new DatabaseException("Det lykkedes ikke at justere salgsprisen", e.getMessage());
        }
    }

    public static void changeStatusByOrderId(String orderStatus, int order_id, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "UPDATE ordrene SET order_status = ? WHERE order_id = ?";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1, orderStatus);
                ps.setInt(2, order_id);
                ps.executeUpdate();
            }
        } catch (SQLException e){
            throw new DatabaseException("Det lykkedes ikke at ændre status på ordren", e.getMessage());
        }
    }


    public static void updateSpecificOrderById(int orderId, double carportWidth, double carportLength, double carportHeight, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "UPDATE ordrene SET carport_width = ?, carport_length = ?, carport_height = ? WHERE order_id = ?";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setDouble(1, carportWidth);
                ps.setDouble(2, carportLength);
                ps.setDouble(3, carportHeight);
                ps.setDouble(4, orderId);
                ps.executeUpdate();
            }
        } catch (SQLException e){
            throw new DatabaseException("Det lykkedes ikke at ændre dimensionerne på carporten", e.getMessage());
        }
    }
}
