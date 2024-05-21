package app.persistence;

import app.entities.CarportPart;
import app.exceptions.DatabaseException;
import java.sql.*;
import java.util.ArrayList;

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
                        case "stolpe" -> partType = CarportPart.CarportPartType.STOLPE;
                        case "spær" -> partType = CarportPart.CarportPartType.SPÆR;
                        case "remme" -> partType = CarportPart.CarportPartType.REM;
                        case "hulbånd" -> partType = CarportPart.CarportPartType.HULBÅND;
                        case "tagplader" -> partType = CarportPart.CarportPartType.TAGPLADER;
                        case "brædder" -> partType = CarportPart.CarportPartType.BRÆDDER;
                        case "skurbrædt" -> partType = CarportPart.CarportPartType.SKURBRÆDT;
                        case "understern" -> partType = CarportPart.CarportPartType.UNDERSTERN;
                        case "overstern" -> partType = CarportPart.CarportPartType.OVERSTERN;
                        case "vandbrædder" -> partType = CarportPart.CarportPartType.VANDBRÆDDER;
                        case "reglar" -> partType = CarportPart.CarportPartType.REGLAR;
                        case "lægte" -> partType = CarportPart.CarportPartType.LÆGTE;
                        case "universalbeslag" -> partType = CarportPart.CarportPartType.UNIVERSALBESLAG;
                        case "skruer" -> partType = CarportPart.CarportPartType.SKRUER;
                        case "bundskruer" -> partType = CarportPart.CarportPartType.BUNDSKRUER;
                        case "bolte" -> partType = CarportPart.CarportPartType.BOLTE;
                        case "vinkelbeslag" -> partType = CarportPart.CarportPartType.VINKELBESLAG;
                        case "firkantskiver" -> partType = CarportPart.CarportPartType.FIRKANTSKIVER;
                        case "hængsel" -> partType = CarportPart.CarportPartType.HÆNGSEL;
                        default -> partType = CarportPart.CarportPartType.NONE;
                    }
                    partList.add(new CarportPart(partType,0,partId, price, length, height, width, description, material_name, unit, name, type));
                }
            }
        }catch (SQLException e){
            throw new DatabaseException("We couldn't get the part", e.getMessage());
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
                    case "stolpe" -> partType = CarportPart.CarportPartType.STOLPE;
                    case "spær" -> partType = CarportPart.CarportPartType.SPÆR;
                    case "remme" -> partType = CarportPart.CarportPartType.REM;
                    case "hulbånd" -> partType = CarportPart.CarportPartType.HULBÅND;
                    case "tagplader" -> partType = CarportPart.CarportPartType.TAGPLADER;
                    case "brædder" -> partType = CarportPart.CarportPartType.BRÆDDER;
                    case "skurbrædt" -> partType = CarportPart.CarportPartType.SKURBRÆDT;
                    case "understern" -> partType = CarportPart.CarportPartType.UNDERSTERN;
                    case "overstern" -> partType = CarportPart.CarportPartType.OVERSTERN;
                    case "vandbrædder" -> partType = CarportPart.CarportPartType.VANDBRÆDDER;
                    case "reglar" -> partType = CarportPart.CarportPartType.REGLAR;
                    case "lægte" -> partType = CarportPart.CarportPartType.LÆGTE;
                    case "universalbeslag" -> partType = CarportPart.CarportPartType.UNIVERSALBESLAG;
                    case "skruer" -> partType = CarportPart.CarportPartType.SKRUER;
                    case "bundskruer" -> partType = CarportPart.CarportPartType.BUNDSKRUER;
                    case "bolte" -> partType = CarportPart.CarportPartType.BOLTE;
                    case "vinkelbeslag" -> partType = CarportPart.CarportPartType.VINKELBESLAG;
                    case "firkantskiver" -> partType = CarportPart.CarportPartType.FIRKANTSKIVER;
                    case "hængsel" -> partType = CarportPart.CarportPartType.HÆNGSEL;
                    default -> partType = CarportPart.CarportPartType.NONE;
                }
                part = new CarportPart(partType, 0,part_id, price, length,height,width, description,material_name, unit, name,type);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving material with id = " + partId, e.getMessage());
        }
        return part;
    }

    /*
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
    }*/

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
                ps.setString(6, part.getDBtype());

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
