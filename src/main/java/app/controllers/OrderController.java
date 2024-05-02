package app.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.OrdersFacade;
import io.javalin.Javalin;
import io.javalin.http.Context;
import app.services.CarportSvg;

public class OrderController {
    public static void addRoutes(Javalin app) {
        app.get("/", ctx -> {
            ctx.render("index.html");
        });
        app.post("/createorder", ctx -> {
            showOrder(ctx);
            ctx.render("showOrder.html");
        });
    }

    private static void index(Context ctx) {
        ctx.render("adminSite.html");
    }

    public static void showOrder(Context ctx) {
        double length = Double.parseDouble(ctx.formParam("length")) * 100;
        double width = Double.parseDouble(ctx.formParam("width")) * 100;
        double height = Double.parseDouble(ctx.formParam("height")) * 100;

        Locale.setDefault(new Locale("US"));
        CarportSvg svg = new CarportSvg((int) width, (int) length, (int) height);
        ctx.attribute("svg", svg.toString());
    }

    // er klar og testet til når kunden accepterer prisen. Dog skal vi lige sætte skurbredde og højde til default 0 i databasen
    public static void createCarport(Context ctx, ConnectionPool connectionPool) throws IOException, DatabaseException, SQLException {
        int currentOrderId;
        int userId = ctx.attribute("userid");
        double length = Double.parseDouble(ctx.formParam("length"));
        double width = Double.parseDouble(ctx.formParam("width"));
        double height = Double.parseDouble(ctx.formParam("height"));

        double shedWidth = Double.parseDouble(ctx.formParam("shedwidth"));
        double shedLength = Double.parseDouble(ctx.formParam("shedLength"));

        try {
            currentOrderId = OrdersFacade.addOrder(width, length, height, userId, shedWidth, shedLength, ConnectionPool.getInstance());
            ctx.attribute("current_order_id", currentOrderId);
            Order order = OrdersFacade.getOrderByOrderId(currentOrderId, ConnectionPool.getInstance());
            ctx.attribute("length", order.getCarportLength());
            ctx.attribute("width", order.getCarportWidth());
            ctx.attribute("height", order.getCarportHeight());

        } catch (DatabaseException e) {
            ctx.attribute("message", "Fejl i oprettelse af ordren!! Prøv igen!");
        }
    }
}