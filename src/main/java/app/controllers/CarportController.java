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
    //static ArrayList<Order> orderLine = new ArrayList<>();

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


            /*app.post("/createorder", ctx -> {
                createOrder(ctx,ConnectionPool.getInstance());
            });*/
            /*app.post("/ordermore", ctx -> {
                showOrders(ctx, ConnectionPool.getInstance());
                ctx.render("index.html");
            });*/

            //app.post("deleteorderline", ctx -> deleteorderline(ctx, ConnectionPool.getInstance()));
        }

    /*private static void deleteorderline(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        List<Order> orderLines = ctx.sessionAttribute("orders");
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        // Her fjerner jeg den ordrelinie som kunden ønsker at fjerne
        for (Order order : orderLines) {
            if (order.getOrderId() == orderId) {
                orderLines.remove(order);
                break;
            }
        }
        // Her lægger jeg den opdaterede orderline-liste ind i en sessionAtribut ved navn orders
        ctx.sessionAttribute("orders", orderLines);
        User currentUser = ctx.sessionAttribute("currentUser");
        // Her udregner jeg hvor mange ordrelinier der er og hvad den samlede pris er for dem
        int orderCount = 0;
        int totalAmount = 0;
        for (Order orderline : orderLines) {
            if (orderline.getUserId() == currentUser.getUserId()) {
                totalAmount += orderline.getOrderlinePrice();
                orderCount++;
            }
        }
        // Her opdaterer jeg orderCount og totalAmount i deres respektive sessionatributter
        ctx.sessionAttribute("orderCount", orderCount);
        ctx.sessionAttribute("totalAmount", totalAmount);

        // Hvis kunden sletter alle sine ordrelinier sender jeg ham tilbage til index.html. Er er stadig
        // ordrelinier smider jeg ham tilbage til checkoutpage.html
        if (orderLines.isEmpty()) {
            showTopping(ctx, ConnectionPool.getInstance());
            showOrders(ctx, ConnectionPool.getInstance());
            ctx.render("index.html");
        } else {
            ctx.render("checkoutpage.html");
        }
    }*/

    private static void createOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int length = ctx.sessionAttribute("carport_length");
        int width = ctx.sessionAttribute("carport_width");
        boolean trapeztag = ctx.sessionAttribute("carport_trapeztag");


        //CarportMapper.InsertIntoOrders(userId,orderDate,orderprice,status,length,width,trapeztag);
    }

    /*public static void showOrders(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        List<Carport> orderList = CarportMapper.showOrders(connectionPool);
        ctx.attribute("orderList", orderList);
    }*/

    /*public static void payForOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        User currentUser = ctx.sessionAttribute("currentUser");
        int orderprice = ctx.sessionAttribute("totalAmount");

        if (currentUser.getBalance() >= orderprice) {
            ArrayList<Order> tempOrderLine = ctx.sessionAttribute("orders");

            int generatedOrderId = CarportMapper.insertOrder(tempOrderLine.get(0).getUserId(), connectionPool);
            List<Topping> toppingList = CarportMapper.showToppings(connectionPool);
            List<Bottom> bottomList = CarportMapper.showBottoms(connectionPool);

            for (Order order : tempOrderLine) {
                int toppingId = 0;
                for (Topping topping : toppingList) {
                    if (order.getTopping().equals(topping.getTopping())) toppingId = topping.getToppingId();
                }
                int bottomId = 0;
                for (Bottom bottom : bottomList) {
                    if (order.getBottom().equals(bottom.getBottom())) bottomId = bottom.getBottomId();
                }

                CarportMapper.InsertIntoOrders(generatedOrderId, toppingId, bottomId, order.getQuantity(), order.getOrderlinePrice(), connectionPool);

            }
            tempOrderLine.clear();
            CarportMapper.deleteUsersBasket(currentUser.getUserId(), connectionPool);
            int newBalance = currentUser.getBalance() - orderprice;
            currentUser.setBalance(newBalance);
            UserMapper.updateBalance(currentUser.getUserId(), newBalance, connectionPool);
            ctx.attribute("message", "Tak for din ordre. Din ordre har fået ordrenummer " + generatedOrderId + ". Du hører fra os når din ordre er parat til afhentning!");
            ctx.attribute("ordercreated", true);
            ctx.sessionAttribute("totalAmount", 0);
            ctx.sessionAttribute("orderCount", 0);
            showTopping(ctx, ConnectionPool.getInstance());
            showOrders(ctx, ConnectionPool.getInstance());
            ctx.render("index.html");
        } else {
            ctx.attribute("message", "Din saldo lyder på " + currentUser.getBalance() + " kr. hvilket ikke er nok til at betale for ordren! Fjern nogle varer fra din kurv eller indbetal penge på din konto!");
            ctx.attribute("notenoughtmoney", true);
            ctx.render("checkoutpage.html");
        }
    }*/

}
