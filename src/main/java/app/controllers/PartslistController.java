package app.controllers;

import app.entities.Partslistline;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.PartslistMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.List;

public class PartslistController {
    static ArrayList<Partslistline> partslistLines = new ArrayList<>();

        public static void addRoutes(Javalin app)
        {

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
        List<Partslistline> partslist = PartslistMapper.getPartsList(orderId, connectionPool);
        ctx.attribute("partslist", partslist);
    }

    public static void insertPartsListLine(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        Partslistline partslistline = ctx.sessionAttribute("partlistline");
        if (ctx.sessionAttribute("partslist") != null) {
            partslistLines = ctx.sessionAttribute("partslist");
        }
        Partslistline newpartslistline = new Partslistline(partslistline.getPartId(), partslistline.getOrderId(), partslistline.getQuantity(), partslistline.getPartlistlineprice(), partslistline.getDescription());
        partslistLines.add(newpartslistline);
        PartslistMapper.insertPartslistLine(newpartslistline, connectionPool);
        ctx.attribute("partslist", partslistLines);
    }
}
