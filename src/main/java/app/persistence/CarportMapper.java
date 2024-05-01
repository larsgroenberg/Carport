package app.persistence;

import app.entities.Carport;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarportMapper {


    /*public static List<Carport> showOrders(ConnectionPool connectionPool) throws DatabaseException {
        List<Carport> orderList = new ArrayList<>();
        String sql = "select * from order";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                int order_id = rs.getInt("order_id");
                int user_id = rs.getInt("user_id");
                String orderdate = rs.getString("orderdate");
                int orderprice = rs.getInt("orderprice");
                String status = rs.getString("status");
                int length = rs.getInt("length");
                int width = rs.getInt("width");
                boolean with_roof = rs.getBoolean("with_roof");

                orderList.add(new Carport(order_id,user_id,orderdate,orderprice,status,length,width,with_roof));
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl!!!!", e.getMessage());
        }
        return orderList;
    }*/


    public static Carport getOrderById(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        Carport carport = null;

        String sql = "SELECT * FROM orders WHERE order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int order_id = rs.getInt("topping_id");
                int user_id = rs.getInt("user_id");
                String orderdate = rs.getString("orderdate");
                int orderprice = rs.getInt("orderprice");
                String status = rs.getString("status");
                int length = rs.getInt("length");
                int width = rs.getInt("width");
                boolean with_roof = rs.getBoolean("with_roof");
                carport = new Carport(order_id,user_id,orderdate,orderprice,status,length,width,with_roof);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving bottom with id = " + orderId, e.getMessage());
        }
        return carport;
    }




    public static void InsertIntoOrders(int userId, String orderdate, int orderprice, String status, int length, int width, boolean with_roof, ConnectionPool connectionPool) throws DatabaseException
    {
        String sql = "INSERT INTO orders (user_id, orderdate, orderprice, status, length, width, with_roof) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {

            ps.setInt(1, userId);
            ps.setString(2, orderdate);
            ps.setInt(3, orderprice);
            ps.setString(4, status);
            ps.setInt(5, length);
            ps.setInt(6, width);
            ps.setBoolean(7, with_roof);


            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1)
            {
                throw new DatabaseException("Fejl ved at indsætte order");
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e.getMessage());
        }
    }
}
