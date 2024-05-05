package app.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import app.entities.Carport;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.OrdersMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import app.services.CarportSvg;

public class OrderController {
    static Carport carport;
    static Date today = new Date();
    static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
    static String formattedDate = formatter.format(today);
    public static void addRoutes(Javalin app) {
        app.get("/", ctx -> {
            ctx.render("index.html");
        });
        app.post("/createcarport", ctx -> {
            showOrder(ctx);
            ctx.render("showOrder.html");
        });
        app.post("/ordercarport", ctx -> {
            createCarport(ctx, ConnectionPool.getInstance());
            ctx.render("index.html");
        });
        app.post("/changeorder", ctx -> {
            ctx.render("index.html");
        });
    }

    private static void index(Context ctx) {
        ctx.render("adminSite.html");
    }

    public static Order getOrderByOrderId(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        return OrdersMapper.getOrderByOrderId(orderId, connectionPool);
    }

    public static Order getOrderByUserId(int userId, ConnectionPool connectionPool) throws DatabaseException {
        return OrdersMapper.getOrderByUserId(userId, connectionPool);
    }

    public static void showOrder(Context ctx) {
        double length = Double.parseDouble(ctx.formParam("length")) * 100;
        double width = Double.parseDouble(ctx.formParam("width")) * 100;
        double height = Double.parseDouble(ctx.formParam("height")) * 100;

        double length_shed = Double.parseDouble(ctx.formParam("length_shed")) * 100;
        double width_shed = Double.parseDouble(ctx.formParam("width_shed")) * 100;
        String roof = ctx.formParam("roof");


        Locale.setDefault(new Locale("US"));
        CarportSvg svg = new CarportSvg((int) width, (int) length, (int) height);
        ctx.attribute("svg", svg.toString());
        ctx.sessionAttribute("length_shed", length_shed);
        ctx.sessionAttribute("width_shed", width_shed);
        ctx.sessionAttribute("length", length);
        ctx.sessionAttribute("width", width);
        ctx.sessionAttribute("height", height);
        ctx.sessionAttribute("roof", roof);
    }

    // er klar og testet
    public static void createCarport(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        double carportWidth = ctx.sessionAttribute("length");
        double carportLength = ctx.sessionAttribute("width");
        double carportHeight = ctx.sessionAttribute("height");
        double shedWidth = ctx.sessionAttribute("width_shed");
        double shedLength = ctx.sessionAttribute("length_shed");

        // her skal vi sevfølgelig trække på nogle formParam-data fra den kundeindtastningessiden som vi ikke har lavet endnu
        int currentOrderId;
        int userId = 1001;
        String email = "oleOlsen@gmail.com";
        //double shedWidth = 0;
        //double shedLength = 0;
        String orderDate = formattedDate;
        //int userId = ctx.attribute("userid");
        //String email = ctx.formParam("email");
        //double shedWidth = Double.parseDouble(ctx.formParam("shedwidth"));
        //double shedLength = Double.parseDouble(ctx.formParam("shedLength"));

        try {
            // har ikke sendt sales_price og material_cost med da de skal beregnes her tænker jeg
            currentOrderId = OrdersMapper.addOrder(carportWidth, carportLength, carportHeight, userId, shedWidth, shedLength, email, orderDate, connectionPool);
            ctx.attribute("current_order_id", currentOrderId);
            Order order = getOrderByOrderId(currentOrderId, ConnectionPool.getInstance());
            ctx.attribute("length", order.getCarportLength());
            ctx.attribute("width", order.getCarportWidth());
            ctx.attribute("height", order.getCarportHeight());

        } catch (DatabaseException e) {
            ctx.attribute("message", "Fejl i oprettelse af ordren!! Prøv igen!");
        }
    }
}