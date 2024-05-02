package app.controllers;

import app.entities.Carport;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.CarportMapper;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

public class CarportController {

        public static void addRoutes(Javalin app)
        {
            app.get("/", ctx -> {

               ctx.render("index.html");

            });
            app.post("/saveCarportData", ctx -> {
                String width = ctx.formParam("carport_width");
                String length = ctx.formParam("carport_length");
                String trapeztag = ctx.formParam("carport_trapeztag");

                ctx.sessionAttribute("carport_width",width);
                ctx.sessionAttribute("carport_length",length);
                ctx.sessionAttribute("carport_trapeztag",trapeztag);

                ctx.render("index.html"); //render næste side efter at man har indtastet carport og trykket videre
            });



            app.post("/createorder", ctx -> {
                createOrder(ctx,ConnectionPool.getInstance());
            });
            app.post("/ordermore", ctx -> {
                showOrders(ctx, ConnectionPool.getInstance());
                ctx.render("index.html");
            });



            //app.post("deleteorderline", ctx -> deleteorderline(ctx, ConnectionPool.getInstance()));
        }

    private static void createOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int length = ctx.sessionAttribute("carport_length");
        int width = ctx.sessionAttribute("carport_width");
        boolean trapeztag = ctx.sessionAttribute("carport_trapeztag");

        CarportMapper.InsertIntoOrders(userId,orderDate,orderprice,status,length,width,trapeztag);

    }

    /*public static void showOrders(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        List<Carport> orderList = CarportMapper.showOrders(connectionPool);
        ctx.attribute("orderList", orderList);
    }*/



    }
}
