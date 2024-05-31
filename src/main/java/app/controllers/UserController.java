package app.controllers;

import app.entities.CarportPart;
import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.CarportPartMapper;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.security.SecureRandom;
import java.util.List;
import static app.persistence.OrdersMapper.getOrderByUserId;

public class UserController {
    public static void addRoutes(Javalin app) {
        app.post("/ToLogin", ctx -> {
            login(ctx, ConnectionPool.getInstance());
        });
        app.get("/ToLogin", ctx -> {
            ctx.render("login.html");
        });
        app.get("/login.html", ctx -> {
            ctx.render("login.html");
        });
        app.get("logout", UserController::logout);

        app.post("/ToConfirmation", ctx -> {
            createUser(ctx);
            ctx.sessionAttribute("confirmed", null);
            ctx.render("checkoutpage.html");
        });
        app.post("/createuser", UserController::createUser);
        app.get("createuser", ctx -> ctx.render("usercredentials.html"));

        app.post("customerdashboard", ctx -> {
            customerlogin(ctx, ConnectionPool.getInstance());
        });
        app.get("/customersite", ctx -> {
            ctx.render("customersite.html");
        });
        app.post("/updateMobile", UserController::updateMobile);
        app.post("/updateName", UserController::updateName);
    }

    private static void updateMobile(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.formParam("user_id"));
            String newMobile = ctx.formParam("mobile");
            UserMapper.updateUserMobile(userId, newMobile, ConnectionPool.getInstance());
            User currentUser = ctx.sessionAttribute("currentUser");
            currentUser.setMobile(newMobile);
            ctx.sessionAttribute("currentUser", currentUser);
            ctx.redirect("/customersite");  // Redirect to a confirmation page or back to the profile
        } catch (NumberFormatException e) {
            ctx.status(400).html("Invalid user ID format");
        } catch (Exception e) {
            ctx.status(500).html("Server Error: " + e.getMessage());
        }
    }

    private static void updateName(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.formParam("user_id"));
            String newName = ctx.formParam("name");
            UserMapper.updateUserName(userId, newName, ConnectionPool.getInstance());
            User currentUser = ctx.sessionAttribute("currentUser");
            currentUser.setName(newName);
            ctx.sessionAttribute("currentUser", currentUser);
            ctx.redirect("/customersite");  // Redirect to a confirmation page or back to the profile
        } catch (NumberFormatException e) {
            ctx.status(400).html("Invalid user ID format");
        } catch (Exception e) {
            ctx.status(500).html("Server Error: " + e.getMessage());
        }
    }

    private static void createUser(Context ctx) {

        // Hent form parametre
        String email = ctx.formParam("email");
        String name = ctx.formParam("name");
        String mobile = ctx.formParam("mobile");
        String address = ctx.formParam("address");
        String zipcode = ctx.formParam("zipcode");

        String password = generatePassword(20);
        User newUser = new User(0, email, password, false, name, mobile, address, zipcode);
        ctx.sessionAttribute("currentUser", newUser);
    }

    public static String generatePassword(int length) {
        String upperLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()-_=+{}[];:'\",<.>/?\\|`~";

        String combinedChars = upperLetters + lowerLetters + numbers + specialChars;
        SecureRandom random = new SecureRandom();
        char[] password = new char[length];

        for (int i = 0; i < length; i++) {
            password[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
        }

        return new String(password);
    }

    private static void logout(Context ctx) {
        // Her sletter jeg alle sessionAttributter
        ctx.req().getSession().invalidate();
        // Her sender jeg brugeren tilbage til forsiden carportspecs.html
        ctx.redirect("/");
    }

    public static void login(Context ctx, ConnectionPool connectionPool) {
        // Hent form parametre
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        // Denne attribut bruger jeg til mine javascript-bokse. Boksen skal kun vises hvis den er true
        ctx.attribute("login", false);

        // Check om bruger findes i DB med de angivne username + password
        try {
            User user = UserMapper.login(email, password, connectionPool);
            ctx.sessionAttribute("currentUser", user);

            // Hvis brugeren er admin, sendes han videre til adminsite.html
            if (user.isAdmin()) {
                ctx.render("adminsite.html");
            } else {
                // Her sender jeg brugeren tilbage til carportspecs.html
                customerlogin(ctx, connectionPool);
            }
        } catch (DatabaseException e) {
            // Her sætter jeg attributten til true hvilket gør at javascriptet vises
            ctx.attribute("login", true);
            // Hvis nej, sendes brugeren tilbage til login siden med fejl besked
            ctx.attribute("message", "Forkert brugernavn eller password. Prøv igen eller opret brugeren!");
            ctx.render("login.html");
        }
    }

    public static void customerlogin(Context ctx, ConnectionPool connectionPool) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        try {
            User user = UserMapper.login(email, password, connectionPool);
            customerCarportInfo(user, ctx);
            ctx.sessionAttribute("currentUser", user);
            ctx.render("customersite.html");

        } catch (DatabaseException e) {
            ctx.attribute("message", "Forkert brugernavn eller password. Prøv igen eller opret brugeren!");
            ctx.render("login.html");
        }
    }

    private static void customerCarportInfo(User user, Context ctx) {
        try {
            int userid = user.getUserId();
            Order customerOrder = getOrderByUserId(userid, ConnectionPool.getInstance());
            int orderId = customerOrder.getOrderId();
            List<CarportPart> customerPartsList = CarportPartMapper.getCompletePartsListByOrderId(orderId, ConnectionPool.getInstance());
            ctx.sessionAttribute("carportInfo", customerOrder);
            ctx.sessionAttribute("customerPartsList", customerPartsList);
            System.out.println(customerPartsList);
        } catch (DatabaseException e) {
            ctx.status(500).result("Failed to retrieve order: " + e.getMessage());
        }
    }
}
