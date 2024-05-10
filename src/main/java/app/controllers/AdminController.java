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
        app.post("/getAllOrders", ctx -> getAllOrders(ctx, ConnectionPool.getInstance()));
        app.post("/getOrderByEmail", ctx -> getOrderByEmail(ctx, ConnectionPool.getInstance()));
        app.post("/getOrderByName", ctx -> getOrderByName(ctx, ConnectionPool.getInstance()));
        app.post("/getCustomerByName", ctx -> getCustomerByName(ctx, ConnectionPool.getInstance()));
        app.post("/getCustomerByEmail", ctx -> getCustomerByEmail(ctx, ConnectionPool.getInstance()));
        app.post("/getPartById", ctx -> getPartById(ctx, ConnectionPool.getInstance()));
        app.post("/getAllParts", ctx -> showPartsList(ctx, ConnectionPool.getInstance()));
        app.post("/changeorderstatus", ctx -> {
            changeOrderStatusToProduced(ctx, ConnectionPool.getInstance());
            ctx.render("adminsite.html");
        });
        app.post("/orderpickedup", ctx -> {
            changeOrderStatusToPickedUp(ctx, ConnectionPool.getInstance());
            ctx.render("adminsite.html");
        });
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
        Order customerOrder = OrdersMapper.getOrderByName(userName, connectionPool);
        ctx.attribute("customerOrders", customerOrder);
        ctx.render("adminsite.html");
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
