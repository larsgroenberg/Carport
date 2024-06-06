package app.persistence;

import app.entities.User;
import app.exceptions.DatabaseException;
import java.sql.*;

public class UserMapper {

    public static User login(String email, String password, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "select * from public.\"users\" where email=? and password=?";

        try
                (
                        Connection connection = connectionPool.getConnection();
                        PreparedStatement ps = connection.prepareStatement(sql)
                ) {
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("user_id");
                boolean isAdmin = rs.getBoolean("is_admin");
                String name = rs.getString("name");
                String mobile = rs.getString("mobile");
                String address = rs.getString("address");
                String zipcode = rs.getString("zipcode");
                return new User(id, email, password, isAdmin, name, mobile, address, zipcode);

            } else {
                throw new DatabaseException("Fejl i login. Prøv igen");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }

    public static int createuser(String email, String password, String name, String mobile, String address, String zipcode, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "insert into users (email, password, is_admin, name, mobile, address, zipcode) values (?,?,?,?,?,?,?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, email);
            ps.setString(2, password);
            ps.setBoolean(3, false);
            ps.setString(4, name);
            ps.setString(5, mobile);
            ps.setString(6, address);
            ps.setString(7, zipcode);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("Fejl ved oprettelse af ny bruger");
            }

            int id = 0;

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
            return id;
        } catch (SQLException e) {
            String msg = "Der er sket en fejl. Prøv igen";
            if (e.getMessage().startsWith("ERROR: duplicate key value ")) {
                msg = "Din e-email findes allerede. Vælg en andet";
            }
            throw new DatabaseException(msg, e.getMessage());
        }
    }

    public static void deleteUserByUserId(int userId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (
                // Her opretter jeg forbindelse til databasen
                Connection connection = connectionPool.getConnection();
                // Her opretter jeg et PreparedStatement til at udføre SQL-forespørgslen
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            // Her sættes parameteren for PreparedStatement
            ps.setInt(1, userId);
            int rowsAffected = ps.executeUpdate();

            // Tjek om nogen rækker blev påvirket (dvs. om brugeren blev slettet)
            if (rowsAffected == 0) {
                // Hvis ingen rækker blev påvirket, betyder det, at brugeren ikke blev fundet
                System.out.println("Brugeren blev ikke fundet i databasen ");
            } else {
                System.out.println("Brugeren blev slettet i databasen ");
            }
        } catch (Exception e) {
            // Her fanger jeg alle undtagelser og kaster dem som DatabaseException
            throw new DatabaseException("Ved forsøg på sletning af bruger med brugerId: "+userId+" opstod følgende fejl: " + e.getMessage(), e);
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
            throw new DatabaseException("Kunden med navnet " + userName + " findes ikke i Databasen!");
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

    public static void updateUserMobile(int userId, String mobile, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE public.\"users\" SET mobile = ? WHERE user_id = ?;";

        sqlUpdate(userId, mobile, connectionPool, sql);
    }

    public static void updateUserName(int userId, String name, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE public.\"users\" SET name = ? WHERE user_id = ?;";

        sqlUpdate(userId, name, connectionPool, sql);
    }

    private static void sqlUpdate(int userId, String informationToUpdate, ConnectionPool connectionPool, String sql) throws DatabaseException {
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setString(1, informationToUpdate);
            ps.setInt(2, userId);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Ingen ændringer blev foretaget. Kontroller bruger-id'et.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl under opdatering af brugeroplysninger", e.getMessage());
        }
    }
}
