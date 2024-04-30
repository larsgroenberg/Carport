package app.persistence;

import app.entities.Order;
import app.entities.Partslistline;
import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartslistMapper {

    public static void insertPartslistLine(Partslistline partslistline, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "insert into partslist(material_id, order_id, quantity, partslistprice) values (?,?,?,?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setInt(1, partslistline.getMaterialId());
            ps.setInt(2, partslistline.getOrderId());
            ps.setInt(3, partslistline.getQuantity());
            ps.setDouble(4, partslistline.getPartlistlineprice());

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

    public static ArrayList<Partslistline> getPartslist(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Partslistline> partslistlines = new ArrayList<>();

        String sql = "SELECT * FROM partslist";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int partlist_id = rs.getInt("partlist_id");
                int materialId = rs.getInt("material_id");
                int quantity = rs.getInt("quantity");
                int price = rs.getInt("price");

                Partslistline partslistLine = new Partslistline(materialId, orderId, quantity, price);
                partslistlines.add(partslistLine);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving topping with id = " + e.getMessage());
        }
        return partslistlines;
    }
}
