package app.persistence;

import app.entities.Carport;
import app.entities.CarportPart;
import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import io.javalin.http.Context;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrdersMapper {
    static java.util.Date today = new Date();
    static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
    static String formattedDate = formatter.format(today);

    public static int createOrder(int userID, Context ctx, ConnectionPool connectionPool) {
        String sql = "insert into ordrene(material_cost, sales_price, carport_width, carport_length, carport_height, user_id, order_status, shed_width, shed_length, email, orderdate, roof, wall) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ) {
            Carport carport = ctx.sessionAttribute("newCarport");
            User user = ctx.sessionAttribute("currentUser");

            ps.setDouble(1, carport.getPrice());
            ps.setDouble(2, Math.ceil(carport.getPrice() * 1.4));
            ps.setDouble(3, carport.getWidth());
            ps.setDouble(4, carport.getLength());
            ps.setDouble(5, carport.getHeight());
            ps.setInt(6, userID);
            ps.setString(7, "modtaget");
            ps.setDouble(8, carport.getShedWidth());
            ps.setDouble(9, carport.getShedLength());
            ps.setString(10, user.getEmail());
            ps.setString(11, formattedDate);
            ps.setBoolean(12, carport.isWithRoof());
            ps.setBoolean(13, carport.isWithShed());


            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("Fejl ved indsættelse af partslistlinie i tabellen partslist");
            }

            int id = 0;

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
            return id;


        } catch (SQLException | DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertPartsNeededForOrder(int orderID, Context ctx, ConnectionPool connectionPool) throws DatabaseException {

        ArrayList<CarportPart> carportPartList = ctx.sessionAttribute("partsList");

        for (CarportPart part : carportPartList) {

            String sql = "insert into partslist(part_id, order_id, quantity, partslistprice, description, unit, part_length, name) values (?,?,?,?,?,?,?,?)";

            try (
                    Connection connection = connectionPool.getConnection();
                    PreparedStatement ps = connection.prepareStatement(sql)
            ) {
                ps.setInt(1, part.getPartId());
                ps.setInt(2, orderID);
                ps.setInt(3, part.getQuantity());
                ps.setDouble(4, part.getDBprice());
                ps.setString(5, part.getDBdescription());
                ps.setString(6, part.getDBunit());
                ps.setInt(7, part.getDBlength());
                ps.setString(8, part.getDBname());


                int rowsAffected = ps.executeUpdate();
                if (rowsAffected != 1) {
                    throw new DatabaseException("Fejl ved indsættelse af partslistlinie i tabellen partslist");
                }
            } catch (SQLException e) {
                throw new DatabaseException(e.getMessage());
            }
        }
    }

    public static int addOrder(double materialCost, double salesPrice, double carportWidth, double carportLength, double carportHeight, int userId, String orderStatus, double shedWidth, double shedLength, String email, String orderDate, String roof, boolean iswall, ConnectionPool connectionPool) throws DatabaseException{
        String sql = "INSERT INTO ordrene (material_cost, sales_price, carport_width, carport_length, carport_height, user_id, order_status, shed_width, shed_length, email, orderdate, roof, wall) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)){
                ps.setDouble(1, materialCost);
                ps.setDouble(2, salesPrice);
                ps.setDouble(3, carportWidth);
                ps.setDouble(4, carportLength);
                ps.setDouble(5, carportHeight);
                ps.setInt(6, userId);
                ps.setString(7, orderStatus);
                ps.setDouble(8, shedWidth);
                ps.setDouble(9, shedLength);
                ps.setString(10, email);
                ps.setString(11, orderDate);
                ps.setString(12, roof);
                ps.setBoolean(13, iswall);

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
                String orderStatus = rs.getString("order_status");
                int shedWidth = rs.getInt("shed_width");
                int shedLength = rs.getInt("shed_length");
                int salesPrice = rs.getInt("sales_price");
                String orderDate = rs.getString("orderdate");
                String roof = rs.getString("roof");
                boolean wall = rs.getBoolean("wall");
                return new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, orderStatus, shedWidth, shedLength, email, orderDate, roof, wall);
            } else {
                throw new DatabaseException("Fejl i hentning af ordre!");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }

    public static Order getOrderByName(String name, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "select * from public.ordrene where user_id=?";
        try
                (
                        Connection connection = connectionPool.getConnection();
                        PreparedStatement ps = connection.prepareStatement(sql)
                ) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int orderId = rs.getInt("order_id");
                int userId = rs.getInt("user_id");
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
                String roof = rs.getString("roof");
                boolean wall = rs.getBoolean("wall");
                return new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, orderStatus, shedWidth, shedLength, email, orderDate, roof, wall);

            } else {
                throw new DatabaseException("Fejl i hentning af ordre!");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }
    public static ArrayList<Order> getAllOrders(ConnectionPool connectionPool) throws DatabaseException {

        ArrayList<Order> orderList = new ArrayList<>();
        String sql = "SELECT o.order_id, o.material_cost, o.sales_price, o.carport_width, o.carport_length, o.carport_height, " +
                "o.user_id, o.order_status, o.shed_width, o.shed_length, o.email, o.orderdate, o.roof, o.wall, " +
                "u.mobile " +
                "FROM public.ordrene o " +
                "JOIN public.users u ON o.user_id = u.user_id";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
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
                String roof = rs.getString("roof");
                boolean wall = rs.getBoolean("wall");
                String mobile = rs.getString("mobile");

                Order order = new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, orderStatus, shedWidth, shedLength, email, orderDate, roof, wall, mobile);
                orderList.add(order);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Vi kunne ikke hente ordrelisten fra databasen!", e.getMessage());
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
                    String roof = rs.getString("roof");
                    boolean wall = rs.getBoolean("wall");
                    order = new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, orderStatus, shedWidth, shedLength, email, orderDate, roof, wall);
                } else {
                    throw new DatabaseException("Der findes ikke ordre med det ordreId i databasen");
                }
            }
        } catch (SQLException e){
            throw new DatabaseException("Det lykkedes ikke at hente brugerens ordre ved at søge på ordreid", e.getMessage());
        }
        return order;
    }

    // Vi går ikke udfra at en kunde har mere end en ordre i systemet
    public static Order getOrderByUserId(int userId, ConnectionPool connectionPool) throws DatabaseException {

        Order order = null;
        String sql = "SELECT * FROM ordrene WHERE user_id = ?";

        try (Connection connection = connectionPool.getConnection() ){
            try (PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {
                    int orderId = rs.getInt("order_id");
                    double materialCost = rs.getDouble("material_cost");
                    double salesPrice = rs.getDouble("sales_price");
                    double carportWidth = rs.getDouble("carport_width");
                    double carportLength = rs.getDouble("carport_length");
                    double carportHeight = rs.getDouble("carport_height");
                    String orderStatus = rs.getString("order_status");
                    double shedWidth = rs.getDouble("shed_width");
                    double shedLength = rs.getDouble("shed_length");
                    String email = rs.getString("email");
                    String orderDate = rs.getString("orderdate");
                    String roof = rs.getString("roof");
                    boolean wall = rs.getBoolean("wall");
                    order = new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, orderStatus, shedWidth, shedLength, email, orderDate, roof, wall);
                } else {
                    throw new DatabaseException("Der findes ikke en ordre med det userId i databasen");
                }
            }
        } catch (SQLException e){
            throw new DatabaseException("Det lykkedes ikke at hente brugerens ordre ved at søge på userid", e.getMessage());
        }
        return order;
    }

    public static void deleteOrderByOrderId(int orderId, ConnectionPool connectionPool) throws DatabaseException, SQLException {


        String sql = "DELETE FROM ordrene WHERE order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, orderId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("Ordren kunne ikke findes i databasen");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl: " + e.getMessage());
        }
    }

    public static void updateOrder(Order order, ConnectionPool connectionPool) throws DatabaseException{

        String sql = "UPDATE ordrene SET material_cost = ?, sales_price = ?, carport_width = ?, carport_length = ?, carport_height = ?, user_id = ?, order_status = ?, shed_width = ?, shed_length = ?, email = ?, orderdate = ?, roof = ?, wall = ? WHERE order_id = ?;";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setDouble(1, order.getMaterialCost());
                ps.setDouble(2, order.getSalesPrice());
                ps.setDouble(3, order.getCarportWidth());
                ps.setDouble(4, order.getCarportLength());
                ps.setDouble(5, order.getCarportHeight());
                ps.setInt(6, order.getUserId());
                ps.setString(7, order.getOrderStatus());
                ps.setDouble(8, order.getShedWidth());
                ps.setDouble(9, order.getShedLength());
                ps.setString(10, order.getUserEmail());
                ps.setString(11, order.getOrderDate());
                ps.setString(12, order.getRoof());
                ps.setBoolean(13, order.isWall());
                ps.setInt(14, order.getOrderId());

                ps.executeUpdate();
            }
        } catch (SQLException e){
            throw new DatabaseException("Det lykkedes ikke at opdatere ordren", e.getMessage());
        }
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

    public static void adjustMaterialCostPrice(int orderId, double newMaterialCostPrice, ConnectionPool connectionPool) throws DatabaseException{

        String sql = "UPDATE ordrene SET material_cost = ? WHERE order_id = ?";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setDouble(1 ,newMaterialCostPrice);
                ps.setInt(2,orderId);

                ps.executeUpdate();
            }
        } catch (SQLException e){
            throw new DatabaseException("Det lykkedes ikke at justere salgsprisen", e.getMessage());
        }
    }

    public static void changeStatusOnOrder(String orderStatus, int order_id, ConnectionPool connectionPool) throws DatabaseException {

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

    // Denne metode er ikke tilpasset den nye kode
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
