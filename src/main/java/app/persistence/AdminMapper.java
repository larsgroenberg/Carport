package app.persistence;

//import app.entities.User;
import app.entities.Material;
import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminMapper {

    public static ArrayList<Material> showMaterials(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Material> materialList = new ArrayList<>();
        String sql = "select * from materials";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                int material_id = rs.getInt("material_id");
                String name = rs.getString("name");
                int price = rs.getInt("price");
                String description = rs.getString("description");
                int length = rs.getInt("length");
                int height = rs.getInt("height");
                int width = rs.getInt("width");
                String type = rs.getString("type");
                materialList.add(new Material(material_id, name, price, description, length, height, width, type));
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl!!!!", e.getMessage());
        }
        return materialList;
    }

    public static Material getMaterialById(int materialId, ConnectionPool connectionPool) throws DatabaseException {
        Material material = null;

        String sql = "SELECT * FROM materials WHERE material_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, materialId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("price");
                String description = rs.getString("description");
                int length = rs.getInt("length");
                int height = rs.getInt("height");
                int width = rs.getInt("width");
                String type = rs.getString("type");
                material = new Material(materialId, name, price, description, length, height, width, type);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving material with id = " + materialId, e.getMessage());
        }
        return material;
    }

    public static Material getMaterialByName(String name, ConnectionPool connectionPool) throws DatabaseException {
        Material material = null;

        String sql = "SELECT * FROM materials WHERE name = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int materialId = rs.getInt("material_id");
                int price = rs.getInt("price");
                String description = rs.getString("description");
                int length = rs.getInt("length");
                int height = rs.getInt("height");
                int width = rs.getInt("width");
                String type = rs.getString("type");
                material = new Material(materialId, name, price, description, length, height, width, type);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving material with name = " + name, e.getMessage());
        }
        return material;
    }


    public static void insertMaterial(String name, int price, String description, int length, int height, int width, String type, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO orders (name, price, description, length, height, width, type) VALUES (?,?,?,?,?,?,?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setString(1, name);
            ps.setInt(2, price);
            ps.setString(3, description);
            ps.setInt(4, length);
            ps.setInt(5, height);
            ps.setInt(6, width);
            ps.setString(7, type);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("Fejl ved indsættelse af et nyt styk materiale");
            }
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }


    public static ArrayList<User> showCustomer(String userName,ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<User> customerInfo = new ArrayList<>();
        String sql = "select * from public.users where name=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, userName);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int user_ID = rs.getInt("user_id");
                String email = rs.getString("email");
                String user_Name = rs.getString("name");
                String user_Mobile = rs.getString("mobile");
                int user_Balance = rs.getInt("balance");
                User newUser = new User(user_ID,email,"",false,user_Name,user_Mobile,user_Balance);
                customerInfo.add(newUser);
            } else {
                //throw new DatabaseException("Fejl i login. Prøv igen");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
        return customerInfo;
    }

    public static ArrayList<Order> showCustomerOrders(String userName, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Order> orderList = new ArrayList<>();
        String query = "SELECT u.user_id, u.email, u.name, u.mobile, u.balance, t.topping, b.bottom, ol.quantity, ol.price AS orderline_price " +
                "FROM public.orders o " +
                "JOIN public.users u ON o.user_id = u.user_id " +
                "JOIN public.orderline ol ON o.order_id = ol.order_id " +
                "JOIN public.topping t ON ol.topping_id = t.topping_id " +
                "JOIN public.bottom b ON ol.bottom_id = b.bottom_id " +
                "WHERE u.name = ?;";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1,userName);
            ResultSet resultSet = ps.executeQuery();
            // Process the result set
            while (resultSet.next()) {
                // Retrieve data from each row
                int userId = resultSet.getInt("user_id");
                String email = resultSet.getString("email");
                String name = resultSet.getString("name");
                String mobile = resultSet.getString("mobile");
                int balance = resultSet.getInt("balance");
                String topping = resultSet.getString("topping");
                String bottom = resultSet.getString("bottom");
                int quantity = resultSet.getInt("quantity");
                int orderlinePrice = resultSet.getInt("orderline_price");

                Order newOrder = new Order(userId,email, name, mobile, balance, topping, bottom, quantity, orderlinePrice);

                orderList.add(newOrder);
            }
            return orderList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static ArrayList<Order> showAllOrders(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Order> orderList = new ArrayList<>();
        String query = "SELECT u.user_id,ol.order_id, u.email, u.name, u.mobile, u.balance, t.topping, b.bottom, ol.quantity, ol.price AS orderline_price " +
                "FROM public.orders o " +
                "JOIN public.users u ON o.user_id = u.user_id " +
                "JOIN public.orderline ol ON o.order_id = ol.order_id " +
                "JOIN public.topping t ON ol.topping_id = t.topping_id " +
                "JOIN public.bottom b ON ol.bottom_id = b.bottom_id";
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ResultSet resultSet = ps.executeQuery();
            // Process the result set
            while (resultSet.next()) {
                // Retrieve data from each row
                int userId = resultSet.getInt("user_id");
                int orderId = resultSet.getInt("order_id");
                String email = resultSet.getString("email");
                String name = resultSet.getString("name");
                String mobile = resultSet.getString("mobile");
                int balance = resultSet.getInt("balance");
                String topping = resultSet.getString("topping");
                String bottom = resultSet.getString("bottom");
                int quantity = resultSet.getInt("quantity");
                int orderlinePrice = resultSet.getInt("orderline_price");

                Order newOrder = new Order(userId,/*orderId,*/ email, name, mobile, balance, topping, bottom, quantity, orderlinePrice);

                orderList.add(newOrder);
            }
            return orderList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean addMoneyToCustomerBalance(String email, String amountToAdd, ConnectionPool connectionPool) throws DatabaseException
    {
        String sql = "UPDATE public.users " +
                "SET balance = balance + ? "+
                "WHERE email = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setInt(1, Integer.parseInt(amountToAdd));
            ps.setString(2, email);

            int rowsUpdated = ps.executeUpdate(); // Execute the update operation
            return rowsUpdated > 0;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }
}