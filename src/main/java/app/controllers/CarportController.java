package app.controllers;

import app.entities.Order;
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
    /*
    static ArrayList<Order> orderLine = new ArrayList<>();

        public static void addRoutes(Javalin app)
        {
            app.get("/", ctx -> {
               showTopping(ctx,ConnectionPool.getInstance());
               showBottom(ctx, ConnectionPool.getInstance());
               ctx.render("carportspecs.html");
            });
            app.post("/createorder", ctx -> {
                createOrder(ctx,ConnectionPool.getInstance());
            });
            app.get("/showcupcakes", ctx -> {
                ctx.render("checkoutpage.html");
            });
            app.post("/ordermore", ctx -> {
                showTopping(ctx,ConnectionPool.getInstance());
                showBottom(ctx, ConnectionPool.getInstance());
                ctx.render("carportspecs.html");
            });

            app.post("/payorder", ctx -> {
                payForOrder(ctx,ConnectionPool.getInstance());
                //ctx.render("checkoutpage.html");
            });
            app.post("deleteorderline", ctx -> deleteorderline(ctx, ConnectionPool.getInstance()));
        }

    private static void deleteorderline(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
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

        // Hvis kunden sletter alle sine ordrelinier sender jeg ham tilbage til carportspecs.html. Er er stadig
        // ordrelinier smider jeg ham tilbage til checkoutpage.html
        if (orderLines.isEmpty()) {
            showTopping(ctx, ConnectionPool.getInstance());
            showBottom(ctx, ConnectionPool.getInstance());
            ctx.render("carportspecs.html");
        } else {
            ctx.render("checkoutpage.html");
        }
    }

    private static void createOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        User currentUser = ctx.sessionAttribute("currentUser");
        if (currentUser == null) {
            ctx.render("login.html");
            return;
        }

        if (ctx.sessionAttribute("orders") != null) {
            orderLine = ctx.sessionAttribute("orders");
        }

        String email = currentUser.getEmail();
        String name = currentUser.getName();
        String mobile = currentUser.getMobile();
        int balance = currentUser.getBalance();

        int toppingId = Integer.parseInt(ctx.formParam("topping")); // Assumes you have toppingId in form
        int bottomId = Integer.parseInt(ctx.formParam("bund"));   // Assumes you have bottomId in form
        int quantity = Integer.parseInt(ctx.formParam("antal"));

        Topping topping = CarportMapper.getToppingById(toppingId, connectionPool);
        Bottom bottom = CarportMapper.getBottomById(bottomId, connectionPool);
        if (topping == null || bottom == null) {
            throw new DatabaseException("cant find id for bottom or topping");
        }

        int orderlinePrice = calculateOrderLinePrice(topping, bottom, quantity);

        Order order = new Order(currentUser.getUserId(), email, name, mobile, balance, topping.getTopping(), bottom.getBottom(), quantity, orderlinePrice);
        orderLine.add(order);

        ctx.sessionAttribute("orders", orderLine);


        int totalAmount = 0;
        int orderCount = 0;
        for (Order orderline : orderLine) {
            if (order.getUserId() == currentUser.getUserId()) {
                totalAmount += orderline.getOrderlinePrice();
                orderCount++;
            }
        }

        ctx.sessionAttribute("totalAmount", totalAmount); // Sender det samlede beløb som en attribut til HTML-skabelonen
        ctx.sessionAttribute("orderCount", orderCount);

        showTopping(ctx, ConnectionPool.getInstance());
        showBottom(ctx, ConnectionPool.getInstance());
        ctx.render("carportspecs.html");
    }

    private static int calculateOrderLinePrice(Topping topping, Bottom bottom, int quantity) {
        int totalItemPrice = topping.getPrice() + bottom.getPrice();
        return totalItemPrice * quantity;
    }

    public static void showBottom(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        List<Bottom> bottomList = CarportMapper.showBottoms(connectionPool);
        ctx.attribute("bottomList", bottomList);
    }

    public static void showTopping(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        List<Topping> toppingList = CarportMapper.showToppings(connectionPool);
        ctx.attribute("toppingList", toppingList);
    }

    public static void payForOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
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

                CarportMapper.payForOrder(generatedOrderId, toppingId, bottomId, order.getQuantity(), order.getOrderlinePrice(), connectionPool);

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
            showBottom(ctx, ConnectionPool.getInstance());
            ctx.render("carportspecs.html");
        } else {
            //ctx.attribute("message", "Din saldo lyder på " + currentUser.getBalance() + " kr. hvilket ikke er nok til at betale for ordren! Fjern nogle varer fra din kurv eller indbetal penge på din konto!");
            ctx.attribute("notenoughtmoney", true);
            ctx.render("checkoutpage.html");
        }
    }
*/
}
