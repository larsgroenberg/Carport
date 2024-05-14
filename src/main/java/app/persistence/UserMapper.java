package app.persistence;

import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.*;

public class UserMapper
{

    public static User login(String email, String password, ConnectionPool connectionPool) throws DatabaseException
    {
        String sql = "select * from public.\"users\" where email=? and password=?";

        try
        (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                int id = rs.getInt("user_id");
                boolean isAdmin = rs.getBoolean("is_admin");
                String name = rs.getString("name");
                String mobile = rs.getString("mobile");
                String address = rs.getString("address");
                String zipcode = rs.getString("zipcode");
                return new User(id, email, password, isAdmin, name, mobile, address, zipcode);

            } else
            {
                throw new DatabaseException("Fejl i login. Prøv igen");
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }


    public static int createuser(String email, String password, String name, String mobile, String address, String zipcode, ConnectionPool connectionPool) throws DatabaseException
    {
        String sql = "insert into users (email, password, is_admin, name, mobile, address, zipcode) values (?,?,?,?,?,?,?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        )
        {
            ps.setString(1, email);
            ps.setString(2, password);
            ps.setBoolean(3, false);
            ps.setString(4, name);
            ps.setString(5, mobile);
            ps.setString(6, address);
            ps.setString(7,zipcode);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1)
            {
                throw new DatabaseException("Fejl ved oprettelse af ny bruger");
            }


            int id = 0;

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
            return id;
        }
        catch (SQLException e)
        {
            String msg = "Der er sket en fejl. Prøv igen";
            if (e.getMessage().startsWith("ERROR: duplicate key value "))
            {
                
                msg = "Din e-email findes allerede. Vælg en andet";


            }
            throw new DatabaseException(msg, e.getMessage());
        }
    }

    public static boolean userexist(String email, ConnectionPool connectionPool) throws DatabaseException
    {
        String sql = "select * from public.\"users\" where email=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
        catch (SQLException e)
        {
            throw new DatabaseException("DB fejl", e.getMessage());

        }

    }

    public static User getCustomerByName(String userName, ConnectionPool connectionPool) throws DatabaseException {
        User user = null;
        String sql = "select * from public.users where name=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
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
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunden med navnet "+userName+" findes ikke i Databasen!");
        }
        return user;
    }

    public static User getCustomerByEmail(String email, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "select * from public.users where email=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
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

    public static int getCustomerId(String email, ConnectionPool connectionPool) throws DatabaseException {

        int id = 0;
        String sql = "select id from public.users where email=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getInt("user_id");
            } else {
                throw new DatabaseException("Fejl i hentning af kundeinfo!");
            }
            return id;
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }

}