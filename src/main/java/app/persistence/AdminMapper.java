package app.persistence;

import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AdminMapper {

    public static Order getOrderByEmail(String email,ConnectionPool connectionPool) throws DatabaseException {

        String sql = "select * from public.ordrene where email=?";
        try
        (
            Connection connection = connectionPool.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                int userId = rs.getInt("user_id");
                int orderId = rs.getInt("order_id");
                int carportLength = rs.getInt("carportLength");
                int carportWidth = rs.getInt("carportWidth");
                int carportHeight = rs.getInt("carportHeight");
                int materialCost = rs.getInt("materialCost");
                String status= rs.getString("status");
                int shedWidth = rs.getInt("shedWidth");
                int shedLength = rs.getInt("shedLength");
                int salesPrice = rs.getInt("salesPrice");
                return new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, status, shedWidth, shedLength, email);

            } else
            {
                throw new DatabaseException("Fejl i hentning af ordre!");
            }
        } catch (SQLException e)
        {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }

    public static Order getOrderByName(String userName,ConnectionPool connectionPool) throws DatabaseException {

        String sql = "select * from public.ordrene where name=?";
        try
                (
                        Connection connection = connectionPool.getConnection();
                        PreparedStatement ps = connection.prepareStatement(sql)
                )
        {
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                int userId = rs.getInt("user_id");
                int orderId = rs.getInt("order_id");
                int carportLength = rs.getInt("carportLength");
                int carportWidth = rs.getInt("carportWidth");
                int carportHeight = rs.getInt("carportHeight");
                int materialCost = rs.getInt("materialCost");
                String status= rs.getString("status");
                int shedWidth = rs.getInt("shedWidth");
                int shedLength = rs.getInt("shedLength");
                int salesPrice = rs.getInt("salesPrice");
                String email = rs.getString("email");
                return new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, status, shedWidth, shedLength, email);

            } else
            {
                throw new DatabaseException("Fejl i hentning af ordre!");
            }
        } catch (SQLException e)
        {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }

    public static ArrayList<Order> showAllOrders(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Order> orderList = new ArrayList<>();
        String query = "SELECT u.user_id, o.order_id, u.email, o.c_length AS carportLength, o.c_width AS carportWidth, o.c_height AS carportHeight, o.material_cost AS materialCost, o.status, o.s_width AS shedWidth, o.s_length AS shedLength, o.sales_price AS salesPrice" +
                "FROM public.ordrene o JOIN public.users u ON o.user_id = u.user_id";
        try
                (
                        Connection connection = connectionPool.getConnection();
                        PreparedStatement ps = connection.prepareStatement(query)
                ) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                int orderId = resultSet.getInt("order_id");
                String email = resultSet.getString("email");
                int carportLength = resultSet.getInt("carportLength");
                int carportWidth = resultSet.getInt("carportWidth");
                int carportHeight = resultSet.getInt("carportHeight");
                int materialCost = resultSet.getInt("materialCost");
                String status = resultSet.getString("status");
                int shedWidth = resultSet.getInt("shedWidth");
                int shedLength = resultSet.getInt("shedLength");
                int salesPrice = resultSet.getInt("salesPrice");

                Order newOrder = new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, status, shedWidth, shedLength, email);
                orderList.add(newOrder);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return orderList;
    }

    public static User getCustomerByName(String userName,ConnectionPool connectionPool) throws DatabaseException {

        String sql = "select * from public.users where name=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
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

    public static User getCustomerByEmail(String email,ConnectionPool connectionPool) throws DatabaseException {

        String sql = "select * from public.users where email=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
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