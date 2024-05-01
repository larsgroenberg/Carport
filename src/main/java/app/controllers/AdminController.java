package app.controllers;

import app.entities.Material;
import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.AdminMapper;
import app.persistence.ConnectionPool;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;

public class AdminController
{

    public static void addRoutes(Javalin app)
    {
        app.get("/adminSite", ctx -> index(ctx));

        app.post("/getAllOrders", ctx -> getAllOrders(ctx, ConnectionPool.getInstance()));
        app.post("/showCustomer", ctx -> showCustomer(ctx, ConnectionPool.getInstance()));
        app.post("/getMaterialById", ctx -> getMaterialById(ctx, ConnectionPool.getInstance()));
        app.post("/getMaterialByName", ctx -> getMaterialByName(ctx, ConnectionPool.getInstance()));
        app.post("/showCustomerOrders", ctx -> showCustomerOrders(ctx, ConnectionPool.getInstance()));
        app.post("/getAllMaterials", ctx -> showMaterials(ctx, ConnectionPool.getInstance()));
        app.post("/addToBalance", ctx -> addMoneyToCustomerBalance(ctx, ConnectionPool.getInstance()));
    }

    private static void index(Context ctx)
    {
        ctx.render("adminSite.html");
    }

    private static void showMaterials(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        AdminMapper.showMaterials(connectionPool);
        ArrayList<Material> materialList = AdminMapper.showMaterials(connectionPool);
        ctx.attribute("materiallist", materialList);

        ctx.render("adminSite.html");
    }

    private static void getMaterialById(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int materialId = Integer.parseInt(ctx.formParam("materialid"));
        Material material = AdminMapper.getMaterialById(materialId, connectionPool);
        ctx.attribute("material", material);

        ctx.render("adminSite.html");
    }

    private static void getMaterialByName(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String name = ctx.formParam("materialbyname");
        Material material = AdminMapper.getMaterialByName(name, connectionPool);
        ctx.attribute("material", material);

        ctx.render("adminSite.html");
    }

    private static void getAllOrders(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        AdminMapper.showAllOrders(connectionPool);
        ArrayList<Order> customerOrders = AdminMapper.showAllOrders(connectionPool);
        ctx.attribute("customerOrders", customerOrders);

        ctx.render("adminSite.html");
    }

    private static void showCustomer(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String username = ctx.formParam("username_input");
        ArrayList<User> customer = AdminMapper.showCustomer(username, connectionPool);
        ctx.attribute("customer", customer);

        ctx.render("adminSite.html");
    }

    private static void showCustomerOrders(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String username = ctx.formParam("username_input");

        ArrayList<Order> customerOrders = AdminMapper.showCustomerOrders(username, connectionPool);
        ctx.attribute("customerOrders", customerOrders);

        ctx.render("adminSite.html");
    }

    private static void addMoneyToCustomerBalance(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String customerEmail = ctx.formParam("email");
        String amountToAdd = ctx.formParam("balance_input");

        AdminMapper.addMoneyToCustomerBalance(customerEmail,amountToAdd, connectionPool);


        ctx.render("adminSite.html");
    }
}
