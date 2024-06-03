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
            // Her henter vi user_id og konverterer Stringen til int
            int userId = Integer.parseInt(ctx.formParam("user_id"));

            // Her henter vi det nye mobilnummer fra formularen
            String newMobile = ctx.formParam("mobile");

            // Her opdaterer vi brugerens mobilnummer i databasen
            UserMapper.updateUserMobile(userId, newMobile, ConnectionPool.getInstance());

            // Her henter vi den nuværende bruger fra sessionen
            User currentUser = ctx.sessionAttribute("currentUser");

            // Her opdaterer vi brugerens mobilnummer i sessionen
            currentUser.setMobile(newMobile);
            ctx.sessionAttribute("currentUser", currentUser);

            // Her omdirigerer vi til en bekræftelsesside eller tilbage til profilsiden
            ctx.redirect("/customersite");
        } catch (NumberFormatException e) {
            // Her håndterer vi fejl hvis user_id ikke kan konverteres til et heltal
            ctx.status(400).html("Ugyldigt format for bruger-ID");
        } catch (DatabaseException e) {
            // Her håndterer vi fejl relateret til databaseoperationer
            ctx.status(500).html("Fejl ved opdatering af mobilnummer: " + e.getMessage());
        } catch (Exception e) {
            // Her håndterer vi andre generelle fejl
            ctx.status(500).html("Serverfejl: " + e.getMessage());
        }
    }

    private static void updateName(Context ctx) {
        try {
            // Her henter vi user_id og konverterer Stringen til int
            int userId = Integer.parseInt(ctx.formParam("user_id"));

            // Her henter vi det nye navn fra formularen
            String newName = ctx.formParam("name");

            // Her opdaterer vi brugerens navn i databasen
            UserMapper.updateUserName(userId, newName, ConnectionPool.getInstance());

            // Her henter vi den nuværende bruger fra sessionen
            User currentUser = ctx.sessionAttribute("currentUser");

            // Her opdaterer vi brugerens navn i sessionen
            currentUser.setName(newName);
            ctx.sessionAttribute("currentUser", currentUser);

            // Her omdiriger vi til en bekræftelsesside eller tilbage til profilsiden
            ctx.redirect("/customersite");
        } catch (NumberFormatException e) {
            // Her håndterer vi fejl hvis user_id ikke kan konverteres til et heltal
            ctx.status(400).html("Ugyldigt format for bruger-ID");
        } catch (DatabaseException e) {
            // Her håndterer vi fejl relateret til databaseoperationer
            ctx.status(500).html("Fejl ved opdatering af brugernavn: " + e.getMessage());
        } catch (Exception e) {
            // Her håndterer vi andre generelle fejl
            ctx.status(500).html("Serverfejl: " + e.getMessage());
        }
    }

    private static void createUser(Context ctx) {
        // Hent form parametre fra forespørgslen
        String email = ctx.formParam("email");
        String name = ctx.formParam("name");
        String mobile = ctx.formParam("mobile");
        String address = ctx.formParam("address");
        String zipcode = ctx.formParam("zipcode");

        // Her genererer vi en adgangskode med en længde på 20 tegn
        String password = generatePassword(20);

        // Her opretter vi en ny bruger med de hentede oplysninger og den genererede adgangskode
        User newUser = new User(0, email, password, false, name, mobile, address, zipcode);

        // Her sætter vi den nye bruger i sessionen under attributten "currentUser"
        ctx.sessionAttribute("currentUser", newUser);
    }

    public static String generatePassword(int length) {
        // Her definerer vi karakterkategorier
        String upperLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()-_=+{}[];:'\",<.>/?\\|`~";

        // Her kombinerer vi alle karakterkategorier til én streng
        String combinedChars = upperLetters + lowerLetters + numbers + specialChars;

        // Her bruger vi SecureRandom til at generere tilfældige karakterer
        SecureRandom random = new SecureRandom();

        // Her opretter vi en char array til at holde adgangskoden
        char[] password = new char[length];

        // Her genererer vi adgangskoden ved at vælge tilfældige karakterer fra combinedChars
        for (int i = 0; i < length; i++) {
            password[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
        }
        // Her konverterer vi char arrayet til en streng og returnerer den
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

        // Her checker vi om brugeren findes i DB med det angivne username og password
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
        // Her henter vi email og password fra formularen
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        try {
            // Her forsøger vi at logge brugeren ind ved hjælp af UserMapper
            User user = UserMapper.login(email, password, connectionPool);

            // Hvis login lykkes, henter vi brugerens carportoplysninger og sætter dem i sessionen
            customerCarportInfo(user, ctx);
            ctx.sessionAttribute("currentUser", user);

            // Her renderer/sender vi kunden videre til customersite.html
            ctx.render("customersite.html");

        } catch (DatabaseException e) {
            // Her håndterer vi en evt. DatabaseException ved login-fejl
            ctx.attribute("message", "Forkert brugernavn eller password. Prøv igen eller opret brugeren!");

            // Her renderer vi login siden igen med ovenstående fejlmeddelelse
            ctx.render("login.html");
        }
    }


    private static void customerCarportInfo(User user, Context ctx) {
        try {
            // Her henter vi brugerens ID
            int userId = user.getUserId();

            // Her henter vi brugerens ordre ved brug af userId
            Order customerOrder = getOrderByUserId(userId, ConnectionPool.getInstance());

            // Her hentes ordre-ID'et fra den hentede ordre
            int orderId = customerOrder.getOrderId();

            // Her hentes listen af dele til carporten baseret på ordre-ID
            List<CarportPart> customerPartsList = CarportPartMapper.getCompletePartsListByOrderId(orderId, ConnectionPool.getInstance());

            // Her gemmer vi ordreoplysninger og deleliste i sessionen
            ctx.sessionAttribute("carportInfo", customerOrder);
            ctx.sessionAttribute("customerPartsList", customerPartsList);

            // Her udskriver vi delelisten til konsollen (til debug-formål)
            System.out.println(customerPartsList);
        } catch (DatabaseException e) {
            // Log fejlmeddelelsen (kan tilpasses til at bruge et logger-bibliotek som Log4j)
            System.err.println("Fejl ved hentning af ordre: " + e.getMessage());

            // Returner en 500 statuskode som er en internal server error og en brugervenlig fejlmeddelelse
            ctx.status(500).result("Kunne ikke hente ordren. Prøv venligst igen senere.");
        }
    }

}
