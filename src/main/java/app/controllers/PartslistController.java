package app.controllers;

import app.entities.Order;
import app.entities.Partslistline;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.PartslistMapper;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

public class PartslistController {
    static ArrayList<Partslistline> partslistlines = new ArrayList<>();

        public static void addRoutes(Javalin app)
        {
            app.get("/", ctx -> {
                ctx.render("index.html");
            });
            app.get("/showpartslist", ctx -> {
                showPartsList(ctx,ConnectionPool.getInstance());
                ctx.render("checkoutpage.html");
            });

            app.get("/insertpartlistline", ctx -> {
                insertPartsListLine(ctx,ConnectionPool.getInstance());
                ctx.render("checkoutpage.html");
            });
        }

    public static void showPartsList(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int orderId = ctx.sessionAttribute("orderid");
        List<Partslistline> partslist = PartslistMapper.getPartslist(orderId, connectionPool);
        ctx.attribute("partslist", partslist);
    }

    public static void insertPartsListLine(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        Partslistline partslistline = ctx.sessionAttribute("partlistline");
        if (ctx.sessionAttribute("partslist") != null) {
            partslistlines = ctx.sessionAttribute("partslist");
        }
        Partslistline newpartslistline = new Partslistline(partslistline.getMaterialId(), partslistline.getOrderId(), partslistline.getQuantity(), partslistline.getPartlistlineprice());
        partslistlines.add(newpartslistline);
        PartslistMapper.insertPartslistLine(newpartslistline, connectionPool);
        ctx.attribute("partslist", partslistlines);
    }
}
