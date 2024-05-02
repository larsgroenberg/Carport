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
        app.post("/getOrderByEmail", ctx -> getOrderByEmail(ctx, ConnectionPool.getInstance()));
        app.post("/getOrderByName", ctx -> getOrderByName(ctx, ConnectionPool.getInstance()));
        app.post("/getCustomerByName", ctx -> getCustomerByName(ctx, ConnectionPool.getInstance()));
        app.post("/getCustomerByEmail", ctx -> getCustomerByEmail(ctx, ConnectionPool.getInstance()));
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

    private static void getOrderByEmail(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String email = ctx.formParam("email");
        Order customerOrder = AdminMapper.getOrderByEmail(email, connectionPool);
        ctx.attribute("customerOrders", customerOrder);
        ctx.render("adminSite.html");
    }

    private static void getOrderByName(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String userName = ctx.formParam("username");
        Order customerOrder = AdminMapper.getOrderByEmail(userName, connectionPool);
        ctx.attribute("customerOrders", customerOrder);
        ctx.render("adminSite.html");
    }

    private static void getCustomerByName(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String userName = ctx.formParam("username");
        User currentUser = AdminMapper.getCustomerByName(userName, connectionPool);
        ctx.attribute("currentuser", currentUser);
        ctx.render("adminSite.html");
    }

    private static void getCustomerByEmail(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String email = ctx.formParam("email");
        User currentUser = AdminMapper.getCustomerByEmail(email, connectionPool);
        ctx.attribute("currentuser", currentUser);
        ctx.render("adminSite.html");
    }
}
