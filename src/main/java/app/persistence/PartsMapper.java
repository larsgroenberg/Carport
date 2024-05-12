package app.persistence;

import app.entities.Carport;
import app.entities.CarportPart;
import app.entities.Part;
import app.exceptions.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PartsMapper {

    static Part getPartByPartId(int partId, ConnectionPool connectionPool) throws DatabaseException {

        Part part = null;

        String sql = "SELECT * FROM materials WHERE part_id = ?";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setInt(1, partId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()){
                    int price = rs.getInt("price");
                    String description = rs.getString("description");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String type = rs.getString("type");
                    String material_name = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    part = new Part(partId, price, description, length, height, width, type, material_name, unit, name);
                }

            }
        }catch (SQLException e){
            throw new DatabaseException("We couldnt get the material", e.getMessage());
        }
        return part;
    }

    static Part getPartByType(String type, ConnectionPool connectionPool) throws DatabaseException {

        Part part = null;

        String sql = "SELECT * FROM parts WHERE type = ?";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1, type);
                ResultSet rs = ps.executeQuery();

                if (rs.next()){
                    int partId = rs.getInt("part_id");
                    int price = rs.getInt("price");
                    String description = rs.getString("description");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String material_name = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    part = new Part(partId, price, description, length, height, width, type, material_name, unit, name);
                }
            }
        }catch (SQLException e){
            throw new DatabaseException( "We couldnt get the material", e.getMessage());
        }
        return part;
    }

    static List<Part> getPartByDescription(String description, ConnectionPool connectionPool) throws DatabaseException {

        List<Part> partList = new ArrayList<>();
        String sql = "SELECT * FROM parts WHERE description = ?";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1, description);
                ResultSet rs = ps.executeQuery();

                while (rs.next()){
                    int partId = rs.getInt("part_id");
                    int price = rs.getInt("price");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String type = rs.getString("type");
                    String material_name = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    partList.add(new Part(partId, price, description, length, height, width, type, material_name, unit, name));
                }
            }
        }catch (SQLException e){
            throw new DatabaseException("We couldnt get the material", e.getMessage());
        }
        return partList;
    }

    static int addPart(double price, String description, int length, int height, int width, String type, String material, String unit, String name, ConnectionPool connectionPool) throws DatabaseException{

        String sql = "INSERT INTO parts (price, description, length, height, width, type, material, unit) VALUES (?,?,?,?,?,?,?.?)";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)){
                ps.setDouble(1, price);
                ps.setString(2, description);
                ps.setInt(3, length);
                ps.setInt(4, height);
                ps.setInt(5, width);
                ps.setString(6, type);
                ps.setString(7, material);
                ps.setString(8, unit);
                ps.setString(8, name);

                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                return rs.getInt(1);
            } catch (SQLException e){
                e.printStackTrace();
            }
        } catch (SQLException e){
            throw new DatabaseException("Something went wrong when trying to add this material", e.getMessage());
        }
        return 0;
    }

    static List<Part> getAllParts(ConnectionPool connectionPool) throws DatabaseException {

        List<Part> partList = new ArrayList<>();
        String sql = "SELECT * FROM parts";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ResultSet rs = ps.executeQuery();

                while (rs.next()){
                    int partId = rs.getInt("part_id");
                    int price = rs.getInt("price");
                    String description = rs.getString("description");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String type = rs.getString("type");
                    String material_name = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    partList.add(new Part(partId, price, description, length, height, width, type, material_name, unit, name));
                }
            }
        }catch (SQLException e){
            throw new DatabaseException("We couldn't get the part", e.getMessage());
        }
        return partList;
    }

    static void adjustCostPrice(int partId, double newCostPrice, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "UPDATE parts SET price = ? WHERE part_id = ?";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setDouble(1,newCostPrice);
                ps.setInt(2, partId);
                ps.executeUpdate();
            }
        }catch (SQLException e){
            throw new DatabaseException("We couldn't update the parts costprice", e.getMessage());
        }
    }


    public static ArrayList<Part> showPartsList(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Part> partList = new ArrayList<>();
        String sql = "select * from parts";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                int partId = rs.getInt("part_id");
                int price = rs.getInt("price");
                String description = rs.getString("description");
                int length = rs.getInt("length");
                int height = rs.getInt("height");
                int width = rs.getInt("width");
                String type = rs.getString("type");
                String material = rs.getString("material");
                String unit = rs.getString("unit");
                String name = rs.getString("name");
                partList.add(new Part(partId, price, description, length, height, width, type, material, unit, name));
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl!!!!", e.getMessage());
        }
        return partList;
    }

    public static Part getPartById(int materialId, ConnectionPool connectionPool) throws DatabaseException {

        Part part = null;
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
                String name = rs.getString("name");
                part = new Part(material_id, price, description, length, height, width, type, material_name, unit, name);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving material with id = " + materialId, e.getMessage());
        }
        return part;
    }


    public static void insertPart(String name, int price, String description, int length, int height, int width, String type, String material, String unit, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO orders (price, description, length, height, width, type, material, unit) VALUES (?,?,?,?,?,?,?,?)";

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

    public static CarportPart getBeamDetails(int carportLength, ConnectionPool connectionPool) {
        String sql = "SELECT price, length, height, width, description, material, unit, name FROM parts WHERE type = 'spær' AND (length >= ? OR length < ?) ORDER BY length >= ? DESC, ABS(? - length) LIMIT 1";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, carportLength);
            ps.setInt(2, carportLength);
            ps.setInt(3, carportLength);
            ps.setInt(4, carportLength);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double price = rs.getDouble("price");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String description = rs.getString("description");
                    String material = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    return new CarportPart(CarportPart.CarportPartType.BEAM, 1, price, length, height, width, description, material, unit, name);
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
        String sql = "SELECT price, length, height, width, description, material, unit, name FROM parts WHERE type = 'stolpe' AND (length >= ? OR length < ?) ORDER BY length >= ? DESC, ABS(? - length) LIMIT 1";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, carportHeight);
            ps.setInt(2, carportHeight);
            ps.setInt(3, carportHeight);
            ps.setInt(4, carportHeight);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double price = rs.getDouble("price");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String description = rs.getString("description");
                    String material = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    return new CarportPart(CarportPart.CarportPartType.SUPPORTPOST, 1, price, length, height, width, description, material, unit, name);
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
        String sql = "SELECT price, length, height, width, description, material, unit, name FROM parts WHERE type = 'spær' AND (length >= ? OR length < ?) ORDER BY length >= ? DESC, ABS(? - length) LIMIT 1";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, carportWidth);
            ps.setInt(2, carportWidth);
            ps.setInt(3, carportWidth);
            ps.setInt(4, carportWidth);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double price = rs.getDouble("price");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String description = rs.getString("description");
                    String material = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    return new CarportPart(CarportPart.CarportPartType.BEAM, 1, price, length, height, width, description, material, unit, name);
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
}
