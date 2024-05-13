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
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import app.services.CarportSvg;

public class OrderController {
    private static Carport carport;
    private static Date today = new Date();
    private static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
    private static String formattedDate = formatter.format(today);

    private static Order order;


    public static void addRoutes(Javalin app) {
        app.get("/", ctx -> {
            ctx.render("index.html");
        });
        app.post("/createdrawing", ctx -> {
            showOrder(ctx);
            createPartsList(ctx, ConnectionPool.getInstance());
            ctx.render("showOrder.html");
        });
        app.post("/askforcredentials", ctx -> {
            ctx.sessionAttribute("showinputcredentials", true);
            ctx.render("showOrder.html");
        });

        app.post("/ordercarport", ctx -> {

            createPartsList(ctx, ConnectionPool.getInstance());
            ctx.render("showOrder.html");
        });

        app.post("/changeorder", ctx -> {
            ctx.render("index.html");
        });
        app.post("/closeinputmodal", ctx -> {
            ctx.sessionAttribute("showinputcredentials", false);
            ctx.render("showOrder.html");
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

    public static void createUser(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String name = ctx.formParam("name");
        String email = ctx.formParam("email");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");
        String address = ctx.formParam("address");
        String mobile = ctx.formParam("mobile");
        String zip = ctx.formParam("zip");

        boolean userexist = UserMapper.userexist(email, connectionPool);

        UserMapper.createuser(email, password1, name, mobile, address, zip, connectionPool);
        int userId = UserMapper.getUseridByEmail(email, connectionPool);
        order.setUserId(userId);
        order.setUserEmail(email);
        ctx.sessionAttribute("order", order);
        ctx.sessionAttribute("userId");
        ctx.sessionAttribute("email");
        /*if(password1.equals(password2)) {
            UserMapper.createuser(email, password1, name, mobile, address, zip, connectionPool);

        } else {
            ctx.sessionAttribute("passwordnotequal", false);
            ctx.render("showOrder.html");
        }*/


    }
    public static void showOrder(Context ctx) {
        double length = Double.parseDouble(ctx.formParam("length"));
        double width = Double.parseDouble(ctx.formParam("width"));
        double height = Double.parseDouble(ctx.formParam("height"));
        double length_shed = Double.parseDouble(ctx.formParam("length_shed"));
        double width_shed = Double.parseDouble(ctx.formParam("width_shed"));
        String roof = ctx.formParam("roof");
        int walls = Integer.parseInt(ctx.formParam("walls"));
        int orderId = 0;
        double materialCost = 0;
        double salesPrice = 0;
        int userId = 0;
        String orderStatus = "modtaget";
        String email = "";
        String orderDate = "";

        order = new Order(orderId, materialCost, salesPrice, width,length,height,userId,orderStatus, width_shed, length_shed, email, orderDate, roof, walls);
        ctx.sessionAttribute("order", order);

        Locale.setDefault(new Locale("US"));
        CarportSvg svgFromTop = new CarportSvg((int) width, (int) length, (int) height, (int)length_shed, (int)width_shed);
        CarportSvg svgFromSide = new CarportSvg((int) width, (int)length, (int)height, (int)length_shed, (int)width_shed, roof);
        ctx.sessionAttribute("svgFromTop", svgFromTop.toString());
        ctx.sessionAttribute("svgFromSide", svgFromSide.toString());
    }

    public static void createCarport(int userId, Context ctx, ConnectionPool connectionPool) throws DatabaseException {

        Order order = ctx.sessionAttribute("order");
        User user = ctx.sessionAttribute("user");
        String orderDate = formattedDate;
        int sidesOfWalls = ctx.sessionAttribute("wall");

        try {

            OrdersMapper.addOrder(order.getMaterialCost(), order.getSalesPrice(), order.getCarportWidth(), order.getCarportLength(), order.getCarportHeight(), userId, order.getOrderStatus(), order.getShedWidth(), order.getShedLength(), user.getEmail(), orderDate, order.getRoof(), sidesOfWalls, connectionPool);
            Order updatedorder = OrdersMapper.getOrderByEmail(order.getUserEmail(), connectionPool);
            ctx.sessionAttribute("order", updatedorder);

        } catch (DatabaseException e) {
            ctx.attribute("message", "Fejl i oprettelse af ordren!! Prøv igen!");
        }
    }

    public static void savePartsList(Context ctx, ConnectionPool connectionPool) throws  DatabaseException {
        ArrayList<Partslistline> partslist = ctx.sessionAttribute("partslist");
        for(Partslistline p: partslist) {
            PartslistMapper.insertPartslistLine(p, connectionPool);
        }
     }


    public static void createPartsList(Context ctx, ConnectionPool connectionPool) throws  DatabaseException {
        //double carportWidth = ctx.sessionAttribute("width");
        //double carportLength = ctx.sessionAttribute("length");
        //double carportHeight = ctx.sessionAttribute("height");
        //double shedWidth = ctx.sessionAttribute("width_shed");
        //double shedLength = ctx.sessionAttribute("length_shed");
        //int currentOrderId = ctx.sessionAttribute("current_order_id");
        //String roof = ctx.sessionAttribute("roof");
        //String email = ctx.sessionAttribute("email");
        Order order = ctx.sessionAttribute("order");
        ArrayList<Partslistline> partslistlines = new ArrayList<>();

        try {
            // calculating and inserting Posts into Partslist
            int numberOfPosts = PartsCalculator.calculateNumberOfPosts(order.getCarportWidth(), order.getCarportLength());
            double metersOfPosts = numberOfPosts*(order.getCarportHeight()+90);
            Part post = PartslistMapper.getPartByType("stolpe", connectionPool);
            double postListPrice = post.getPrice() * (metersOfPosts/100);
            partslistlines.add(new Partslistline(post.getPartId(), order.getOrderId(), numberOfPosts, postListPrice, post.getDescription(), post.getUnit(), post.getLength(), post.getName()));

            // calculating and inserting Rafts into Partslist
            int numberOfRafts = PartsCalculator.calculateNumberOfRafts(order.getCarportLength());
            double metersOfRafts = numberOfRafts*order.getCarportWidth();
            Part raft = PartslistMapper.getPartByTypeAndLength("spær", order.getCarportWidth(), connectionPool);
            double raftListPrice = raft.getPrice() * (metersOfRafts/100);
            partslistlines.add(new Partslistline(raft.getPartId(), order.getOrderId(), numberOfRafts, raftListPrice, raft.getDescription(), raft.getUnit(), raft.getLength(), raft.getName()));

            // calculating and inserting Roofmaterial into PartsList
            if(ctx.sessionAttribute("roof") != null) {
                double lengthOfRoofPlate = 360;
                int numberofRoofPlates = PartsCalculator.calculateNumberOfRoofPlates(order.getCarportLength(), order.getCarportWidth());
                Part roofPart = PartslistMapper.getPartByTypeAndLength("tagplader", lengthOfRoofPlate, connectionPool);
                double roofListPrice = roofPart.getPrice() * (numberofRoofPlates/100);
                partslistlines.add(new Partslistline(roofPart.getPartId(), order.getOrderId(), numberofRoofPlates, roofListPrice, roofPart.getDescription(), roofPart.getUnit(), roofPart.getLength(), roofPart.getName()));
            }

            // calculating and inserting vindkryds into PartsList
            double lenghtOfKryds = 4*(order.getCarportLength()/100);
            double quantityOfKryds = lenghtOfKryds/10;
            int roundedQuantity = (int) Math.ceil(quantityOfKryds);
            Part kryds = PartslistMapper.getPartByType("hulbånd", connectionPool);
            double krydsListPrice = kryds.getPrice() * roundedQuantity;
            partslistlines.add(new Partslistline(kryds.getPartId(), order.getOrderId(), roundedQuantity, krydsListPrice, kryds.getDescription(), kryds.getUnit(), kryds.getLength(), kryds.getName()));

            // calculating and inserting Beams into Partslist
            double metersOfBeams = 2*order.getCarportLength();
            double beamsListPrice = raft.getPrice() * (metersOfBeams/100);
            partslistlines.add(new Partslistline(raft.getPartId(), order.getOrderId(), 2, beamsListPrice, raft.getDescription(), raft.getUnit(), raft.getLength(), raft.getName()));

            // Updating the price for the Carport
            double materialCostPrice = 0;
            for(Partslistline partslistline: partslistlines) {
                materialCostPrice += partslistline.getPartlistlineprice();
            }

            // Her sætter vi salgsprisen med en avance på 40%, runder af så der kun er 2 decimaler og gemmer dem i order-instansen
            String afrundetKostPris = String.format("%.2f", materialCostPrice);
            String afrundetSalgspris = String.format("%.2f", (materialCostPrice*1.4));
            double salesPrice = Double.parseDouble(afrundetSalgspris);
            double costPrice = Double.parseDouble(afrundetKostPris);
            order.setSalesPrice((salesPrice));
            order.setMaterialCost(costPrice);

            // Her opdaterer vi order-sessionattributten med salgspris og materialekostpris
            ctx.sessionAttribute("order", order);

            // Her sætter vi sessionattributten partslist med de udregninger vi har lavet
            ctx.sessionAttribute("partslist", partslistlines);

        } catch (DatabaseException e) {
            ctx.attribute("message", "Fejl i oprettelse af PartsListen!");
        }
    }
}