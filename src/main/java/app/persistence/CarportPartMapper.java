package app.persistence;

import app.entities.CarportPart;
import app.exceptions.DatabaseException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarportPartMapper {

    //todo: tjek efter lignende functioner og erstat eller andet
    public static ArrayList<CarportPart> getDBParts(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<CarportPart> partList = new ArrayList<>();
        String sql = "SELECT * FROM parts";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ResultSet rs = ps.executeQuery();

                while (rs.next()){
                    int partId = rs.getInt("part_id");
                    Double price = rs.getDouble("price");
                    String description = rs.getString("description");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String type = rs.getString("type");
                    String material_name = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    CarportPart.CarportPartType partType = null;
                    switch (type) {
                        case "stolpe" -> partType = CarportPart.CarportPartType.SUPPORTPOST;
                        case "spær" -> partType = CarportPart.CarportPartType.RAFT;
                        case "remme" -> partType = CarportPart.CarportPartType.BEAM;
                        case "hulbånd" -> partType = CarportPart.CarportPartType.CROSSSUPPORT;
                        case "tagplader" -> partType = CarportPart.CarportPartType.ROOFTILE;
                        case "brædder" -> partType = CarportPart.CarportPartType.STERN;
                        case "reglar" -> partType = CarportPart.CarportPartType.SHEDWOOD;
                        default -> partType = CarportPart.CarportPartType.NONE;
                    }
                    partList.add(new CarportPart(partType,0,partId, price, length, height, width, description, material_name, unit, name));
                }
            }
        }catch (SQLException e){
            throw new DatabaseException("We couldn't get the part", e.getMessage());
        }
        return partList;
    }

    public static ArrayList<CarportPart> getPartsList(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<CarportPart> partslistLines = new ArrayList<>();

        String sql = "SELECT * FROM partslist WHERE order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int partId = rs.getInt("part_id");
                int quantity = rs.getInt("quantity");
                int partslistlineprice = rs.getInt("partslistprice");
                String description = rs.getString("description");
                String unit = rs.getString("unit");
                int partLength = rs.getInt("part_length");
                String name = rs.getString("name");

                CarportPart partslistLine = new CarportPart(null, quantity,partId, partslistlineprice, partLength,0,0, description,name, unit, name);
                partslistLines.add(partslistLine);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving topping with id = " + e.getMessage());
        }
        return partslistLines;
    }

    public static ArrayList<CarportPart> getPartsListByOrderid(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<CarportPart> partslistLines = new ArrayList<>();

        String sql = "SELECT * FROM partslist WHERE order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int partId = rs.getInt("part_id");
                int quantity = rs.getInt("quantity");
                int partslistlineprice = rs.getInt("partslistprice");
                String description = rs.getString("description");
                String unit = rs.getString("unit");
                int partLength = rs.getInt("part_length");
                String name = rs.getString("name");

                CarportPart partslistLine = new CarportPart(null, quantity,partId, partslistlineprice, partLength,0,0, description,name, unit, name);
                partslistLines.add(partslistLine);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving topping with id = " + e.getMessage());
        }
        return partslistLines;
    }


    public static void adjustPartsCostPrice(int partId, double newCostPrice, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "UPDATE parts SET price = (?) WHERE part_id = ?";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setDouble(1,newCostPrice);
                ps.setInt(2, partId);
                ps.executeUpdate();
            }
        }catch (SQLException e){
            throw new DatabaseException("We couldn't update the meterial costprice", e.getMessage());
        }
    }

    public static CarportPart getPartByType(String type, ConnectionPool connectionPool) throws DatabaseException {

        CarportPart part = null;

        String sql = "SELECT * FROM parts WHERE type = ?";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1,type);
                ResultSet rs = ps.executeQuery();

                if (rs.next()){
                    int part_id = rs.getInt("part_id");
                    int price = rs.getInt("price");
                    String description = rs.getString("description");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String material_name = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    part = new CarportPart(null, 0,part_id, price, length,height,width, description,material_name, unit, name);
                }

            }
        }catch (SQLException e){
            throw new DatabaseException( "We couldnt get the material", e.getMessage());
        }
        return part;
    }

    public static CarportPart getPartByTypeAndLength(String type, double carportWidth, ConnectionPool connectionPool) throws DatabaseException {

        CarportPart part = null;

        String sql = "SELECT * FROM parts WHERE type = ? AND length = ?";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1,type);
                ps.setDouble(2, carportWidth);
                ResultSet rs = ps.executeQuery();

                if (rs.next()){
                    int part_id = rs.getInt("part_id");
                    int price = rs.getInt("price");
                    String description = rs.getString("description");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String material_name = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    part = new CarportPart(null, 0,part_id, price, length,height,width, description,material_name, unit, name);
                }
            }
        }catch (SQLException e){
            throw new DatabaseException( "We couldnt get the material with that length", e.getMessage());
        }
        return part;
    }

    public static ArrayList<CarportPart> gePartsByDescription(String description, ConnectionPool connectionPool) throws DatabaseException {

        ArrayList<CarportPart> partList = new ArrayList<>();
        String sql = "SELECT * FROM parts WHERE description = ?";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1,description);
                ResultSet rs = ps.executeQuery();

                while (rs.next()){
                    int part_id = rs.getInt("part_id");
                    int price = rs.getInt("price");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String type = rs.getString("type");
                    String material_name = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    partList.add(new CarportPart(null, 0,part_id, price, length,height,width, description,material_name, unit, name));
                }
            }
        }catch (SQLException e){
            throw new DatabaseException("We couldnt get the material", e.getMessage());
        }
        return partList;
    }


    public static CarportPart getPartById(int partId, ConnectionPool connectionPool) throws DatabaseException {

        CarportPart part = null;
        String sql = "SELECT * FROM parts WHERE part_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, partId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int part_id = rs.getInt("part_id");
                double price = rs.getDouble("price");
                String description = rs.getString("description");
                int length = rs.getInt("length");
                int height = rs.getInt("height");
                int width = rs.getInt("width");
                String type = rs.getString("type");
                String material_name = rs.getString("material");
                String unit = rs.getString("unit");
                String name = rs.getString("name");
                CarportPart.CarportPartType partType = null;
                switch (type) {
                    case "stolpe" -> partType = CarportPart.CarportPartType.SUPPORTPOST;
                    case "spær" -> partType = CarportPart.CarportPartType.RAFT;
                    case "brædder" -> partType = CarportPart.CarportPartType.BEAM;
                    case "hulbånd" -> partType = CarportPart.CarportPartType.CROSSSUPPORT;
                    default -> partType = CarportPart.CarportPartType.ROOFTILE;
                }
                part = new CarportPart(partType, 0,part_id, price, length,height,width, description,material_name, unit, name);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving material with id = " + partId, e.getMessage());
        }
        return part;
    }

    public static CarportPart getPartByName(String name, ConnectionPool connectionPool) throws DatabaseException {

        CarportPart part = null;
        String sql = "SELECT * FROM parts WHERE name = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int part_id = rs.getInt("part_id");
                int price = rs.getInt("price");
                String description = rs.getString("description");
                int length = rs.getInt("length");
                int height = rs.getInt("height");
                int width = rs.getInt("width");
                String type = rs.getString("type");
                String material_name = rs.getString("material");
                String unit = rs.getString("unit");
                part = new CarportPart(null, 0,part_id, price, length,height,width, description,material_name, unit, name);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving material with name = " + name, e.getMessage());
        }
        return part;
    }


    public static void addPart(int price, String description, int length, int height, int width, String type, String material, String unit, String name, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO parts (price, description, length, height, width, type, material, unit, name) VALUES (?,?,?,?,?,?,?,?,?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setInt(1, price);
            ps.setString(2, description);
            ps.setInt(3, length);
            ps.setInt(4, height);
            ps.setInt(5, width);
            ps.setString(6, type);
            ps.setString(7, material);
            ps.setString(8, unit);
            ps.setString(9, name);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("Fejl ved indsættelse af et nyt styk materiale");
            }
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public static CarportPart getBeamDetails(int carportLength, ConnectionPool connectionPool) {
        String sql = "SELECT * FROM parts WHERE type = 'remme' AND (length >= ? OR length < ?) ORDER BY length >= ? DESC, ABS(? - length) LIMIT 1";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, carportLength);
            ps.setInt(2, carportLength);
            ps.setInt(3, carportLength);
            ps.setInt(4, carportLength);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int partID = rs.getInt("part_id");
                    double price = rs.getDouble("price");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String description = rs.getString("description");
                    String material = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    return new CarportPart(CarportPart.CarportPartType.BEAM, 0,partID, price, length, height, width, description, material, unit, name);
                } else {
                    System.out.println("No matching beam found for length " + carportLength);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public static CarportPart getSupportPostDetails(int carportHeight, ConnectionPool connectionPool) {
        String sql = "SELECT * FROM parts WHERE type = 'stolpe' AND (length >= ? OR length < ?) ORDER BY length >= ? DESC, ABS(? - length) LIMIT 1";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, carportHeight);
            ps.setInt(2, carportHeight);
            ps.setInt(3, carportHeight);
            ps.setInt(4, carportHeight);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int partID = rs.getInt("part_id");
                    double price = rs.getDouble("price");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String description = rs.getString("description");
                    String material = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    return new CarportPart(CarportPart.CarportPartType.SUPPORTPOST, 0,partID, price, length, height, width, description, material, unit, name);
                } else {
                    System.out.println("No matching support post found for length " + carportHeight);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public static CarportPart getRaftDetails(int carportWidth, ConnectionPool connectionPool) {
        String sql = "SELECT * FROM parts WHERE type = 'spær' AND (length >= ? OR length < ?) ORDER BY length >= ? DESC, ABS(? - length) LIMIT 1";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, carportWidth);
            ps.setInt(2, carportWidth);
            ps.setInt(3, carportWidth);
            ps.setInt(4, carportWidth);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int partID = rs.getInt("part_id");
                    double price = rs.getDouble("price");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String description = rs.getString("description");
                    String material = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    return new CarportPart(CarportPart.CarportPartType.RAFT, 0,partID, price, length, height, width, description, material, unit, name);
                } else {
                    System.out.println("No matching raft found for width " + carportWidth);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }
    public static List<CarportPart> getCompletePartsListByOrderId(int orderId, ConnectionPool pool) throws DatabaseException {
        List<CarportPart> partsList = new ArrayList<>();
        String sql = "SELECT p.part_id, p.type, pl.quantity, p.price as DBprice, p.length as DBlength, p.height as DBheight, p.width as DBwidth, " +
                "p.description as DBdescription, p.material as DBmaterial, p.unit as DBunit, p.name as DBname " +
                "FROM parts p JOIN partslist pl ON p.part_id = pl.part_id WHERE pl.order_id = ?";
        try (Connection conn = pool.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                List<CarportPart.CarportPartType> types = mapToCarportPartType(rs.getString("type")); // This now returns a list of types
                for (CarportPart.CarportPartType type : types) {
                    partsList.add(new CarportPart(
                            rs.getInt("part_id"),
                            type,
                            rs.getInt("quantity"),
                            rs.getDouble("DBprice"),
                            rs.getInt("DBlength"),
                            rs.getInt("DBheight"),
                            rs.getInt("DBwidth"),
                            rs.getString("DBdescription"),
                            rs.getString("DBmaterial"),
                            rs.getString("DBunit"),
                            rs.getString("DBname")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch parts list: " + e.getMessage());
        }
        return partsList;
    }

    private static List<CarportPart.CarportPartType> mapToCarportPartType(String dbType) {
        List<CarportPart.CarportPartType> types = new ArrayList<>();
        switch (dbType.toLowerCase()) {
            case "stolpe":
                types.add(CarportPart.CarportPartType.SUPPORTPOST);
                break;
            case "spær":
                types.add(CarportPart.CarportPartType.RAFT);
                types.add(CarportPart.CarportPartType.BEAM);
                break;
            case "reglar":
                types.add(CarportPart.CarportPartType.BEAM);
                break;
            case "lægte":
            case "brædder":
                types.add(CarportPart.CarportPartType.ROOFTILE);
                break;
            case "hulbånd":  // Adding a case for 'hulbånd'
                types.add(CarportPart.CarportPartType.CROSSSUPPORT);  // Assume 'CROSSSUPPORT' or create a new enum type if needed
                break;
            case "remme":  // Adding a case for 'hulbånd'
                types.add(CarportPart.CarportPartType.BEAM);  // Assume 'CROSSSUPPORT' or create a new enum type if needed
                break;
            default:
                throw new IllegalArgumentException("Unexpected type: " + dbType);
        }
        return types;
    }

    public static void updatePart(CarportPart part, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "UPDATE parts SET price = ?, description = ?, length = ?, height = ?, width = ?, type = ?, material = ?, unit = ?, name = ? WHERE part_id = ?;";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setDouble(1, part.getDBprice());
                ps.setString(2, part.getDBdescription());
                ps.setInt(3, part.getDBlength());
                ps.setInt(4, part.getDBheight());
                ps.setInt(5, part.getDBwidth());

                //todo: skal fikses da denne funktion vil omdøbe ting i databasen.
                ps.setString(6, String.valueOf(part.getType()));

                ps.setString(7, part.getDBmaterial());
                ps.setString(8, part.getDBunit());
                ps.setString(9, part.getDBname());
                ps.setInt(10, part.getPartId());

                ps.executeUpdate();
            }
        }catch (SQLException e){
            throw new DatabaseException("We couldn't update the parts costprice", e.getMessage());
        }
    }
}
