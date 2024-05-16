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

import java.util.List;
import java.util.Map;

import static app.persistence.OrdersMapper.getOrderByEmail;
import static app.persistence.OrdersMapper.getOrderByUserId;

public class UserController {
    public static void addRoutes(Javalin app) {
        app.post("login", ctx -> {
            login(ctx, ConnectionPool.getInstance());
        });
        app.get("login", ctx -> {
            ctx.render("login.html");
        });

        app.get("logout", ctx -> logout(ctx, ConnectionPool.getInstance()));

        app.post("/ToConfirmation", ctx -> {
            createUser(ctx, ConnectionPool.getInstance());
            ctx.render("checkoutpage.html");
        });
        app.post("/createuser", ctx -> createUser(ctx, ConnectionPool.getInstance()));
        app.get("createuser", ctx -> ctx.render("createuser.html"));

        app.get("/customersitelogin", ctx ->{
            ctx.render("customersitelogin");
        });
        app.post("customerdashboard",ctx -> {
            customerlogin(ctx,ConnectionPool.getInstance());
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


    //todo: programmet vil crashe såfremt der allerede findes en bruger med disse oplysninger
    private static void createUser(Context ctx, ConnectionPool connectionPool) throws DatabaseException {

        // Hent form parametre
        String email = ctx.formParam("email");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");
        String name = ctx.formParam("name");
        String mobile = ctx.formParam("mobile");
        String address = ctx.formParam("address");
        String zipcode = ctx.formParam("zipcode");

        boolean userexist = UserMapper.userexist(email, connectionPool);

        if (!userexist) {
            if (password1.equals(password2)) {
                User newUser = new User(0,email,password1,false,name,mobile,address,zipcode);
                ctx.sessionAttribute("currentUser", newUser);

                //UserMapper.createuser(email, password1, name, mobile, address, zipcode,connectionPool);
                //ctx.attribute("message", "Du er hermed oprettet med brugernavn: " + email + ". Nu kan du logge på.");
                //ctx.attribute("login", true);
                //ctx.render("login.html");

            } else {
                ctx.attribute("message", "Passwords matcher ikke! Prøv igen");
                ctx.attribute("error", true);
                //ctx.render("createuser.html");
            }
        } else {
            ctx.attribute("message", "En bruger med denne email findes allerede. Venligst vælg en anden email");
            //ctx.attribute("login", true);
            //ctx.render("login.html");
        }
    }

    private static void logout(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
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
                ctx.render("carportspecs.html");
            }
        } catch (DatabaseException e) {
            // Her sætter jeg attributten til true hvilket gør at javascriptet vises
            ctx.attribute("login", true);
            // Hvis nej, sendes brugeren tilbage til login siden med fejl besked
            ctx.attribute("message", "Forkert brugernavn eller password. Prøv igen eller opret brugeren!");
            ctx.render("login.html");
        }
    }
    public static void customerlogin(Context ctx, ConnectionPool connectionPool){
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        try {
            User user = UserMapper.login(email, password, connectionPool);
            customerCarportInfo(user,ctx);
            ctx.sessionAttribute("currentUser", user);
            ctx.render("customersite.html");


        } catch (DatabaseException e) {
            ctx.attribute("message", "Forkert brugernavn eller password. Prøv igen eller opret brugeren!");
            ctx.render("customersitelogin.html");

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
