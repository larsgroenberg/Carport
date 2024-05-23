package app.controllers;

import java.util.*;

import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.CarportPartMapper;
import app.persistence.ConnectionPool;
import app.persistence.OrdersMapper;
import app.persistence.UserMapper;
import app.services.EmailService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import app.services.CarportSvg;


public class OrderController {
    private static Carport carport;
    private static Order order;
    private static ArrayList<CarportPart> dbPartsList;
    public static void addRoutes(Javalin app) {
        app.get("/", ctx -> {
            //ctx.render("carportspecs.html", prepareModel(ctx));
            ctx.render("adminsite.html");
        });
        app.post("/createcarport", ctx -> {
            showOrder(ctx, ConnectionPool.getInstance());
            calculateAndCreatePartsList(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("showpartslist", true);
            ctx.render("showOrder.html");
        });
        app.post("/ordercarport", ctx -> {
            ctx.render("showOrder.html");
        });
        app.get("/changeorder", ctx -> {
            ctx.render("carportspecs.html", prepareModel(ctx));
        });

        app.post("/finishOrder", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            int user_id = UserMapper.createuser(user.getEmail(), user.getPassword(), user.getName(), user.getMobile(), user.getAddress(), user.getZipcode(), ConnectionPool.getInstance());
            int orderID = OrdersMapper.createOrder(user_id, ctx, ConnectionPool.getInstance());
            OrdersMapper.insertPartsNeededForOrder(orderID, ctx, ConnectionPool.getInstance());

            ctx.sessionAttribute("confirmed", true);

            EmailService.sendEmail(user);

            ctx.render("checkoutpage.html");
            //todo: skal nok lige kigges igennem og laves check for diverse ting og sager.
        });

        //todo: find på smart måde således at brugere ikke bliver ført videre såfremt de ikke er nået dertil i processen.
        app.get("/carport-drawing", ctx -> {
            if (ctx.sessionAttribute("newCarport") != null) {
                ctx.render("showOrder.html");
            } else ctx.render("carportspecs.html");
        });

        app.get("/user-details", ctx -> {
            if (ctx.sessionAttribute("newCarport") != null) {
                ctx.render("createuser.html");
            } else ctx.render("carportspecs.html");

        });

        app.get("/confirmation", ctx -> {
            if (ctx.sessionAttribute("newCarport") != null && ctx.sessionAttribute("currentUser") != null) {
                ctx.render("checkoutpage.html");
            } else ctx.render("carportspecs.html");
        });
    }

    // Prepare model method to add session attributes to the model
    private static Map<String, Object> prepareModel(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        // Add session attributes to the model
        model.put("newCarport", ctx.sessionAttribute("newCarport"));
        return model;
    }
    private static void index(Context ctx) {
        ctx.render("adminsite.html");
    }

    public static Order getOrderByOrderId(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        return OrdersMapper.getOrderByOrderId(orderId, connectionPool);
    }

    public static Order getOrderByUserId(int userId, ConnectionPool connectionPool) throws DatabaseException {
        return OrdersMapper.getOrderByUserId(userId, connectionPool);
    }

    public static void showOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        double length = Double.parseDouble(ctx.formParam("carport_length"));
        double width = Double.parseDouble(ctx.formParam("carport_width"));
        double height = Double.parseDouble(ctx.formParam("carport_height"));
        String lengthShedString = ctx.formParam("length_shed");
        String widthShedString = ctx.formParam("width_shed");
        double length_shed = 0.0;
        double width_shed = 0.0;

        if (lengthShedString != null && !lengthShedString.isEmpty()) {
            length_shed = Double.parseDouble(lengthShedString.trim());
        }
        if (widthShedString != null && !widthShedString.isEmpty()) {
            width_shed = Double.parseDouble(widthShedString.trim());
        }

        String roof = (ctx.formParam("carport_trapeztag"));
        double postLength = height + 90;

        // TODO: gør dette pænere
        boolean withRoof = !roof.contains("Uden");
        boolean withShed = length_shed > 0;
        ctx.sessionAttribute("carport_trapeztag", roof);
        ctx.sessionAttribute("carport_width", (int) width);
        ctx.sessionAttribute("carport_length", (int) length);
        ctx.sessionAttribute("postlength", (int) postLength);
        if(length_shed > 0) {
            ctx.sessionAttribute("shed", true);
        }

        dbPartsList = CarportPartMapper.getDBParts(connectionPool);
        ctx.sessionAttribute("newdbPartsList", dbPartsList);

        Locale.setDefault(new Locale("da-DK"));

        CarportSvg svgFromTop = new CarportSvg(ctx, (int) width, (int) length, (int) height, length_shed, width_shed);
        CarportSvg svgFromSide = new CarportSvg(ctx, (int) width, (int) length, (int) height, length_shed, width_shed, roof);
        ctx.sessionAttribute("totalPoles", svgFromTop.getTotalPoles());
        ctx.sessionAttribute("totalBeams", svgFromTop.getTotalBeams());
        ctx.sessionAttribute("totalRafters", svgFromTop.getTotalRafters());
        ctx.sessionAttribute("totalCrossSupports", svgFromTop.getTotalCrossSupports());
        ctx.sessionAttribute("totalBoards", svgFromSide.getTotalBoards());

        ctx.sessionAttribute("svgFromTop", svgFromTop.toString());
        ctx.sessionAttribute("svgFromSide", svgFromSide.toString());
        carport = new Carport(length, width, height, withRoof, withShed, length_shed, width_shed,0);
    }

    public static double calculateAndCreatePartsList(Context ctx, ConnectionPool connectionPool) throws DatabaseException
    {
        double totalPrice = 0.0;
        dbPartsList = CarportPartMapper.getDBParts(connectionPool);
        ctx.sessionAttribute("dbPartsList", dbPartsList);
        PartsCalculator partsCalculator = new PartsCalculator(carport, dbPartsList, ctx, ConnectionPool.getInstance());
        partsCalculator.calculateCarport(ctx);

        ArrayList<CarportPart> partsList = ctx.sessionAttribute("partsList");
        for(CarportPart part: partsList) {
            totalPrice += (part.getQuantity()*part.getDBprice());
        }
        totalPrice = Math.ceil(totalPrice);
        double carportTotalPrice = Math.ceil(totalPrice*1.4);
        ctx.sessionAttribute("carportprice", carportTotalPrice);
        carport.setPrice(totalPrice);
        ctx.sessionAttribute("newCarport", carport);
        return totalPrice;
    }

}
