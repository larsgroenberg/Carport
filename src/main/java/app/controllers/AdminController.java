package app.controllers;

import app.entities.CarportPart;
import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.*;
import app.services.EmailService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class AdminController {
    public static void addRoutes(Javalin app) {
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
        app.post("/addPart", ctx -> {
            //addPart(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("addPart", true);
            ctx.render("adminsite.html");
        });

        app.post("/closeaddpartformular", ctx -> {
            ctx.sessionAttribute("addPart", false);
            ctx.render("adminsite.html");
        });
        app.post("/addNewPart", ctx -> {
            addPart(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("addPart", false);
            ctx.render("adminsite.html");
        });
        app.post("/changeOrderStatus", ctx -> {
            int orderId = changeOrderStatusToProduced(ctx, ConnectionPool.getInstance());
            Order order = OrdersMapper.getOrderByOrderId(orderId, ConnectionPool.getInstance());
            EmailService.sendCarportReadyEmail(order);
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
            ctx.sessionAttribute("addPart", false);
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
            ctx.sessionAttribute("addPart", false);
            ctx.sessionAttribute("showallparts", false);
            ctx.sessionAttribute("showallorders", true);
            ctx.render("adminsite.html");
        });
        app.post("/seeAllSale", ctx -> {
            seeAllSale(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("showallsale", true);
            ctx.sessionAttribute("addPart", false);
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

    private static void seeAllSale(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Order> customerOrders = OrdersMapper.getAllOrders(connectionPool);
        ctx.sessionAttribute("customerorders", customerOrders);
        double totalSale = 0, totalCost = 0, totalRevenue = 0;
        int carportSold = 0;
        for (Order order : customerOrders) {
            if (order.getOrderStatus().equalsIgnoreCase("leveret")) {
                totalSale += order.getSalesPrice();
                totalCost += order.getMaterialCost();
                carportSold++;
            }
            totalRevenue = totalSale - totalCost;
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

    private static void editOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        Order order = OrdersMapper.getOrderByOrderId(orderId, connectionPool);
        ctx.sessionAttribute("order", order);
    }

    private static void deleteOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException, SQLException {
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        OrdersMapper.deleteOrderByOrderId(orderId, connectionPool);
    }

    private static void updateOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
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

    private static void updatePart(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
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
            case "remme" -> partType = CarportPart.CarportPartType.REM;
            case "hulbånd" -> partType = CarportPart.CarportPartType.HULBÅND;
            case "tagplader" -> partType = CarportPart.CarportPartType.TAGPLADER;
            case "brædder" -> partType = CarportPart.CarportPartType.BRÆDDER;
            case "skurbrædt" -> partType = CarportPart.CarportPartType.SKURBRÆDT;
            case "understern" -> partType = CarportPart.CarportPartType.UNDERSTERN;
            case "overstern" -> partType = CarportPart.CarportPartType.OVERSTERN;
            case "vandbrædder" -> partType = CarportPart.CarportPartType.VANDBRÆDDER;
            case "reglar" -> partType = CarportPart.CarportPartType.REGLAR;
            case "lægte" -> partType = CarportPart.CarportPartType.LÆGTE;
            case "universalbeslag" -> partType = CarportPart.CarportPartType.UNIVERSALBESLAG;
            case "skruer" -> partType = CarportPart.CarportPartType.SKRUER;
            case "bundskruer" -> partType = CarportPart.CarportPartType.BUNDSKRUER;
            case "bolte" -> partType = CarportPart.CarportPartType.BOLTE;
            case "vinkelbeslag" -> partType = CarportPart.CarportPartType.VINKELBESLAG;
            case "firkantskiver" -> partType = CarportPart.CarportPartType.FIRKANTSKIVER;
            case "hængsel" -> partType = CarportPart.CarportPartType.HÆNGSEL;
            default -> partType = CarportPart.CarportPartType.NONE;
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

    private static void addPart(Context ctx, ConnectionPool connectionPool) throws DatabaseException {

        String description = ctx.formParam("description");
        int partLength = Integer.parseInt(ctx.formParam("length"));
        int partHeigth = Integer.parseInt(ctx.formParam("height"));
        int partWidth = Integer.parseInt(ctx.formParam("width"));
        String type = ctx.formParam("type");
        CarportPart.CarportPartType partType;
        switch (type) {
            case "stolpe" -> partType = CarportPart.CarportPartType.STOLPE;
            case "spær" -> partType = CarportPart.CarportPartType.SPÆR;
            case "remme" -> partType = CarportPart.CarportPartType.REM;
            case "hulbånd" -> partType = CarportPart.CarportPartType.HULBÅND;
            case "tagplader" -> partType = CarportPart.CarportPartType.TAGPLADER;
            case "brædder" -> partType = CarportPart.CarportPartType.BRÆDDER;
            case "skurbrædt" -> partType = CarportPart.CarportPartType.SKURBRÆDT;
            case "understern" -> partType = CarportPart.CarportPartType.UNDERSTERN;
            case "overstern" -> partType = CarportPart.CarportPartType.OVERSTERN;
            case "vandbrædder" -> partType = CarportPart.CarportPartType.VANDBRÆDDER;
            case "reglar" -> partType = CarportPart.CarportPartType.REGLAR;
            case "lægte" -> partType = CarportPart.CarportPartType.LÆGTE;
            case "universalbeslag" -> partType = CarportPart.CarportPartType.UNIVERSALBESLAG;
            case "skruer" -> partType = CarportPart.CarportPartType.SKRUER;
            case "bundskruer" -> partType = CarportPart.CarportPartType.BUNDSKRUER;
            case "bolte" -> partType = CarportPart.CarportPartType.BOLTE;
            case "vinkelbeslag" -> partType = CarportPart.CarportPartType.VINKELBESLAG;
            case "firkantskiver" -> partType = CarportPart.CarportPartType.FIRKANTSKIVER;
            case "hængsel" -> partType = CarportPart.CarportPartType.HÆNGSEL;
            default -> partType = CarportPart.CarportPartType.NONE;
        }
        partType = CarportPart.CarportPartType.valueOf(ctx.formParam("type"));
        String partName = ctx.formParam("name");
        String partMaterial = ctx.formParam("material");
        String partUnit = ctx.formParam("unit");
        double partPrice = Double.parseDouble(ctx.formParam("price"));
        CarportPart part = new CarportPart(0, partType, 0, partPrice, partLength, partHeigth, partWidth, description, partMaterial, partUnit, partName, String.valueOf(partType));

        ctx.sessionAttribute("part", part);

        part = ctx.sessionAttribute("part");
        CarportPartMapper.addPart(part, connectionPool);

    }

    private static void editCarportPart(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int partId = Integer.parseInt(ctx.formParam("partId"));
        CarportPart part = CarportPartMapper.getPartById(partId, connectionPool);
        ctx.sessionAttribute("part", part);
        ctx.sessionAttribute("showpart", true);
        ctx.sessionAttribute("showallparts", true);
    }

    private static void getPartsList(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<CarportPart> partList = CarportPartMapper.getDBParts(connectionPool);
        ctx.sessionAttribute("partslist", partList);
        System.out.println(partList.get(1).getType());
    }

    private static void getPartById(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int partId = Integer.parseInt(ctx.formParam("partid"));
        CarportPart part = CarportPartMapper.getPartById(partId, connectionPool);
        ctx.sessionAttribute("part", part);
    }

    private static void getAllOrders(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Order> customerOrders = OrdersMapper.getAllOrders(connectionPool);
        ctx.sessionAttribute("customerorders", customerOrders);
    }

    private static void getOrderByEmail(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String email = ctx.formParam("email");
        Order customerOrder = OrdersMapper.getOrderByEmail(email, connectionPool);
        ctx.sessionAttribute("customerOrders", customerOrder);
    }

    private static void getOrderByName(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        User user = null;
        String userName = ctx.formParam("username_input");
        user = UserMapper.getCustomerByName(userName, connectionPool);
        if (user != null) {
            Order order = OrdersMapper.getOrderByUserId(user.getUserId(), connectionPool);
            System.out.println(order.getUserEmail());
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

    private static void getCustomerByName(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String userName = ctx.formParam("username");
        User currentUser = UserMapper.getCustomerByName(userName, connectionPool);
        ctx.sessionAttribute("currentuser", currentUser);
    }

    private static void getCustomerByEmail(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String email = ctx.formParam("email");
        User currentUser = UserMapper.getCustomerByEmail(email, connectionPool);
        ctx.sessionAttribute("currentuser", currentUser);
    }

    private static int changeOrderStatusToProduced(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        OrdersMapper.changeStatusOnOrder("pakket", orderId, connectionPool);
        getAllOrders(ctx, connectionPool);
        return orderId;
    }

    private static void changeOrderStatusToPickedUp(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        OrdersMapper.changeStatusOnOrder("leveret", orderId, connectionPool);
        getAllOrders(ctx, connectionPool);
    }
}
