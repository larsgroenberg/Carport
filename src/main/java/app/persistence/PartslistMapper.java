package app.persistence;

import app.entities.Part;
import app.entities.Partslistline;
import app.exceptions.DatabaseException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartslistMapper {

    public static void insertPartslistLine(Partslistline partslistline, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "insert into partslist(part_id, order_id, quantity, partslistprice, description, unit, part_length, name) values (?,?,?,?,?,?,?,?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setInt(1, partslistline.getPartId());
            ps.setInt(2, partslistline.getOrderId());
            ps.setInt(3, partslistline.getQuantity());
            ps.setDouble(4, partslistline.getPartlistlineprice());
            ps.setString(5, partslistline.getDescription());
            ps.setString(6, partslistline.getUnit());
            ps.setInt(7, partslistline.getPartLength());
            ps.setString(8, partslistline.getName());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1)
            {
                throw new DatabaseException("Fejl ved indsættelse af partslistlinie i tabellen partslist");
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e.getMessage());
        }
    }

    public static ArrayList<Partslistline> getPartsList(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Partslistline> partslistLines = new ArrayList<>();

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

                Partslistline partslistLine = new Partslistline(partId, orderId, quantity, partslistlineprice, description, unit, partLength, name);
                partslistLines.add(partslistLine);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving topping with id = " + e.getMessage());
        }
        return partslistLines;
    }

    public static ArrayList<Partslistline> getPartsListByOrderid(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Partslistline> partslistLines = new ArrayList<>();

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

                Partslistline partslistLine = new Partslistline(partId, orderId, quantity, partslistlineprice, description, unit, partLength, name);
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

    public static List<Part> getAllParts(ConnectionPool connectionPool) throws DatabaseException {

        List<Part> partList = new ArrayList<>();
        String sql = "SELECT * FROM parts";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ResultSet rs = ps.executeQuery();

                while (rs.next()){
                    int part_id = rs.getInt("part_id");
                    int price = rs.getInt("price");
                    String description = rs.getString("description");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String type = rs.getString("type");
                    String material_name = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    partList.add(new Part(part_id, price, description, length, height, width, type, material_name, unit, name));
                }
            }
        }catch (SQLException e){
            throw new DatabaseException("We couldn't get the material", e.getMessage());
        }
        return partList;
    }

    public static Part getPartByType(String type, ConnectionPool connectionPool) throws DatabaseException {

        Part part = null;

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
                    part = new Part(part_id, price, description, length, height, width, type, material_name, unit, name);
                }

            }
        }catch (SQLException e){
            throw new DatabaseException( "We couldnt get the material", e.getMessage());
        }
        return part;
    }

    public static Part getPartByTypeAndLength(String type, double carportWidth, ConnectionPool connectionPool) throws DatabaseException {

        Part part = null;

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
                    part = new Part(part_id, price, description, length, height, width, type, material_name, unit, name);
                }
            }
        }catch (SQLException e){
            throw new DatabaseException( "We couldnt get the material with that length", e.getMessage());
        }
        return part;
    }

    public static List<Part> gePartsByDescription(String description, ConnectionPool connectionPool) throws DatabaseException {

        List<Part> partList = new ArrayList<>();
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
                    partList.add(new Part(part_id, price, description, length, height, width, type, material_name, unit, name));
                }
            }
        }catch (SQLException e){
            throw new DatabaseException("We couldnt get the material", e.getMessage());
        }
        return partList;
    }


    public static Part getPartById(int partId, ConnectionPool connectionPool) throws DatabaseException {

        Part part = null;
        String sql = "SELECT * FROM parts WHERE part_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, partId);
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
                String name = rs.getString("name");
                part = new Part(part_id, price, description, length, height, width, type, material_name, unit, name);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving material with id = " + partId, e.getMessage());
        }
        return part;
    }

    public static Part getPartByName(String name, ConnectionPool connectionPool) throws DatabaseException {

        Part part = null;
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
                part = new Part(part_id, price, description, length, height, width, type, material_name, unit, name);
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

}
