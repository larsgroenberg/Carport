package app.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.OrdersMapper;
import app.persistence.PartslistMapper;
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
            createPartsList(ctx, ConnectionPool.getInstance());
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

        Locale.setDefault(new Locale("US"));
        CarportSvg svg = new CarportSvg((int) width, (int) length, (int) height);
        ctx.attribute("svg", svg.toString());
    }

    public static void createCarport(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        double carportWidth = ctx.sessionAttribute("length");
        double carportLength = ctx.sessionAttribute("width");
        double carportHeight = ctx.sessionAttribute("height");
        double shedWidth = ctx.sessionAttribute("width_shed");
        double shedLength = ctx.sessionAttribute("length_shed");
        String roof = ctx.sessionAttribute("roof");
        String email = ctx.sessionAttribute("email");

        String orderDate = formattedDate;

        try {
            int currentOrderId = OrdersMapper.addOrder(carportWidth, carportLength, carportHeight, "modtaget", shedWidth, shedLength, email, orderDate, connectionPool);
            ctx.sessionAttribute("current_order_id", currentOrderId);
            Order order = getOrderByOrderId(currentOrderId, ConnectionPool.getInstance());
        } catch (DatabaseException e) {
            ctx.attribute("message", "Fejl i oprettelse af ordren!! Prøv igen!");
        }
    }

    public static void createPartsList(Context ctx, ConnectionPool connectionPool) throws  DatabaseException {
        double carportWidth = ctx.sessionAttribute("length");
        double carportLength = ctx.sessionAttribute("width");
        double carportHeight = ctx.sessionAttribute("height");
        double shedWidth = ctx.sessionAttribute("width_shed");
        double shedLength = ctx.sessionAttribute("length_shed");
        int currentOrderId = ctx.sessionAttribute("current_order_id");
        String roof = ctx.sessionAttribute("roof");
        
        Partslistline partslistline;

        try {
            // calculating posts
            int numberOfPosts = PartsCalculator.calculateNumberOfPosts(carportWidth, carportLength);
            double metersOfPosts = numberOfPosts*(carportHeight+90);
            Part post = PartslistMapper.getPartByType("stolpe", connectionPool);
            double postListPrice = post.getPrice()*metersOfPosts;
            PartslistMapper.insertPartslistLine(new Partslistline(post.getPartId(), currentOrderId, numberOfPosts, postListPrice, post.getDescription(), post.getUnit(), post.getLength()), connectionPool);

            // calculating Rafts
            int numberOfRafts = PartsCalculator.calculateNumberOfRafts(carportLength);
            double metersOfRafts = numberOfRafts*carportWidth;
            Part raft = PartslistMapper.getPartByTypeAndLength("spær", carportWidth, connectionPool);
            double raftListPrice = raft.getPrice()*metersOfRafts;
            PartslistMapper.insertPartslistLine(new Partslistline(raft.getPartId(), currentOrderId, numberOfRafts, raftListPrice, raft.getDescription(), raft.getUnit(), raft.getLength()), connectionPool);

            // calculating Roof
            double lengthOfRoofPlate = 360;
            int numberofRoofPlates = PartsCalculator.calculateNumberOfRoofPlates(carportLength, carportWidth);
            Part roofPart = PartslistMapper.getPartByTypeAndLength("tagplader", lengthOfRoofPlate, connectionPool);
            double roofListPrice = roofPart.getPrice()*numberofRoofPlates;
            PartslistMapper.insertPartslistLine(new Partslistline(roofPart.getPartId(), currentOrderId, numberofRoofPlates, roofListPrice, roofPart.getDescription(), roofPart.getUnit(), roofPart.getLength()), connectionPool);

            double lenghtOfKryds = 4*carportLength;
            double metersOfBeams = 2*carportLength;
        } catch (DatabaseException e) {
            ctx.attribute("message", "Fejl i oprettelse af PartsListen!");
        }

    }
}