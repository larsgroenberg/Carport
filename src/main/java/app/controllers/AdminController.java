package app.controllers;

import app.entities.CarportPart;
import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.*;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class AdminController
{
    public static void addRoutes(Javalin app)
    {
        app.get("/adminSite", ctx -> {
            ctx.render("adminsite.html");
        });
        app.post("/getOrderByEmail", ctx -> {
            getOrderByEmail(ctx, ConnectionPool.getInstance());
            ctx.render("adminsite.html");
        });
        app.post("/getOrderByName", ctx -> {
            getOrderByName(ctx, ConnectionPool.getInstance());
            ctx.render("adminsite.html");
        });
        app.post("/getCustomerByName", ctx -> {
            getCustomerByName(ctx, ConnectionPool.getInstance());
            ctx.render("adminsite.html");
        });
        app.post("/getCustomerByEmail", ctx -> {
            getCustomerByEmail(ctx, ConnectionPool.getInstance());
            ctx.render("adminsite.html");
        });
        app.post("/getPartById", ctx -> {
            getPartById(ctx, ConnectionPool.getInstance());
            ctx.render("adminsite.html");
        });
        app.post("/changeOrderStatus", ctx -> {
            changeOrderStatusToProduced(ctx, ConnectionPool.getInstance());
            ctx.render("adminsite.html");
        });
        app.post("/orderPickedUp", ctx -> {
            changeOrderStatusToPickedUp(ctx, ConnectionPool.getInstance());
            ctx.render("adminsite.html");
        });
        app.post("/editPart", ctx -> {
            editCarportPart(ctx, ConnectionPool.getInstance());
            ctx.render("adminsite.html");
        });
        app.post("/updatePart", ctx -> {
            updatePart(ctx, ConnectionPool.getInstance());
            getPartsList(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("showallparts", true);
            ctx.render("adminsite.html");
        });
        app.post("/getAllParts", ctx -> {
            getPartsList(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("showallparts", true);
            ctx.render("adminsite.html");
        });
        app.post("/editOrder", ctx -> {
            editOrder(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("showorder", true);
            ctx.sessionAttribute("showallorders", true);
            ctx.render("adminsite.html");
        });
        app.post("/updateOrder", ctx -> {
            updateOrder(ctx, ConnectionPool.getInstance());
            getAllOrders(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("showallorders", true);
            ctx.render("adminsite.html");
        });
        app.post("/adminDeleteOrder", ctx -> {
            deleteOrder(ctx, ConnectionPool.getInstance());
            getAllOrders(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("showallorders", true);
            ctx.render("adminsite.html");
        });
        app.post("/getAllOrders", ctx -> {
            getAllOrders(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("showallparts", false);
            ctx.sessionAttribute("showallorders", true);
            ctx.render("adminsite.html");
        });
        app.post("/seeAllSale", ctx -> {
            seeAllSale(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("showallsale", true);
            ctx.sessionAttribute("showallorders", false);
            ctx.render("adminsite.html");
        });
        app.post("/lukmodal", ctx -> {
            ctx.sessionAttribute("modalmedbesked", false);
            ctx.render("adminsite.html");
        });
        app.post("/luksalg", ctx -> {
            ctx.sessionAttribute("showallsale", false);
            ctx.render("adminsite.html");
        });
    }
    private static void seeAllSale(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        ArrayList<Order> customerOrders = OrdersMapper.getAllOrders(connectionPool);
        ctx.sessionAttribute("customerorders", customerOrders);
        double totalSale = 0, totalCost = 0, totalRevenue = 0;
        int carportSold = 0;
        for(Order order: customerOrders) {
            if(order.getOrderStatus().equalsIgnoreCase("leveret")) {
                totalSale += order.getSalesPrice();
                totalCost += order.getMaterialCost();
                carportSold++;
            }
            totalRevenue = totalSale-totalCost;
        }
        // Her afrunder vi til nærmeste hele 10 øre
        totalSale = Math.round(totalSale * 10) / 10.0;
        totalCost = Math.round(totalCost * 10) / 10.0;
        totalRevenue = Math.round(totalRevenue * 10) / 10.0;
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");

        // Her konverterer vi variablerne til strenge så vi får dem med 2 decimaler
        String formattedTotalSale = decimalFormat.format(totalSale);
        String formattedTotalCost = decimalFormat.format(totalCost);
        String formattedTotalRevenue = decimalFormat.format(totalRevenue);

        ctx.sessionAttribute("totalSale", formattedTotalSale);
        ctx.sessionAttribute("totalCost", formattedTotalCost);
        ctx.sessionAttribute("totalRevenue", formattedTotalRevenue);
        ctx.sessionAttribute("carportSold", carportSold);
    }

    private static void editOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        Order order = OrdersMapper.getOrderByOrderId(orderId, connectionPool);
        ctx.sessionAttribute("order", order);
    }

    private static void deleteOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException, SQLException
    {
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        OrdersMapper.deleteOrderByOrderId(orderId, connectionPool);
    }

    private static void updateOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        Order order = ctx.sessionAttribute("order");
        order.setMaterialCost(Double.parseDouble(ctx.formParam("materialCost")));
        order.setSalesPrice(Double.parseDouble(ctx.formParam("salesPrice")));
        order.setCarportLength(Double.parseDouble(ctx.formParam("carportLength")));
        order.setCarportHeight(Double.parseDouble(ctx.formParam("carportHeight")));
        order.setCarportWidth(Double.parseDouble(ctx.formParam("carportWidth")));
        order.setUserId(Integer.parseInt(ctx.formParam("userId")));
        order.setOrderStatus(ctx.formParam("orderStatus"));
        order.setShedLength(Double.parseDouble(ctx.formParam("shedLength")));
        order.setShedWidth(Double.parseDouble(ctx.formParam("shedWidth")));
        order.setUserEmail(ctx.formParam("userEmail"));
        order.setOrderDate(ctx.formParam("orderDate"));
        order.setRoof(ctx.formParam("roof"));
        order.setWall(Boolean.parseBoolean(ctx.formParam("wall")));

        ctx.sessionAttribute("order", order);

        Order updatedOrder = ctx.sessionAttribute("order");
        OrdersMapper.updateOrder(updatedOrder, connectionPool);
        ctx.sessionAttribute("showallorders", true);
        ctx.sessionAttribute("showorder", false);
    }

    private static void updatePart(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        CarportPart part = ctx.sessionAttribute("part");
        part.setDBdescription(ctx.formParam("description"));
        part.setDBlength(Integer.parseInt(ctx.formParam("length")));
        part.setDBheight(Integer.parseInt(ctx.formParam("height")));
        part.setDBwidth(Integer.parseInt(ctx.formParam("width")));
        String type = ctx.formParam("type");
        CarportPart.CarportPartType partType;
        switch (type) {
            case "stolpe" -> partType = CarportPart.CarportPartType.STOLPE;
            case "spær" -> partType = CarportPart.CarportPartType.SPÆR;
            case "brædder" -> partType = CarportPart.CarportPartType.REM;
            case "hulbånd" -> partType = CarportPart.CarportPartType.HULBÅND;
            default -> partType = CarportPart.CarportPartType.TAGPLADER;
        }

        part.setType(partType);
        part.setDBmaterial(ctx.formParam("material"));
        part.setDBunit(ctx.formParam("unit"));
        part.setDBprice(Double.parseDouble(ctx.formParam("price")));
        ctx.sessionAttribute("part", part);

        CarportPart updatedPart = ctx.sessionAttribute("part");
        CarportPartMapper.updatePart(updatedPart, connectionPool);

        ctx.sessionAttribute("showallparts", true);
        ctx.sessionAttribute("showpart", false);
    }

    private static void editCarportPart(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        int partId = Integer.parseInt(ctx.formParam("partId"));
        CarportPart part = CarportPartMapper.getPartById(partId, connectionPool);
        ctx.sessionAttribute("part", part);
        ctx.sessionAttribute("showpart", true);
        ctx.sessionAttribute("showallparts", true);
    }

    private static void getPartsList(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        ArrayList<CarportPart> partList = CarportPartMapper.getDBParts(connectionPool);
        ctx.sessionAttribute("partslist", partList);
        System.out.println(partList.get(1).getType());
    }

    private static void getPartById(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        int partId = Integer.parseInt(ctx.formParam("partid"));
        CarportPart part = CarportPartMapper.getPartById(partId, connectionPool);
        ctx.sessionAttribute("part", part);
    }

    private static void getAllOrders(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        ArrayList<Order> customerOrders = OrdersMapper.getAllOrders(connectionPool);
        ctx.sessionAttribute("customerorders", customerOrders);
    }

    private static void getOrderByEmail(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        String email = ctx.formParam("email");
        Order customerOrder = OrdersMapper.getOrderByEmail(email, connectionPool);
        ctx.sessionAttribute("customerOrders", customerOrder);
    }

    private static void getOrderByName(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        User user = null;
        String userName = ctx.formParam("username_input");
        user = UserMapper.getCustomerByName(userName, connectionPool);
        if(user != null) {
            Order order = OrdersMapper.getOrderByUserId(user.getUserId(), connectionPool);
            System.out.println(""+order.getUserEmail());
            ctx.sessionAttribute("order", order);
            ctx.sessionAttribute("modalmedbesked", false);
            ctx.sessionAttribute("showorder", true);
        } else {
            String message = "Der findes ingen kunder med det navn i vores Database";
            ctx.sessionAttribute("message", message);
            ctx.sessionAttribute("modalmedbesked", true);
            ctx.sessionAttribute("showorder", false);
        }
    }

    private static void getCustomerByName(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        String userName = ctx.formParam("username");
        User currentUser = UserMapper.getCustomerByName(userName, connectionPool);
        ctx.sessionAttribute("currentuser", currentUser);
    }

    private static void getCustomerByEmail(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        String email = ctx.formParam("email");
        User currentUser = UserMapper.getCustomerByEmail(email, connectionPool);
        ctx.sessionAttribute("currentuser", currentUser);
    }

    private static void changeOrderStatusToProduced(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        OrdersMapper.changeStatusOnOrder("pakket", orderId, connectionPool);
        getAllOrders(ctx, connectionPool);
    }

    private static void changeOrderStatusToPickedUp(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        OrdersMapper.changeStatusOnOrder( "leveret", orderId, connectionPool);
        getAllOrders(ctx, connectionPool);
    }
}
