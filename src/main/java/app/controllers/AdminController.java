package app.controllers;

import app.entities.CarportPart;
import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.ArrayList;

public class AdminController
{
    public static void addRoutes(Javalin app)
    {
        app.get("/adminSite", ctx -> index(ctx));
        app.post("/getOrderByEmail", ctx -> getOrderByEmail(ctx, ConnectionPool.getInstance()));
        app.post("/getOrderByName", ctx -> getOrderByName(ctx, ConnectionPool.getInstance()));
        app.post("/getCustomerByName", ctx -> getCustomerByName(ctx, ConnectionPool.getInstance()));
        app.post("/getCustomerByEmail", ctx -> getCustomerByEmail(ctx, ConnectionPool.getInstance()));
        app.post("/getPartById", ctx -> getPartById(ctx, ConnectionPool.getInstance()));

        app.post("/changeorderstatus", ctx -> {
            changeOrderStatusToProduced(ctx, ConnectionPool.getInstance());
            ctx.render("adminsite.html");
        });
        app.post("/orderpickedup", ctx -> {
            changeOrderStatusToPickedUp(ctx, ConnectionPool.getInstance());
            ctx.render("adminsite.html");
        });
        app.post("/editPart", ctx -> {
            edittask(ctx, ConnectionPool.getInstance());
            ctx.render("adminSite.html");
        });
        app.post("/updatepart", ctx -> {
            updatepart(ctx, ConnectionPool.getInstance());
            showPartsList(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("showallparts", true);
            ctx.render("adminSite.html");
        });
        app.post("/getAllParts", ctx -> {
            showPartsList(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("showallparts", true);
            ctx.render("adminSite.html");
        });
        app.post("/getAllOrders", ctx -> {
            getAllOrders(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("showallparts", false);
            ctx.render("adminSite.html");
        });
        app.post("/lukmodal", ctx -> {
            ctx.sessionAttribute("modalmedbesked", false);
            ctx.render("adminSite.html");
        });


    }

    private static void updatepart(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String description = ctx.formParam("description");
        int length = Integer.parseInt(ctx.formParam("length"));
        int height = Integer.parseInt(ctx.formParam("height"));
        int width = Integer.parseInt(ctx.formParam("width"));
        String type = ctx.formParam("type");
        String material = ctx.formParam("material");
        String unit = ctx.formParam("unit");
        int price = Integer.parseInt(ctx.formParam("price"));
        CarportPart part = ctx.sessionAttribute("part");
        part.setDBdescription(description);
        part.setDBlength(length);
        part.setDBheight(height);
        part.setDBwidth(width);
        CarportPart.CarportPartType partType;
        switch (type) {
            case "stolpe" -> partType = CarportPart.CarportPartType.SUPPORTPOST;
            case "spær" -> partType = CarportPart.CarportPartType.RAFT;
            case "brædder" -> partType = CarportPart.CarportPartType.BEAM;
            case "hulbånd" -> partType = CarportPart.CarportPartType.CROSSSUPPORT;
            default -> partType = CarportPart.CarportPartType.ROOFTILE;
        }

        part.setType(partType);

        part.setDBmaterial(material);
        part.setDBunit(unit);
        part.setDBprice(price);
        ctx.sessionAttribute("part", part);

        CarportPart updatedPart = ctx.sessionAttribute("part");
        CarportPartMapper.updatePart(updatedPart, connectionPool);

        ctx.sessionAttribute("showallparts", true);
        ctx.sessionAttribute("showpart", false);
    }

    private static void edittask(Context ctx, ConnectionPool connectionPool) {
        try {
            int partId = Integer.parseInt(ctx.formParam("partId"));
            CarportPart part = CarportPartMapper.getPartById(partId, connectionPool);
            ctx.sessionAttribute("part", part);
            ctx.sessionAttribute("showpart", true);
            ctx.sessionAttribute("showallparts", true);
            ctx.render("adminSite.html");

        } catch (DatabaseException | NumberFormatException e) {
            ctx.attribute("message", e.getMessage());
            ctx.render("index.html");
        }
    }

    private static void index(Context ctx)
    {
        ctx.render("adminsite.html");
    }

    private static void showPartsList(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        //CarportPartMapper.getAllParts(connectionPool);
        ArrayList<CarportPart> partList = CarportPartMapper.getDBParts(connectionPool);
        ctx.attribute("partslist", partList);
        ctx.render("adminsite.html");
    }

    private static void getPartById(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int partId = Integer.parseInt(ctx.formParam("partid"));
        CarportPart part = CarportPartMapper.getPartById(partId, connectionPool);
        ctx.attribute("part", part);
        ctx.render("adminsite.html");
    }

    private static void getAllOrders(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Order> customerOrders = OrdersMapper.getAllOrders(connectionPool);
        ctx.attribute("customerOrders", customerOrders);
        ctx.render("adminsite.html");
    }

    private static void getOrderByEmail(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String email = ctx.formParam("email");
        Order customerOrder = OrdersMapper.getOrderByEmail(email, connectionPool);
        ctx.attribute("customerOrders", customerOrder);
        ctx.render("adminsite.html");
    }

    private static void getOrderByName(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String userName = ctx.formParam("username");
        User user = UserMapper.getCustomerByName(userName, connectionPool);
        if(user != null) {
            Order customerOrder = OrdersMapper.getOrderByUserId(user.getUserId(), connectionPool);
            ctx.attribute("customerOrders", customerOrder);
            System.out.println("ingen kunde");
        } else {
            String message = "Der findes ingen kunder med det navn i vores Database";
            ctx.attribute("message", message);
            ctx.attribute("modalmedbesked", true);
        }
        ctx.render("adminSite.html");
    }

    private static void getCustomerByName(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String userName = ctx.formParam("username");
        User currentUser = UserMapper.getCustomerByName(userName, connectionPool);
        ctx.attribute("currentuser", currentUser);
        ctx.render("adminsite.html");
    }

    private static void getCustomerByEmail(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String email = ctx.formParam("email");
        User currentUser = UserMapper.getCustomerByEmail(email, connectionPool);
        ctx.attribute("currentuser", currentUser);
        ctx.render("adminsite.html");
    }

    private static void changeOrderStatusToProduced(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String orderStatus = "produceret";
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        OrdersMapper.changeStatusOnOrder(orderStatus, orderId, connectionPool);
        getAllOrders(ctx, connectionPool);
    }

    private static void changeOrderStatusToPickedUp(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String orderStatus = "leveret";
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        OrdersMapper.changeStatusOnOrder(orderStatus, orderId, connectionPool);
        getAllOrders(ctx, connectionPool);
    }


}
