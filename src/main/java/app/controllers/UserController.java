package app.controllers;

import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.security.SecureRandom;

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
    }



//todo: programmet vil crashe såfremt der allerede findes en bruger med disse oplysninger
    private static void createUser(Context ctx, ConnectionPool connectionPool) throws DatabaseException {

        // Hent form parametre
        String email = ctx.formParam("email");
        //String password1 = ctx.formParam("password1");
        //String password2 = ctx.formParam("password2");
        String name = ctx.formParam("name");
        String mobile = ctx.formParam("mobile");
        String address = ctx.formParam("address");
        String zipcode = ctx.formParam("zipcode");

        String password = generatePassword(20);
        //boolean userexist = UserMapper.userexist(email, connectionPool);

        if (/*!userexist*/true) {
            //if (password1.equals(password2)) {
                User newUser = new User(0,email,password,false,name,mobile,address,zipcode);
                ctx.sessionAttribute("currentUser", newUser);

                //UserMapper.createuser(email, password1, name, mobile, address, zipcode,connectionPool);
                //ctx.attribute("message", "Du er hermed oprettet med brugernavn: " + email + ". Nu kan du logge på.");
                //ctx.attribute("login", true);
                //ctx.render("login.html");

            /*} else {
                ctx.attribute("message", "Passwords matcher ikke! Prøv igen");
                ctx.attribute("error", true);
                //ctx.render("createuser.html");
            }*/
        } else {
            ctx.attribute("message", "En bruger med denne email findes allerede. Venligst vælg en anden email");
            //ctx.attribute("login", true);
            //ctx.render("login.html");
        }
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
}
