package app.controllers;

import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.CarportMapper;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

public class UserController {
    public static void addRoutes(Javalin app) {
        app.post("login", ctx -> {
            login(ctx, ConnectionPool.getInstance());
        });
        app.get("login", ctx -> {
            ctx.render("login.html");
        });
        app.get("index.html", ctx -> {
            ctx.render("index.html");
        });
        app.get("logout", ctx -> logout(ctx, ConnectionPool.getInstance()));
        app.post("createuser", ctx -> createUser(ctx, ConnectionPool.getInstance()));
        app.get("createuser", ctx -> ctx.render("createuser.html"));
    }

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
        ctx.attribute("createuser", true);
        ctx.attribute("usercreated", false);

        if (!userexist) {
            if (password1.equals(password2)) {
                try {
                    UserMapper.createuser(email, password1, name, mobile, address, zipcode,connectionPool);
                    ctx.attribute("message", "Du er hermed oprettet med brugernavn: " + email +
                            ". Nu kan du logge på.");
                    ctx.attribute("login", true);
                    ctx.render("login.html");
                } catch (DatabaseException e) {
                    ctx.attribute("message", "Dit brugernavn findes allerede. Prøv igen, eller log ind");
                    ctx.attribute("login", true);
                    ctx.render("login.html");
                }
            } else {
                ctx.attribute("message", "Dine to passwords matcher ikke! Prøv igen");
                ctx.attribute("createuser", true);
                ctx.render("createuser.html");
            }
        } else {
            ctx.attribute("message", "Dit brugernavn findes allerede. Prøv igen, eller log ind");
            ctx.attribute("login", true);
            ctx.render("login.html");
        }
    }

    private static void logout(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
            // Her sletter jeg alle sessionAttributter
            ctx.req().getSession().invalidate();
            // Her sender jeg brugeren tilbage til forsiden index.html
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
                ctx.render("adminSite.html");
            } else {
                // Her sender jeg brugeren tilbage til index.html
                ctx.render("index.html");
            }
        } catch (DatabaseException e) {
            // Her sætter jeg attributten til true hvilket gør at javascriptet vises
            ctx.attribute("login", true);
            // Hvis nej, sendes brugeren tilbage til login siden med fejl besked
            ctx.attribute("message", "Forkert brugernavn eller password. Prøv igen eller opret brugeren!");
            ctx.render("login.html");
        }
    }
}
