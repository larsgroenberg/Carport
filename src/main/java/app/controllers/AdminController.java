package app.controllers;

import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.*;
import app.services.CarportSvg;
import app.services.EmailService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

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
            ctx.sessionAttribute("showallorders", true);
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
            ctx.sessionAttribute("showorder", false);
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

    private static void seeAllSale(Context ctx, ConnectionPool connectionPool) {
        try {
            ArrayList<Order> customerOrders = OrdersMapper.getAllOrders(connectionPool); // Denne linje kan kaste en DatabaseException
            if (customerOrders == null) {
                throw new NullPointerException("Order list is null.");
            }
            ctx.sessionAttribute("customerorders", customerOrders);

            double totalSale = 0, totalCost = 0;
            int carportSold = 0;

            for (Order order : customerOrders) {
                if (order.getOrderStatus().equalsIgnoreCase("leveret")) {
                    totalSale += order.getSalesPrice();
                    totalCost += order.getMaterialCost();
                    carportSold++;
                }
            }

            double totalRevenue = totalSale - totalCost;

            // Afrunder til nærmeste hele 10 øre (1 decimal)
            totalSale = Math.round(totalSale * 10) / 10.0;
            totalCost = Math.round(totalCost * 10) / 10.0;
            totalRevenue = Math.round(totalRevenue * 10) / 10.0;

            DecimalFormat decimalFormat = new DecimalFormat("#0.00");

            // Formaterer værdierne så de har 2 decimaler
            String formattedTotalSale = decimalFormat.format(totalSale);
            String formattedTotalCost = decimalFormat.format(totalCost);
            String formattedTotalRevenue = decimalFormat.format(totalRevenue);

            ctx.sessionAttribute("totalSale", formattedTotalSale);
            ctx.sessionAttribute("totalCost", formattedTotalCost);
            ctx.sessionAttribute("totalRevenue", formattedTotalRevenue);
            ctx.sessionAttribute("carportSold", carportSold);

        } catch (DatabaseException e) {
            // I tilfælde af at der opstår en Databaseexception i getAllOrders kastes der en DatabaseException
            System.err.println("Fejl ved hentning af ordrer: " + e.getMessage());
            ctx.sessionAttribute("error", "Fejl ved hentning af ordrer: " + e.getMessage());
        } catch (NullPointerException e) {
            //Hvis CustomerOrders er null, altså der ikke er nogle ordre i systemet kastes der en NullpointerException
            System.err.println("Null reference fejl: " + e.getMessage());
            ctx.sessionAttribute("error", "Ingen ordrer at hente: " + e.getMessage());
        } catch (Exception e) {
            // Her håndterer jeg eventuelle andre uventede undtagelser
            System.err.println("Uventet fejl: " + e.getMessage());
            ctx.sessionAttribute("error", "Uventet fejl: " + e.getMessage());
        }
    }

    private static void editOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        Order order;
        try {
            // Her forsøger jeg at hente ordren fra databasen
            order = OrdersMapper.getOrderByOrderId(orderId, connectionPool);
        } catch (DatabaseException e) {
            // Hej håndterer jeg en evt. databasefejl
            System.err.println("Database error: " + e.getMessage());
            ctx.sessionAttribute("error", "Database error: " + e.getMessage());
            return;  // Her afbryder jeg metoden, hvis der opstår en databasefejl da sessionAtributten ikke kan sættes hvis order er null
        }
        ctx.sessionAttribute("order", order);
    }

    private static void deleteOrder(Context ctx, ConnectionPool connectionPool) {
        // Den ydre try-catch blok som omslutter hele metoden er for at fange og håndtere eventuelle andre uventede undtagelser
        try {
            int orderId = Integer.parseInt(ctx.formParam("orderId"));
            Order order = OrdersMapper.getOrderByOrderId(orderId, connectionPool);
            int userId = order.getUserId();

            try {
                // Her forsøger jeg at slette ordren i databasen
                OrdersMapper.deleteOrderByOrderId(orderId, connectionPool);
            } catch (DatabaseException e) {
                // Her håndterer jeg en evt. databasefejl eller SQL-fejl
                System.err.println("Database fejl ved forsøg på at slette ordren: " + e.getMessage());
                ctx.sessionAttribute("error", "Database error: " + e.getMessage());
            }

            // Her sletter jeg den partslist der hører til den specifikke ordre
            try {
                OrdersMapper.deleteUsersPartslistByOrderId(orderId, connectionPool);
            } catch (DatabaseException e) {
                // Her håndterer jeg en evt. databasefejl eller SQL-fejl
                System.err.println("Database fejl ved forsøg på sletning af kundens partsList: " + e.getMessage());
                ctx.sessionAttribute("error", "Database error: " + e.getMessage());
            }

            // Her forsøger jeg at slette brugeren
            try {
                UserMapper.deleteUserByUserId(userId, connectionPool);
            } catch (DatabaseException e) {
                // Her håndterer jeg en evt. databasefejl eller SQL-fejl
                System.err.println("Database fejl ved forsøg på sletning af user: " + e.getMessage());
                ctx.sessionAttribute("error", "Database error: " + e.getMessage());
            }
        } catch (Exception e) {
            // Her håndterer jeg alle andre uventede undtagelser
            System.err.println("Uventet fejl: " + e.getMessage());
            ctx.sessionAttribute("error", "Uventet fejl: " + e.getMessage());
        }
    }


    private static void updateOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        // Her henter jeg de opdaterede data og parser dem
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        Order order = OrdersMapper.getOrderByOrderId(orderId, connectionPool);

        double materialCost = Double.parseDouble(ctx.formParam("materialCost"));
        double salesPrice = Double.parseDouble(ctx.formParam("salesPrice"));
        double carportLength = Double.parseDouble(ctx.formParam("carportLength"));
        double carportHeight = Double.parseDouble(ctx.formParam("carportHeight"));
        double carportWidth = Double.parseDouble(ctx.formParam("carportWidth"));
        int userId = Integer.parseInt(ctx.formParam("userId"));
        String orderStatus = ctx.formParam("orderStatus");
        double shedLength = Double.parseDouble(ctx.formParam("shedLength"));
        double shedWidth = Double.parseDouble(ctx.formParam("shedWidth"));
        String userEmail = ctx.formParam("userEmail");
        String orderDate = ctx.formParam("orderDate");
        String roof = ctx.formParam("roof");
        boolean wall = Boolean.parseBoolean(ctx.formParam("wall"));

        // Her opretter jeg et nyt carport-objekt baseret på de nye data
        Order updatedOrder = new Order(orderId, materialCost, salesPrice, carportWidth, carportLength, carportHeight, userId, orderStatus, shedWidth, shedLength, userEmail, orderDate, roof, wall);

        // Her kontrollerer jeg om der er ændringer til carport og evt. skur
        if (order.getCarportHeight() != updatedOrder.getCarportHeight() ||
                order.getCarportLength() != updatedOrder.getCarportLength() ||
                order.getCarportWidth() != updatedOrder.getCarportWidth() ||
                order.getShedLength() != updatedOrder.getShedLength() ||
                order.getShedWidth() != updatedOrder.getShedWidth() ||
                !order.getRoof().equalsIgnoreCase(updatedOrder.getRoof())) {

            try {
                // Her sletter jeg brugerens partslist og genberegner totalprisen hvis der er ændringer til carport og skur
                OrdersMapper.deleteUsersPartslistByOrderId(order.getOrderId(), connectionPool);
                double totalPrice = calculateAndCreateNewOrderAndPartslist(order.getOrderId(), updatedOrder, ctx, connectionPool);
                updatedOrder.setMaterialCost(totalPrice);
                updatedOrder.setSalesPrice(ctx.sessionAttribute("carportprice"));
            } catch (DatabaseException e) {
                // Her håndterer jeg databasefejl ved sletning og genberegning
                System.err.println("Database error: " + e.getMessage());
                ctx.sessionAttribute("error", "Database error: " + e.getMessage());
                return;  // Her afbryder jeg metoden ved fejl
            }
        }

        // Her sætter jeg session attributten order med de nye værdier
        ctx.sessionAttribute("order", updatedOrder);

        // Her opdaterer jeg ordren i databasen
        try {
            OrdersMapper.updateOrder(updatedOrder, connectionPool);
        } catch (DatabaseException e) {
            // Her håndterer jeg en evt. databasefejl ved opdateringen
            System.err.println("Database error: " + e.getMessage());
            ctx.sessionAttribute("error", "Database error: " + e.getMessage());
        }
    }

    public static double calculateAndCreateNewOrderAndPartslist(int orderId, Order updatedOrder, Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        double length = updatedOrder.getCarportLength();
        double width = updatedOrder.getCarportWidth();
        double height = updatedOrder.getCarportHeight();
        double lengthShed = updatedOrder.getShedLength();
        double widthShed = updatedOrder.getShedWidth();

        double postLength = height + 90;
        String roof = updatedOrder.getRoof();
        boolean withRoof = !updatedOrder.getRoof().contains("Uden");
        boolean withShed = lengthShed > 0;

        Carport carport = new Carport(length, width, height, withRoof, withShed, lengthShed, widthShed, 0);

        // Her sætter jeg session attributter til brug ved beregning i PartsCalculator
        ctx.sessionAttribute("carport_trapeztag", roof);
        ctx.sessionAttribute("carport_width", (int) width);
        ctx.sessionAttribute("carport_length", (int) length);
        ctx.sessionAttribute("postlength", (int) postLength);
        if (lengthShed > 0) {
            ctx.sessionAttribute("shed", true);
        }

        // Her opretter jeg en ArrayListe med carportdelene fra databasen
        ArrayList<CarportPart> dbPartsList;
        try {
            dbPartsList = CarportPartMapper.getDBParts(connectionPool);
        } catch (DatabaseException e) {
            throw new DatabaseException("Fejl ved hentning af carportdele fra databasen: " + e.getMessage(), String.valueOf(e));
        }

        ctx.sessionAttribute("newdbPartsList", dbPartsList);
        ctx.sessionAttribute("dbPartsList", dbPartsList);

        // Her indstiller jeg locale
        Locale.setDefault(new Locale("da-DK"));

        // Her laver jeg de 2 tegninger svgFromTop og svgFromSide
        CarportSvg svgFromTop = new CarportSvg(ctx, (int) width, (int) length, (int) height, lengthShed, widthShed);
        CarportSvg svgFromSide = new CarportSvg(ctx, (int) width, (int) length, (int) height, lengthShed, widthShed, roof);

        ctx.sessionAttribute("totalPoles", svgFromTop.getTotalPoles());
        ctx.sessionAttribute("totalBeams", svgFromTop.getTotalBeams());
        ctx.sessionAttribute("totalRafters", svgFromTop.getTotalRafters());
        ctx.sessionAttribute("totalCrossSupports", svgFromTop.getTotalCrossSupports());
        ctx.sessionAttribute("totalBoards", svgFromSide.getTotalBoards());

        ctx.sessionAttribute("svgFromTop", svgFromTop.toString());
        ctx.sessionAttribute("svgFromSide", svgFromSide.toString());

        double totalPrice = 0.0;

        // Her beregner jeg delene der skal bruges gemmer i calculateCarport-metoden partsList
        PartsCalculator partsCalculator = new PartsCalculator(carport, dbPartsList);
        partsCalculator.calculateCarport(ctx);

        // Her henter jeg partsListen fra sessionAtributten
        ArrayList<CarportPart> partsList = ctx.sessionAttribute("partsList");
        for (CarportPart part : partsList) {
            totalPrice += (part.getQuantity() * part.getDBprice());
        }
        totalPrice = Math.ceil(totalPrice);
        double carportTotalPrice = Math.ceil(totalPrice * 1.4);
        ctx.sessionAttribute("carportprice", carportTotalPrice);
        carport.setPrice(totalPrice);
        ctx.sessionAttribute("newCarport", carport);

        // Indsæt de nødvendige dele for ordren i databasen
        try {
            OrdersMapper.insertPartsNeededForOrder(orderId, ctx, connectionPool);
        } catch (DatabaseException e) {
            throw new DatabaseException("Fejl ved indsættelse af dele til ordren: " + e.getMessage(), String.valueOf(e));
        }

        return totalPrice;
    }

    private static void updatePart(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        CarportPart part = ctx.sessionAttribute("part");
        part.setDBdescription(ctx.formParam("description"));
        part.setDBname(ctx.formParam("name"));
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
        // Her forsøger jeg at opdatere delen i databasen
        try {
            CarportPartMapper.updatePart(updatedPart, connectionPool);
        } catch (DatabaseException e) {
            // Her håndterer jeg en evt. databasefejl
            System.out.println("Database error: " + e.getMessage());
            ctx.sessionAttribute("error", "Database error: " + e.getMessage());
            return;  // Afbryder metoden, hvis der opstår en databasefejl
        }

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
        String partName = ctx.formParam("name");
        String partMaterial = ctx.formParam("material");
        String partUnit = ctx.formParam("unit");
        double partPrice = Double.parseDouble(ctx.formParam("price"));
        CarportPart part = new CarportPart(0, partType, 0, partPrice, partLength, partHeigth, partWidth, description, partMaterial, partUnit, partName, String.valueOf(partType));

        ctx.sessionAttribute("part", part);

        try {
            CarportPartMapper.addPart(part, connectionPool);
        } catch (DatabaseException e) {
            // Her håndterer jeg en evt. databasefejl
            System.out.println("Database error: " + e.getMessage());
            ctx.sessionAttribute("error", "Database error: " + e.getMessage());
        }
    }

    private static void editCarportPart(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int partId = Integer.parseInt(ctx.formParam("partId"));
        CarportPart part;
        try {
            part = CarportPartMapper.getPartById(partId, connectionPool);
        } catch (DatabaseException e) {
            // Her håndterer jeg en evt. databasefejl
            System.out.println("Database error: " + e.getMessage());
            ctx.sessionAttribute("error", "Database error: " + e.getMessage());
            return;  // Afbryder metoden, hvis der opstår en databasefejl
        }
        ctx.sessionAttribute("part", part);
        ctx.sessionAttribute("showpart", true);
        ctx.sessionAttribute("showallparts", true);
    }

    private static void getPartsList(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<CarportPart> partList;
        try {
            partList = CarportPartMapper.getDBParts(connectionPool);
        } catch (DatabaseException e) {
            // Her håndterer jeg en evt. databasefejl
            System.out.println("Database error: " + e.getMessage());
            ctx.sessionAttribute("error", "Database error: " + e.getMessage());
            return;  // Afbryder metoden, hvis der opstår en databasefejl
        }
        ctx.sessionAttribute("partslist", partList);
        System.out.println(partList.get(1).getType());
    }

    private static void getPartById(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int partId = Integer.parseInt(ctx.formParam("partid"));
        CarportPart part;
        try {
            part = CarportPartMapper.getPartById(partId, connectionPool);
        } catch (DatabaseException e) {
            // Her håndterer jeg en evt. databasefejl
            System.out.println("Database error: " + e.getMessage());
            ctx.sessionAttribute("error", "Database error: " + e.getMessage());
            return;  // Afbryder metoden, hvis der opstår en databasefejl
        }
        ctx.sessionAttribute("part", part);
    }

    private static void getAllOrders(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Order> customerOrders;
        try {
            customerOrders = OrdersMapper.getAllOrders(connectionPool);
        } catch (DatabaseException e) {
            // Her håndterer jeg en evt. databasefejl
            System.out.println("Database error: " + e.getMessage());
            ctx.sessionAttribute("error", "Database error: " + e.getMessage());
            return;  // Afbryder metoden, hvis der opstår en databasefejl
        }
        ctx.sessionAttribute("customerorders", customerOrders);
    }

    private static void getOrderByEmail(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String email = ctx.formParam("email");
        Order customerOrder;
        try {
            customerOrder = OrdersMapper.getOrderByEmail(email, connectionPool);
        } catch (DatabaseException e) {
            // Her håndterer jeg en evt. databasefejl
            System.out.println("Database error: " + e.getMessage());
            ctx.sessionAttribute("error", "Database error: " + e.getMessage());
            return;  // Afbryder metoden, hvis der opstår en databasefejl
        }
        ctx.sessionAttribute("customerOrders", customerOrder);
    }

    private static void getOrderByName(Context ctx, ConnectionPool connectionPool) {
        String userName = ctx.formParam("username_input");

        if (userName == null || userName.trim().isEmpty()) {
            String message = "Brugernavn er påkrævet.";
            ctx.sessionAttribute("message", message);
            ctx.sessionAttribute("modalmedbesked", true);
            ctx.sessionAttribute("showorder", false);
            return;
        }

        try {
            User user = UserMapper.getCustomerByName(userName, connectionPool);
            if (user != null) {
                Order order = OrdersMapper.getOrderByUserId(user.getUserId(), connectionPool);
                if (order != null) {
                    System.out.println(order.getUserEmail());
                    ctx.sessionAttribute("order", order);
                    ctx.sessionAttribute("modalmedbesked", false);
                    ctx.sessionAttribute("showorder", true);
                } else {
                    String message = "Ingen ordre fundet for den angivne bruger.";
                    ctx.sessionAttribute("message", message);
                    ctx.sessionAttribute("modalmedbesked", true);
                    ctx.sessionAttribute("showorder", false);
                }
            } else {
                String message = "Der findes ingen kunder med det navn i vores Database.";
                ctx.sessionAttribute("message", message);
                ctx.sessionAttribute("modalmedbesked", true);
                ctx.sessionAttribute("showorder", false);
            }
        } catch (DatabaseException e) {
            // Håndterer databasefejl
            System.out.println("Database error: " + e.getMessage());
            ctx.sessionAttribute("error", "Database error: " + e.getMessage());
        } catch (Exception e) {
            // Her håndterer jeg alle andre uventede fejl eksempelvis netværksfejl og skrivefejl
            System.out.println("Unexpected error: " + e.getMessage());
            ctx.sessionAttribute("error", "Unexpected error: " + e.getMessage());
        }
    }

    private static void getCustomerByName(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String userName = ctx.formParam("username");
        User currentUser;
        try {
            currentUser = UserMapper.getCustomerByName(userName, connectionPool);
        } catch (DatabaseException e) {
            // Her håndterer jeg en evt. databasefejl
            System.out.println("Database error: " + e.getMessage());
            ctx.sessionAttribute("error", "Database error: " + e.getMessage());
            return;  // Afbryder metoden, hvis der opstår en databasefejl
        }
        ctx.sessionAttribute("currentuser", currentUser);
    }

    private static void getCustomerByEmail(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String email = ctx.formParam("email");
        User currentUser;
        try {
            currentUser = UserMapper.getCustomerByEmail(email, connectionPool);
        } catch (DatabaseException e) {
            // Her håndterer jeg en evt. databasefejl
            System.out.println("Database error: " + e.getMessage());
            ctx.sessionAttribute("error", "Database error: " + e.getMessage());
            return;  // Afbryder metoden, hvis der opstår en databasefejl
        }
        ctx.sessionAttribute("currentuser", currentUser);
    }

    private static int changeOrderStatusToProduced(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        try {
            OrdersMapper.changeStatusOnOrder("pakket", orderId, connectionPool);
        } catch (DatabaseException e) {
            // Her håndterer jeg en evt. databasefejl
            System.out.println("Database error: " + e.getMessage());
            ctx.sessionAttribute("error", "Database error: " + e.getMessage());
        }
        getAllOrders(ctx, connectionPool);
        return orderId;
    }

    private static void changeOrderStatusToPickedUp(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        try {
            OrdersMapper.changeStatusOnOrder("leveret", orderId, connectionPool);
        } catch (DatabaseException e) {
            // Her håndterer jeg en evt. databasefejl
            System.out.println("Database error: " + e.getMessage());
            ctx.sessionAttribute("error", "Database error: " + e.getMessage());
        }
        getAllOrders(ctx, connectionPool);
    }
}
