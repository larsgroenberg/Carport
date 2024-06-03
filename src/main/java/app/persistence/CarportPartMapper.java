package app.persistence;

import app.entities.CarportPart;
import app.exceptions.DatabaseException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarportPartMapper {
    public static ArrayList<CarportPart> getDBParts(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<CarportPart> partList = new ArrayList<>();
        String sql = "SELECT * FROM parts";

        try (
                // Her opretter jeg forbindelse til databasen
                Connection connection = connectionPool.getConnection();
                // Her opretter jeg et PreparedStatement til at udføre SQL-forespørgslen
                PreparedStatement ps = connection.prepareStatement(sql);
                // Her udfører jeg forespørgslen og gemmer resultatet i et ResultSet
                ResultSet rs = ps.executeQuery()
        ) {
            // Her løber jeg gennem ResultSet'et og tilføjer hver del til partList
            while (rs.next()) {
                int partId = rs.getInt("part_id");
                double price = rs.getDouble("price");
                String description = rs.getString("description");
                int length = rs.getInt("length");
                int height = rs.getInt("height");
                int width = rs.getInt("width");
                String type = rs.getString("type");
                String material_name = rs.getString("material");
                String unit = rs.getString("unit");
                String name = rs.getString("name");

                // Map type til CarportPartType og opret CarportPart-objekt
                CarportPart.CarportPartType partType = mapToCarportPartType(type);
                partList.add(new CarportPart(partId, partType, 0, price, length, height, width, description, material_name, unit, name, type));
            }
        } catch (SQLException e) {
            // Her kaster vi en DatabaseException hvis der opstår en SQL-relateret fejl
            throw new DatabaseException("Det lykkedes ikke at hente byggematerialer fra databasen", e);
        }
        return partList;
    }


    public static CarportPart getPartById(int partId, ConnectionPool connectionPool) throws DatabaseException {
        CarportPart part = null;
        String sql = "SELECT * FROM parts WHERE part_id = ?";

        try (
                // Her opretter jeg forbindelse til databasen
                Connection connection = connectionPool.getConnection();
                // Her opretter jeg et PreparedStatement til at udføre SQL-forespørgslen
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            // Her sætter jeg parametrene for PreparedStatementet
            ps.setInt(1, partId);

            // Her udfører jeg forespørgslen og gemmer resultatet i et ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Her henter jeg data fra ResultSet'et
                    double price = rs.getDouble("price");
                    String description = rs.getString("description");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String type = rs.getString("type");
                    String material_name = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");

                    // Map type til CarportPartType og opret CarportPart-objekt
                    CarportPart.CarportPartType partType = mapToCarportPartType(type);
                    part = new CarportPart(partId, partType, 0, price, length, height, width, description, material_name, unit, name, type);
                }
            }
        } catch (SQLException e) {
            // Her kaster vi en DatabaseException hvis der opstår en SQL-relateret fejl
            throw new DatabaseException("Fejl ved hentning af materialet med id = " + partId, e);
        }

        return part;
    }

    public static void addPart(CarportPart part, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO parts (price, description, length, height, width, type, material, unit, name) VALUES (?,?,?,?,?,?,?,?,?)";

        try (
                // Her opretter jeg forbindelse til databasen
                Connection connection = connectionPool.getConnection();
                // Her opretter jeg et PreparedStatement til at udføre SQL-forespørgslen
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            // Hej sætter jeg parametrene for PreparedStatementet
            ps.setDouble(1, part.getDBprice());
            ps.setString(2, part.getDBdescription());
            ps.setInt(3, part.getDBlength());
            ps.setInt(4, part.getDBheight());
            ps.setInt(5, part.getDBwidth());
            ps.setString(6, part.getDBtype());
            ps.setString(7, part.getDBmaterial());
            ps.setString(8, part.getDBunit());
            ps.setString(9, part.getDBname());

            // Her udfører jeg indsættelsen
            ps.executeUpdate();
        } catch (SQLException e) {
            // Her kaster vi en DatabaseException hvis der opstår en SQL-relateret fejl
            throw new DatabaseException("Byggematerialet kunne ikke indsættes i databasen", e);
        }
    }


    public static void updatePart(CarportPart part, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE parts SET price = ?, description = ?, length = ?, height = ?, width = ?, type = ?, material = ?, unit = ?, name = ? WHERE part_id = ?";

        try (
                // Her opretter jeg forbindelse til databasen
                Connection connection = connectionPool.getConnection();
                // Her opretter jeg et PreparedStatement til at udføre SQL-forespørgslen
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            // Her sætter vi parametrene for PreparedStatementet
            ps.setDouble(1, part.getDBprice());
            ps.setString(2, part.getDBdescription());
            ps.setInt(3, part.getDBlength());
            ps.setInt(4, part.getDBheight());
            ps.setInt(5, part.getDBwidth());
            ps.setString(6, part.getDBtype());
            ps.setString(7, part.getDBmaterial());
            ps.setString(8, part.getDBunit());
            ps.setString(9, part.getDBname());
            ps.setInt(10, part.getPartId());

            // Her udfører vi opdateringen
            ps.executeUpdate();
        } catch (SQLException e) {
            // Her kaster vi en DatabaseException hvis der opstår en SQL-relateret fejl
            throw new DatabaseException("Part'en kunne ikke opdateres i databasen", e);
        }
    }


    public static List<CarportPart> getCompletePartsListByOrderId(int orderId, ConnectionPool pool) throws DatabaseException {
        List<CarportPart> partsList = new ArrayList<>();
        String sql = "SELECT p.part_id, p.type, pl.quantity, p.price as DBprice, p.length as DBlength, p.height as DBheight, " +
                "p.width as DBwidth, p.description as DBdescription, p.material as DBmaterial, p.unit as DBunit, p.name as DBname, " +
                "p.type as DBtype FROM parts p JOIN partslist pl ON p.part_id = pl.part_id WHERE pl.order_id = ?";

        try (Connection conn = pool.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CarportPart.CarportPartType partType = mapToCarportPartType(rs.getString("type")); // This now returns a list of types
                partsList.add(new CarportPart(
                        rs.getInt("part_id"),
                        partType,
                        rs.getInt("quantity"),
                        rs.getDouble("DBprice"),
                        rs.getInt("DBlength"),
                        rs.getInt("DBheight"),
                        rs.getInt("DBwidth"),
                        rs.getString("DBdescription"),
                        rs.getString("DBmaterial"),
                        rs.getString("DBunit"),
                        rs.getString("DBname"),
                        rs.getString("DBtype")
                ));

            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch parts list: " + e.getMessage());
        }
        return partsList;
    }

    private static CarportPart.CarportPartType mapToCarportPartType(String dbType) {
        CarportPart.CarportPartType partType = null;
        switch (dbType) {
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
        return partType;
    }
}
