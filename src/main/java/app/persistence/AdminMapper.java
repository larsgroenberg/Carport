package app.persistence;

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
                int price = rs.getInt("price");
                String description = rs.getString("description");
                int length = rs.getInt("length");
                int height = rs.getInt("height");
                int width = rs.getInt("width");
                String type = rs.getString("type");
                String material = rs.getString("material");
                String unit = rs.getString("unit");
                materialList.add(new Material(material_id, price, description, length, height, width, type, material, unit));
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
                int material_id = rs.getInt("material_id");
                int price = rs.getInt("price");
                String description = rs.getString("description");
                int length = rs.getInt("length");
                int height = rs.getInt("height");
                int width = rs.getInt("width");
                String type = rs.getString("type");
                String material_name = rs.getString("material");
                String unit = rs.getString("unit");
                material = new Material(material_id, price, description, length, height, width, type, material_name, unit);
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
                int material_id = rs.getInt("material_id");
                int price = rs.getInt("price");
                String description = rs.getString("description");
                int length = rs.getInt("length");
                int height = rs.getInt("height");
                int width = rs.getInt("width");
                String type = rs.getString("type");
                String material_name = rs.getString("material");
                String unit = rs.getString("unit");
                material = new Material(material_id, price, description, length, height, width, type, material_name, unit);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving material with name = " + name, e.getMessage());
        }
        return material;
    }


    public static void insertMaterial(String name, int price, String description, int length, int height, int width, String type, String material, String unit, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO orders (price, description, length, height, width, type, material, unit) VALUES (?,?,?,?,?,?,?)";

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
            ps.setString(8, material);
            ps.setString(9, unit);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("Fejl ved indsættelse af et nyt styk materiale");
            }
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
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

    public static ArrayList<Order> showAllOrders(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Order> orderList = new ArrayList<>();
        String query = "SELECT * FROM public.ordrene";
        try
                (
                        Connection connection = connectionPool.getConnection();
                        PreparedStatement ps = connection.prepareStatement(query)
                ) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                int orderId = rs.getInt("order_id");
                int carportLength = rs.getInt("carport_length");
                int carportWidth = rs.getInt("carport_width");
                int carportHeight = rs.getInt("carport_height");
                int materialCost = rs.getInt("material_cost");
                String orderStatus = rs.getString("order_status");
                int shedWidth = rs.getInt("shed_width");
                int shedLength = rs.getInt("shed_length");
                int salesPrice = rs.getInt("sales_price");
                String email = rs.getString("email");
                String orderDate = rs.getString("orderdate");

                Order newOrder = new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, orderStatus, shedWidth, shedLength, email, orderDate);
                orderList.add(newOrder);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return orderList;
    }

    public static User getCustomerByName(String userName, ConnectionPool connectionPool) throws DatabaseException {

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
                String password = rs.getString("password");
                boolean is_admin = rs.getBoolean("is_admin");
                String userMobile = rs.getString("mobile");
                String address = rs.getString("address");
                String zipcode = rs.getString("zipcode");
                return new User(user_ID, email, password, is_admin, userName, userMobile, address, zipcode);
            } else {
                throw new DatabaseException("Fejl i hentning af kundeinfo!");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }

    public static User getCustomerByEmail(String email, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "select * from public.users where email=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int user_ID = rs.getInt("user_id");
                String userName = rs.getString("name");
                String password = rs.getString("password");
                boolean is_admin = rs.getBoolean("is_admin");
                String userMobile = rs.getString("mobile");
                String address = rs.getString("address");
                String zipcode = rs.getString("zipcode");
                return new User(user_ID, email, password, is_admin, userName, userMobile, address, zipcode);
            } else {
                throw new DatabaseException("Fejl i hentning af kundeinfo!");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }
}