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

public class OrdersMapper {
    static java.util.Date today = new Date();
    static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
    static String formattedDate = formatter.format(today);

    public static int createOrder(int userID, Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO ordrene(material_cost, sales_price, carport_width, carport_length, carport_height, user_id, order_status, shed_width, shed_length, email, orderdate, roof, wall) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                // Her opretter jeg forbindelse til databasen
                Connection connection = connectionPool.getConnection();
                // Her opretter jeg et PreparedStatement til at udføre SQL-forespørgslen
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            Carport carport = ctx.sessionAttribute("newCarport");
            User user = ctx.sessionAttribute("currentUser");

            // Sæt parametre for PreparedStatement
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
            ps.setString(11, formattedDate);  // Sørg for at formattedDate er korrekt defineret
            ps.setBoolean(12, carport.isWithRoof());
            ps.setBoolean(13, carport.isWithShed());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("Fejl ved oprettelse af ordren i tabellen ordrene");
            }

            // Her henter jeg den genererede nøgleværdi
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new DatabaseException("Kunne ikke hente den genererede ordre ID");
                }
            }
        } catch (SQLException e) {
            // Her kaster vi en DatabaseException hvis der opstår en SQL-relateret fejl
            throw new DatabaseException("Det lykkedes ikke at oprette ordren", e);
        }
    }


    public static void insertPartsNeededForOrder(int orderID, Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<CarportPart> carportPartList = ctx.sessionAttribute("partsList");
        String sql = "INSERT INTO partslist(part_id, order_id, quantity, partslistprice, description, unit, part_length, name) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                // Her opretter jeg forbindelse til databasen
                Connection connection = connectionPool.getConnection();
                // Her opretter jeg et PreparedStatement til at udføre SQL-forespørgslen
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            for (CarportPart part : carportPartList) {
                // Her sætter jeg parametrene for PreparedStatementet
                ps.setInt(1, part.getPartId());
                ps.setInt(2, orderID);
                ps.setInt(3, part.getQuantity());
                ps.setDouble(4, part.getDBprice());
                ps.setString(5, part.getDBdescription());
                ps.setString(6, part.getDBunit());
                ps.setInt(7, part.getDBlength());
                ps.setString(8, part.getDBname());

                // Her udfører jeg opdateringen
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected != 1) {
                    throw new DatabaseException("Fejl ved indsættelse af partslistlinie i tabellen partslist");
                }
            }
        } catch (SQLException e) {
            // Her kaster vi en DatabaseException hvis der opstår en SQL-relateret fejl
            throw new DatabaseException("Det lykkedes ikke at indsætte dele i partslist-tabellen", e);
        }
    }

    public static Order getOrderByEmail(String email, ConnectionPool connectionPool) throws DatabaseException {
        // SQL-forespørgslen for at hente ordre baseret på e-mail
        String sql = "select * from public.ordrene where email = ?";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            // Her indstiller jeg emailparameteren i PreparedStatement
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Her henter jeg data fra ResultSet'tet og opretter et nyt Order-objekt
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
                    // Returner en ny Order baseret på de hentede data
                    return new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, orderStatus, shedWidth, shedLength, email, orderDate, roof, wall);
                } else {
                    // Hvis der ikke blev fundet nogen ordre, kastes en DatabaseException
                    throw new DatabaseException("Fejl i forsøg på at hente ordren");
                }
            } catch (SQLException e) {
                // Hvis der opstår en SQL-fejl under udførelsen af query'en, kastes en DatabaseException med fejlmeddelelsen
                throw new DatabaseException("Databasefejl ", e.getMessage());
            }
        } catch (SQLException e) {
            // Hvis der opstår en SQL-fejl under oprettelsen af forbindelsen, kastes en RuntimeException med fejlmeddelelsen
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<Order> getAllOrders(ConnectionPool connectionPool) throws DatabaseException {
        // Opret en liste til at gemme de hentede ordrer
        ArrayList<Order> orderList = new ArrayList<>();

        // SQL-forespørgslen for at hente alle ordrer og tilhørende brugeroplysninger
        String sql = "SELECT o.order_id, o.material_cost, o.sales_price, o.carport_width, o.carport_length, o.carport_height, " +
                "o.user_id, o.order_status, o.shed_width, o.shed_length, o.email, o.orderdate, o.roof, o.wall, " +
                "u.mobile " +
                "FROM public.ordrene o " +
                "JOIN public.users u ON o.user_id = u.user_id";

        try (
                // Her opretter jeg forbindelse til databasen
                Connection connection = connectionPool.getConnection();
                // Opret et PreparedStatement til at udføre SQL-forespørgslen
                PreparedStatement ps = connection.prepareStatement(sql);
                // Udfør forespørgslen og gem resultatet i et ResultSet
                ResultSet rs = ps.executeQuery()
        ) {
            // Løber hele ResultSet'et igennem
            while (rs.next()) {
                // Her henter jeg data fra hvert resultat
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

                // Her opretter jeg et ny Order-objekt baseret på de hentede data og tilføjer den til orderList
                Order order = new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, orderStatus, shedWidth, shedLength, email, orderDate, roof, wall, mobile);
                orderList.add(order);
            }
        } catch (SQLException e) {
            // Hvis der opstår en SQL-fejl under udførelsen af query'en, kastes der en DatabaseException med fejlmeddelelsen
            throw new DatabaseException("Vi kunne ikke hente ordrelisten fra databasen ", e.getMessage());
        }
        // Her returneres orderList med alle ordrene i tabellen
        return orderList;
    }

    public static Order getOrderByOrderId(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        Order order = null;
        String sql = "SELECT * FROM ordrene WHERE order_id = ?";

        try (
                // Her opretter jeg forbindelse til databasen
                Connection connection = connectionPool.getConnection();
                // Her opretter jeg et PreparedStatement til at udføre SQL-forespørgslen
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            // Sæt parameteren for PreparedStatement
            ps.setInt(1, orderId);

            // Udfør forespørgslen og gem resultatet i et ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Her henter jeg data fra ResultSet'et
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

                    // Her opretter jeg et Order-objekt med de fundne data
                    order = new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, orderStatus, shedWidth, shedLength, email, orderDate, roof, wall);
                } else {
                    // Kast DatabaseException hvis der ikke findes nogen ordre med det givne ordreID
                    throw new DatabaseException("Der findes ikke ordre med det ordreId i databasen");
                }
            }
        } catch (SQLException e) {
            // Her kaster vi en DatabaseException hvis der opstår en SQL-relateret fejl
            throw new DatabaseException("Det lykkedes ikke at hente brugerens ordre ved at søge på ordreid", e.getMessage());
        }
        return order;
    }

    // Vi går ikke udfra at en kunde har mere end en ordre i systemet
    public static Order getOrderByUserId(int userId, ConnectionPool connectionPool) throws DatabaseException {
        Order order = null;
        String sql = "SELECT * FROM ordrene WHERE user_id = ?";

        try (
                // Her opretter jeg forbindelse til databasen
                Connection connection = connectionPool.getConnection();
                // Her opretter jeg et PreparedStatement til at udføre SQL-forespørgslen
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            // Sæt parameteren for PreparedStatement
            ps.setInt(1, userId);

            // Udfør forespørgslen og gem resultatet i et ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Her henter jeg data fra ResultSet'et
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

                    // Hej opretter jeg et Order-objekt
                    order = new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, orderStatus, shedWidth, shedLength, email, orderDate, roof, wall);
                } else {
                    // Her kaster jeg en DatabaseException hvis der ikke findes nogen ordre med det givne userId
                    throw new DatabaseException("Der findes ikke en ordre med det userId i databasen");
                }
            }
        } catch (SQLException e) {
            // Her kaster jeg en DatabaseException hvis der opstår en SQL-relateret fejl
            throw new DatabaseException("Det lykkedes ikke at hente brugerens ordre ved at søge på userId", e);
        }
        return order;
    }


    public static void deleteOrderByOrderId(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "DELETE FROM ordrene WHERE order_id = ?";
        try (
                // Her opretter jeg forbindelse til databasen
                Connection connection = connectionPool.getConnection();
                // Her opretter jeg et PreparedStatement til at udføre SQL-forespørgslen
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            // Her sættes parameteren for PreparedStatement
            ps.setInt(1, orderId);
            int rowsAffected = ps.executeUpdate();

            // Tjek om nogen rækker blev påvirket (dvs. om ordren blev slettet)
            if (rowsAffected == 0) {
                // Hvis ingen rækker blev påvirket, betyder det, at ordren ikke blev fundet
                System.out.println("Ordren kunne ikke findes i databasen ");
            }
        } catch (SQLException e) {
            // Her kaster vi en DatabaseException hvis der opstår en SQL-relateret fejl
            throw new DatabaseException("Fejl ved sletning af ordre: " + e.getMessage(), e);
        }
    }


    public static void updateOrder(Order order, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE ordrene SET material_cost = ?, sales_price = ?, carport_width = ?, carport_length = ?, carport_height = ?, user_id = ?, order_status = ?, shed_width = ?, shed_length = ?, email = ?, orderdate = ?, roof = ?, wall = ? WHERE order_id = ?;";

        try (
                // Her opretter jeg forbindelse til databasen
                Connection connection = connectionPool.getConnection();
                // Her opretter jeg et PreparedStatement til at udføre SQL-forespørgslen
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            // Sæt parametre for PreparedStatement
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

            // Udfør opdateringen
            ps.executeUpdate();
        } catch (SQLException e) {
            // Her kaster vi en DatabaseException hvis der opstår en SQL-relateret fejl
            throw new DatabaseException("Det lykkedes ikke at opdatere ordren ", e.getMessage());
        }
    }


    public static void changeStatusOnOrder(String orderStatus, int orderId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE ordrene SET order_status = ? WHERE order_id = ?";

        try (
                // Her opretter jeg forbindelse til databasen
                Connection connection = connectionPool.getConnection();
                // Her opretter jeg et PreparedStatement til at udføre SQL-forespørgslen
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            // Her sætter jeg parametre for PreparedStatement
            ps.setString(1, orderStatus);
            ps.setInt(2, orderId);

            // Her udfører jeg opdateringen
            ps.executeUpdate();
        } catch (SQLException e) {
            // Her kaster vi en DatabaseException hvis der opstår en SQL-relateret fejl
            throw new DatabaseException("Det lykkedes ikke at ændre status på ordren", e);
        }
    }

}
