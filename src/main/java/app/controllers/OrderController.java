package app.controllers;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.OrdersMapper;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import app.services.CarportSvg;

public class OrderController {
    static Carport carport;

    public static void addRoutes(Javalin app) {
        app.get("/", ctx -> {
            ctx.render("carportspecs.html");
        });
        app.post("/createcarport", ctx -> {
            showOrder(ctx);
            ctx.render("showOrder.html");
        });
        app.post("/ordercarport", ctx -> {
            ctx.render("showOrder.html");
        });
        app.post("/changeorder", ctx -> {
            ctx.render("carportspecs.html");
        });

        app.post("/finishOrder", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            int user_id = UserMapper.createuser(user.getEmail(), user.getPassword(), user.getName(), user.getMobile(), user.getAddress(), user.getZipcode(), ConnectionPool.getInstance());
            int orderID = OrdersMapper.createOrder(user_id, ctx, ConnectionPool.getInstance());
            OrdersMapper.insertPartsNeededForOrder(orderID, ctx, ConnectionPool.getInstance());

            ctx.sessionAttribute("confirmed", true);

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

    private static void index(Context ctx) {
        ctx.render("adminsite.html");
    }

    public static Order getOrderByOrderId(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        return OrdersMapper.getOrderByOrderId(orderId, connectionPool);
    }

    public static Order getOrderByUserId(int userId, ConnectionPool connectionPool) throws DatabaseException {
        return OrdersMapper.getOrderByUserId(userId, connectionPool);
    }

    public static void showOrder(Context ctx) throws DatabaseException {
        double length = Double.parseDouble(ctx.formParam("carport_length"));
        double width = Double.parseDouble(ctx.formParam("carport_width"));
        double height = Double.parseDouble(ctx.formParam("carport_height"));
        double length_shed = Double.parseDouble(ctx.formParam("length_shed"));
        double width_shed = Double.parseDouble(ctx.formParam("width_shed"));
        String roof = (ctx.formParam("carport_trapeztag"));

        // TODO: gør dette pænere
        boolean withRoof = !roof.contains("Uden");
        boolean withShed = length_shed > 0;

        Locale.setDefault(new Locale("da-DK"));

        CarportSvg svg = new CarportSvg((int) width, (int) length, (int) height);

        ctx.attribute("svg", svg.toString());

        //todo: fiks således at shed ikke bliver tilføjet når flueben tjekkes på og af.
        Carport newCarport = new Carport(new ArrayList<CarportPart>(), length, width, height, withRoof, withShed, length_shed, width_shed, 0);

        newCarport.setBEAM(new CarportPart(CarportPart.CarportPartType.BEAM, svg.getMaterialQuantity().get("totalBeams")));
        newCarport.setSUPPORTPOST(new CarportPart(CarportPart.CarportPartType.SUPPORTPOST, svg.getMaterialQuantity().get("totalPoles")));
        newCarport.setRAFT(new CarportPart(CarportPart.CarportPartType.RAFT, svg.getMaterialQuantity().get("totalRafters")));
        newCarport.setCROSSSUPPORT(new CarportPart(CarportPart.CarportPartType.CROSSSUPPORT, svg.getMaterialQuantity().get("totalCrossSupports")));

        ctx.sessionAttribute("newCarport", newCarport);
        PartsCalculator partsCalculator = new PartsCalculator(ctx, ConnectionPool.getInstance());

        ctx.sessionAttribute("carportprice", partsCalculator.getTotalPrice());

        System.out.println(partsCalculator.getCheapestPartList());

        ctx.sessionAttribute("partslist", partsCalculator.getCheapestPartList());
        ctx.sessionAttribute("showpartslist", true);
    }





}