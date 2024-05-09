package app.entities;

import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PartsCalculator {
    private ArrayList<CarportPart> partNeededList;
    private ArrayList<CarportPart> dbPartsList;
    private Carport carport = null;
    private ArrayList<CarportPart> cheapestPartList;
    private double totalPrice = 0;
    //Context ctx;

    public PartsCalculator(Context _ctx, ConnectionPool _connectionPool) throws DatabaseException {
        carport = _ctx.sessionAttribute("newCarport");

        partNeededList = carport.getCarportPartList();

        dbPartsList = getDBParts(_connectionPool);

        //simpleCompareLists();
        MaterialCalculationOnlyMaxLength();
    }

    public ArrayList<CarportPart> getDBParts(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<CarportPart> partList = new ArrayList<>();
        String sql = "SELECT * FROM parts";

        try(Connection connection = connectionPool.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(sql)){
                ResultSet rs = ps.executeQuery();

                while (rs.next()){
                    int partId = rs.getInt("part_id");
                    int price = rs.getInt("price");
                    String description = rs.getString("description");
                    int length = rs.getInt("length");
                    int height = rs.getInt("height");
                    int width = rs.getInt("width");
                    String type = rs.getString("type");
                    String material_name = rs.getString("material");
                    String unit = rs.getString("unit");
                    String name = rs.getString("name");
                    CarportPart.CarportPartType partType = null;
                    switch (type) {
                        case "stolpe" -> partType = CarportPart.CarportPartType.SUPPORTPOST;
                        case "spær" -> partType = CarportPart.CarportPartType.BEAM;
                        case "brædder" -> partType = CarportPart.CarportPartType.RAFT;
                        case "hulbånd" -> partType = CarportPart.CarportPartType.CROSSSUPPORT;
                    }
                    partList.add(new CarportPart(partType,0,partId, price, length, height, width, description, material_name, unit, name));
                }
            }
        }catch (SQLException e){
            throw new DatabaseException("We couldn't get the part", e.getMessage());
        }
        return partList;
    }


    public void simpleCompareLists(){
        CarportPart cheapestBeam = new CarportPart(CarportPart.CarportPartType.BEAM, 0,0, 10000, 0,0,0,"","","","");
        CarportPart cheapestSupport = new CarportPart(CarportPart.CarportPartType.SUPPORTPOST, 0,0, 10000, 0,0,0,"","","","");
        CarportPart cheapestRaft = new CarportPart(CarportPart.CarportPartType.RAFT, 0,0, 10000, 0,0,0,"","","","");
        cheapestPartList = new ArrayList<>();

        for (CarportPart part : dbPartsList){
            if(part.getType() == cheapestBeam.getType() && part.getDBprice() < cheapestBeam.getDBprice()){
                cheapestBeam = part;
            }
            if(part.getType() == cheapestSupport.getType() && part.getDBprice() < cheapestSupport.getDBprice()){
                cheapestSupport = part;
            }
            if(part.getType() == cheapestRaft.getType() && part.getDBprice() < cheapestRaft.getDBprice()){
                cheapestRaft = part;
            }
        }

        cheapestBeam.setQuantity(carport.getBEAM().getQuantity());
        cheapestSupport.setQuantity(carport.getSUPPORTPOST().getQuantity());
        cheapestRaft.setQuantity(carport.getRAFT().getQuantity());



        cheapestPartList.add(cheapestBeam);
        cheapestPartList.add(cheapestSupport);
        cheapestPartList.add(cheapestRaft);

        totalPrice = (cheapestBeam.getDBprice() * cheapestBeam.getQuantity()) + (cheapestRaft.getDBprice() * cheapestRaft.getQuantity()) + (cheapestSupport.getDBprice() * cheapestSupport.getQuantity());
    }


    // Denne metode tager udregner den samlede længde på de forskellige sorter og derefter finder det længdste stykke træ og dividere dem for at få et antal som er større for at garentere at der er nok træ.
    public void MaterialCalculationOnlyMaxLength(){
        double supportPostLength = (90 + carport.getHeight()) * carport.getSUPPORTPOST().getQuantity();
        double beamLength = carport.getLength() * carport.getBEAM().getQuantity();
        double raftLength = carport.getWidth() * carport.getRAFT().getQuantity();

        CarportPart cheapestBeam = new CarportPart(CarportPart.CarportPartType.BEAM, 0,0, 10000, 0,0,0,"","","","");
        CarportPart cheapestSupport = new CarportPart(CarportPart.CarportPartType.SUPPORTPOST, 0,0, 10000, 0,0,0,"","","","");
        CarportPart cheapestRaft = new CarportPart(CarportPart.CarportPartType.RAFT, 0,0, 10000, 0,0,0,"","","","");
        CarportPart cheapestCrossSupport = new CarportPart(CarportPart.CarportPartType.CROSSSUPPORT, 0,0, 10000, 0,0,0,"","","","");
        cheapestPartList = new ArrayList<>();

        for (CarportPart part : dbPartsList){
            if(part.getType() == cheapestBeam.getType() && part.getDBlength() > cheapestBeam.getDBlength()){
                cheapestBeam = part;
            }
            if(part.getType() == cheapestSupport.getType() && part.getDBlength() > cheapestSupport.getDBlength()){
                cheapestSupport = part;
            }
            if(part.getType() == cheapestRaft.getType() && part.getDBlength() > cheapestRaft.getDBlength()){
                cheapestRaft = part;
            }
            if(part.getType() == cheapestCrossSupport.getType() && part.getDBlength() > cheapestCrossSupport.getDBlength()){
                cheapestCrossSupport = part;
            }
        }
        int beamQuantityNeeded = (int) Math.ceil(beamLength / cheapestBeam.getDBlength());
        int supportPostQuantityNeeded = (int) Math.ceil(supportPostLength / cheapestSupport.getDBlength());
        int raftQuantityNeeded = (int) Math.ceil(raftLength / cheapestRaft.getDBlength());

        int crossSupportQuantityNeeded = (int) Math.ceil(Math.sqrt(carport.getWidth()*carport.getWidth() + carport.getLength()* carport.getLength())/cheapestCrossSupport.getDBlength()) *2;


        cheapestBeam.setQuantity(beamQuantityNeeded);
        cheapestSupport.setQuantity(supportPostQuantityNeeded);
        cheapestRaft.setQuantity(raftQuantityNeeded);
        cheapestCrossSupport.setQuantity(crossSupportQuantityNeeded);


        cheapestPartList.add(cheapestBeam);
        cheapestPartList.add(cheapestSupport);
        cheapestPartList.add(cheapestRaft);
        cheapestPartList.add(cheapestCrossSupport);

        totalPrice = (cheapestBeam.getDBprice() * cheapestBeam.getQuantity()) + (cheapestRaft.getDBprice() * cheapestRaft.getQuantity()) + (cheapestSupport.getDBprice() * cheapestSupport.getQuantity()) + (cheapestCrossSupport.getDBprice() * cheapestCrossSupport.getQuantity());
    }



    public double getTotalPrice() {
        return totalPrice;
    }

    public ArrayList<CarportPart> getCheapestPartList() {
        return cheapestPartList;
    }
}
