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
    static Date today = new Date();
    static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
    static String formattedDate = formatter.format(today);

    public static void addRoutes(Javalin app) {
        app.get("/", ctx -> {
            ctx.render("carportspecs.html");
        });
        app.post("/createcarport", ctx -> {
            showOrder(ctx);
            //createCarport(ctx, ConnectionPool.getInstance());
            //createPartsList(ctx, ConnectionPool.getInstance());
            ctx.render("showOrder.html");
        });
        app.post("/ordercarport", ctx -> {
            //createCarport(ctx, ConnectionPool.getInstance());
            //createPartsList(ctx, ConnectionPool.getInstance());
            ctx.render("showOrder.html");
        });
        app.post("/changeorder", ctx -> {
            ctx.render("carportspecs.html");
        });

        app.post("/finishOrder", ctx -> {
            //ctx.render("carportspecs.html");
            //createCarport(ctx, ConnectionPool.getInstance());

            User user = ctx.sessionAttribute("currentUser");
            int user_id = UserMapper.createuser(user.getEmail(), user.getPassword(), user.getName(), user.getMobile(), user.getAddress(), user.getZipcode(), ConnectionPool.getInstance());
            int orderID = createOrder(user_id, ctx, ConnectionPool.getInstance());
            insertPartsNeededForOrder(orderID, ctx, ConnectionPool.getInstance());

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


        ctx.sessionAttribute("length", length);
        ctx.sessionAttribute("width", width);
        ctx.sessionAttribute("height", height);
        ctx.sessionAttribute("width_shed", length_shed);
        ctx.sessionAttribute("length_shed", width_shed);
        ctx.sessionAttribute("email", "oleolesen@gmail.com");
        ctx.sessionAttribute("roof", roof);

        Locale.setDefault(new Locale("US"));

        CarportSvg svg = new CarportSvg((int) width, (int) length, (int) height);

        ctx.attribute("svg", svg.toString());

        //todo: fiks således at shed ikke bliver tilføjet når flueben tjekkes på og af.
        Carport newCarport = new Carport(new ArrayList<CarportPart>(), length, width, height, withRoof, withShed, length_shed, width_shed, 0);
        newCarport.addToCarportPartList(new CarportPart(CarportPart.CarportPartType.SUPPORTPOST, svg.getMaterialQuantity().get("totalPoles")));
        newCarport.addToCarportPartList(new CarportPart(CarportPart.CarportPartType.BEAM, svg.getMaterialQuantity().get("totalBeams")));
        newCarport.addToCarportPartList(new CarportPart(CarportPart.CarportPartType.RAFT, svg.getMaterialQuantity().get("totalRafters")));
        newCarport.addToCarportPartList(new CarportPart(CarportPart.CarportPartType.CROSSSUPPORT, svg.getMaterialQuantity().get("totalCrossSupports")));

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
        //ctx.sessionAttribute("partslist", partslist);


        //System.out.println(newCarport);
    }

    // TODO: Skal fjernes/laves ordentligt
    public static void createCarport(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        double carportWidth = ctx.sessionAttribute("length");
        double carportLength = ctx.sessionAttribute("width");
        double carportHeight = ctx.sessionAttribute("height");
        double shedWidth = ctx.sessionAttribute("width_shed");
        double shedLength = ctx.sessionAttribute("length_shed");
        String roof = ctx.sessionAttribute("roof");
        String email = ctx.sessionAttribute("email");
        String orderDate = formattedDate;

        try {
            OrdersMapper.addOrder(carportWidth, carportLength, carportHeight, "modtaget", shedWidth, shedLength, email, orderDate, roof, connectionPool);
            Order order = OrdersMapper.getOrderByEmail(email, connectionPool);
            int currentOrderId = order.getOrderId();
            ctx.sessionAttribute("current_order_id", currentOrderId);

        } catch (DatabaseException e) {
            ctx.attribute("message", "Fejl i oprettelse af ordren!! Prøv igen!");
        }
    }

    public static int createOrder(int userID, Context ctx, ConnectionPool connectionPool) {
        String sql = "insert into ordrene(material_cost, sales_price, carport_width, carport_length, carport_height, user_id, order_status, shed_width, shed_length, email, orderdate, roof, wall) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ) {
            Carport carport = ctx.sessionAttribute("newCarport");
            User user = ctx.sessionAttribute("currentUser");

            ps.setDouble(1, 99999);
            ps.setDouble(2, 99999);
            ps.setDouble(3, carport.getWidth());
            ps.setDouble(4, carport.getLength());
            ps.setDouble(5, carport.getHeight());
            ps.setInt(6, userID); //todo: hent currentUsers user id
            ps.setString(7, "modtaget");
            ps.setDouble(8, carport.getShedWidth());
            ps.setDouble(9, carport.getShedLength());
            ps.setString(10, user.getEmail());
            ps.setString(11, formattedDate);
            ps.setBoolean(12, carport.isWithRoof());
            ps.setBoolean(13, carport.isWithShed());


            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("Fejl ved indsættelse af partslistlinie i tabellen partslist");
            }

            int id = 0;

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
            return id;


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }


    public static void insertPartsNeededForOrder(int orderID, Context ctx, ConnectionPool connectionPool) throws DatabaseException {

        ArrayList<CarportPart> carportPartList = ctx.sessionAttribute("partslist");


        for (CarportPart part : carportPartList) {

            String sql = "insert into partslist(part_id, order_id, quantity, partslistprice, description, unit, part_length, name) values (?,?,?,?,?,?,?,?)";

            try (
                    Connection connection = connectionPool.getConnection();
                    PreparedStatement ps = connection.prepareStatement(sql)
            ) {
                ps.setInt(1, part.getPartId());
                ps.setInt(2, orderID); //todo: virker ikke skal fikses.
                ps.setInt(3, part.getQuantity());
                ps.setDouble(4, part.getDBprice());
                ps.setString(5, part.getDBdescription());
                ps.setString(6, part.getDBunit());
                ps.setInt(7, part.getDBlength());
                ps.setString(8, part.getDBname());


                int rowsAffected = ps.executeUpdate();
                if (rowsAffected != 1) {
                    throw new DatabaseException("Fejl ved indsættelse af partslistlinie i tabellen partslist");
                }
            } catch (SQLException e) {
                throw new DatabaseException(e.getMessage());
            }
        }
    }


//TODO: Skal fjernes/laves ordentligt
    /*public static void createPartsList(Context ctx, ConnectionPool connectionPool) throws  DatabaseException {
        double carportWidth = ctx.sessionAttribute("width");
        double carportLength = ctx.sessionAttribute("length");
        double carportHeight = ctx.sessionAttribute("height");
        double shedWidth = ctx.sessionAttribute("width_shed");
        double shedLength = ctx.sessionAttribute("length_shed");
        int currentOrderId = ctx.sessionAttribute("current_order_id");
        String roof = ctx.sessionAttribute("roof");
        String email = ctx.sessionAttribute("email");

        try {
            // calculating and inserting Posts into Partslist
            int numberOfPosts = PartsCalculator.calculateNumberOfPosts(carportWidth, carportLength);
            double metersOfPosts = numberOfPosts*(carportHeight+90);
            Part post = PartslistMapper.getPartByType("stolpe", connectionPool);
            double postListPrice = post.getPrice() * (metersOfPosts/100);
            PartslistMapper.insertPartslistLine(new Partslistline(post.getPartId(), currentOrderId, numberOfPosts, postListPrice, post.getDescription(), post.getUnit(), post.getLength(), post.getName()), connectionPool);

            // calculating and inserting Rafts into Partslist
            int numberOfRafts = PartsCalculator.calculateNumberOfRafts(carportLength);
            double metersOfRafts = numberOfRafts*carportWidth;
            Part raft = PartslistMapper.getPartByTypeAndLength("spær", carportWidth, connectionPool);
            double raftListPrice = raft.getPrice() * (metersOfRafts/100);
            PartslistMapper.insertPartslistLine(new Partslistline(raft.getPartId(), currentOrderId, numberOfRafts, raftListPrice, raft.getDescription(), raft.getUnit(), raft.getLength(), raft.getName()), connectionPool);

            // calculating and inserting Roofmaterial into PartsList
            if(ctx.sessionAttribute("roof") != null) {
                double lengthOfRoofPlate = 360;
                int numberofRoofPlates = PartsCalculator.calculateNumberOfRoofPlates(carportLength, carportWidth);
                Part roofPart = PartslistMapper.getPartByTypeAndLength("tagplader", lengthOfRoofPlate, connectionPool);
                double roofListPrice = roofPart.getPrice() * (numberofRoofPlates/100);
                PartslistMapper.insertPartslistLine(new Partslistline(roofPart.getPartId(), currentOrderId, numberofRoofPlates, roofListPrice, roofPart.getDescription(), roofPart.getUnit(), roofPart.getLength(), roofPart.getName()), connectionPool);
            }

            // calculating and inserting vindkryds into PartsList
            double lenghtOfKryds = 4*(carportLength/100);
            double quantityOfKryds = lenghtOfKryds/10;
            int roundedQuantity = (int) Math.ceil(quantityOfKryds);
            Part kryds = PartslistMapper.getPartByType("hulbånd", connectionPool);
            double krydsListPrice = kryds.getPrice() * roundedQuantity;
            PartslistMapper.insertPartslistLine(new Partslistline(kryds.getPartId(), currentOrderId, roundedQuantity, krydsListPrice, kryds.getDescription(), kryds.getUnit(), kryds.getLength(), kryds.getName()), connectionPool);

            // calculating and inserting Beams into Partslist
            double metersOfBeams = 2*carportLength;
            double beamsListPrice = raft.getPrice() * (metersOfBeams/100);
            PartslistMapper.insertPartslistLine(new Partslistline(raft.getPartId(), currentOrderId, 2, beamsListPrice, raft.getDescription(), raft.getUnit(), raft.getLength(), raft.getName()), connectionPool);

            // Updating the price for the Carport
            ArrayList<Partslistline> partslist = PartslistMapper.getPartsListByOrderid(currentOrderId, connectionPool);
            double sum = 0;
            for(Partslistline partslistline: partslist) {
                sum += partslistline.getPartlistlineprice();
            }
            System.out.println(""+sum);
            ctx.sessionAttribute("carportprice", sum);
            ctx.sessionAttribute("partslist", partslist);

            //creating the partlist and add it to a sessionattribut



            //ctx.sessionAttribute("showpartslist", true);

        } catch (DatabaseException e) {
            ctx.attribute("message", "Fejl i oprettelse af PartsListen!");
        }
    }*/
}