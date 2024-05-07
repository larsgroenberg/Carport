package app.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
            createCarport(ctx, ConnectionPool.getInstance());
            createPartsList(ctx, ConnectionPool.getInstance());
            ctx.render("showOrder.html");
        });
        app.post("/ordercarport", ctx -> {
            //createCarport(ctx, ConnectionPool.getInstance());
            //createPartsList(ctx, ConnectionPool.getInstance());
            ctx.render("showOrder.html");
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
        double length = Double.parseDouble(ctx.formParam("length"));
        double width = Double.parseDouble(ctx.formParam("width"));
        double height = Double.parseDouble(ctx.formParam("height"));
        double length_shed = Double.parseDouble(ctx.formParam("length_shed"));
        double width_shed = Double.parseDouble(ctx.formParam("width_shed"));
        String roof = (ctx.formParam("roof"));
        ctx.sessionAttribute("length", length);
        ctx.sessionAttribute("width", width);
        ctx.sessionAttribute("height", height);
        ctx.sessionAttribute("width_shed", length_shed);
        ctx.sessionAttribute("length_shed", width_shed);
        ctx.sessionAttribute("email", "oleolesen@gmail.com");
        ctx.sessionAttribute("roof", roof);

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
            OrdersMapper.addOrder(carportWidth, carportLength, carportHeight, "modtaget", shedWidth, shedLength, email, orderDate, roof, connectionPool);
            Order order = OrdersMapper.getOrderByEmail(email, connectionPool);
            int currentOrderId = order.getOrderId();
            ctx.sessionAttribute("current_order_id", currentOrderId);

        } catch (DatabaseException e) {
            ctx.attribute("message", "Fejl i oprettelse af ordren!! Prøv igen!");
        }
    }

    public static void createPartsList(Context ctx, ConnectionPool connectionPool) throws  DatabaseException {
        double carportWidth = ctx.sessionAttribute("width");
        double carportLength = ctx.sessionAttribute("length");
        double carportHeight = ctx.sessionAttribute("height");
        double shedWidth = ctx.sessionAttribute("width_shed");
        double shedLength = ctx.sessionAttribute("length_shed");
        int currentOrderId = ctx.sessionAttribute("current_order_id");
        String roof = ctx.sessionAttribute("roof");
        String email = ctx.sessionAttribute("email");

        try {
            // calculating and inserting Posts into Partslist
            int numberOfPosts = PartsCalculator.calculateNumberOfPosts(carportWidth, carportLength);
            double metersOfPosts = numberOfPosts*(carportHeight+90);
            Part post = PartslistMapper.getPartByType("stolpe", connectionPool);
            double postListPrice = post.getPrice() * (metersOfPosts/100);
            PartslistMapper.insertPartslistLine(new Partslistline(post.getPartId(), currentOrderId, numberOfPosts, postListPrice, post.getDescription(), post.getUnit(), post.getLength(), post.getName()), connectionPool);

            // calculating and inserting Rafts into Partslist
            int numberOfRafts = PartsCalculator.calculateNumberOfRafts(carportLength);
            double metersOfRafts = numberOfRafts*carportWidth;
            Part raft = PartslistMapper.getPartByTypeAndLength("spær", carportWidth, connectionPool);
            double raftListPrice = raft.getPrice() * (metersOfRafts/100);
            PartslistMapper.insertPartslistLine(new Partslistline(raft.getPartId(), currentOrderId, numberOfRafts, raftListPrice, raft.getDescription(), raft.getUnit(), raft.getLength(), raft.getName()), connectionPool);

            // calculating and inserting Roofmaterial into PartsList
            if(ctx.sessionAttribute("roof") != null) {
                double lengthOfRoofPlate = 360;
                int numberofRoofPlates = PartsCalculator.calculateNumberOfRoofPlates(carportLength, carportWidth);
                Part roofPart = PartslistMapper.getPartByTypeAndLength("tagplader", lengthOfRoofPlate, connectionPool);
                double roofListPrice = roofPart.getPrice() * (numberofRoofPlates/100);
                PartslistMapper.insertPartslistLine(new Partslistline(roofPart.getPartId(), currentOrderId, numberofRoofPlates, roofListPrice, roofPart.getDescription(), roofPart.getUnit(), roofPart.getLength(), roofPart.getName()), connectionPool);
            }

            // calculating and inserting vindkryds into PartsList
            double lenghtOfKryds = 4*(carportLength/100);
            double quantityOfKryds = lenghtOfKryds/10;
            int roundedQuantity = (int) Math.ceil(quantityOfKryds);
            Part kryds = PartslistMapper.getPartByType("hulbånd", connectionPool);
            double krydsListPrice = kryds.getPrice() * roundedQuantity;
            PartslistMapper.insertPartslistLine(new Partslistline(kryds.getPartId(), currentOrderId, roundedQuantity, krydsListPrice, kryds.getDescription(), kryds.getUnit(), kryds.getLength(), kryds.getName()), connectionPool);

            // calculating and inserting Beams into Partslist
            double metersOfBeams = 2*carportLength;
            double beamsListPrice = raft.getPrice() * (metersOfBeams/100);
            PartslistMapper.insertPartslistLine(new Partslistline(raft.getPartId(), currentOrderId, 2, beamsListPrice, raft.getDescription(), raft.getUnit(), raft.getLength(), raft.getName()), connectionPool);

            // Updating the price for the Carport
            ArrayList<Partslistline> partslist = PartslistMapper.getPartsListByOrderid(currentOrderId, connectionPool);
            double sum = 0;
            for(Partslistline partslistline: partslist) {
                sum += partslistline.getPartlistlineprice();
            }
            System.out.println(""+sum);
            ctx.sessionAttribute("carportprice", sum);
            ctx.sessionAttribute("partslist", partslist);

            //creating the partlist and add it to a sessionattribut



            //ctx.sessionAttribute("showpartslist", true);

        } catch (DatabaseException e) {
            ctx.attribute("message", "Fejl i oprettelse af PartsListen!");
        }
    }
}