package app.entities;

import app.controllers.CalculationController;
import app.exceptions.DatabaseException;
import app.persistence.CarportPartMapper;
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
    //private ArrayList<CarportPart> partNeededList;
    private ArrayList<CarportPart> dbPartsList;
    private Carport carport = null;
    private ArrayList<CarportPart> cheapestPartList;
    private double totalPrice = 0;
    //Context ctx;

    public PartsCalculator(Context _ctx, ConnectionPool _connectionPool) throws DatabaseException {
        carport = _ctx.sessionAttribute("newCarport");

        /*partNeededList = new ArrayList<CarportPart>();
        partNeededList.add(carport.getBEAM());
        partNeededList.add(carport.getCROSSSUPPORT());
        partNeededList.add(carport.getRAFT());
        partNeededList.add(carport.getSUPPORTPOST());*/



        dbPartsList = CarportPartMapper.getDBParts(_connectionPool);

        //simpleCompareLists();
        //MaterialCalculationOnlyMaxLength();
        MaterialCalculationPreciseMatch();
        double lukasMetoden = CalculationController.calculateFullCarportPrice(_ctx,carport,_connectionPool);
        _ctx.sessionAttribute("lukas",lukasMetoden);
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

    // Denne metode tager udregner den samlede længde på de forskellige sorter og derefter finder det længdste stykke træ og dividere dem for at få et antal som er større for at garentere at der er nok træ.
    public void MaterialCalculationPreciseMatch(){
        double supportPostLength = (90 + carport.getHeight()); // * carport.getSUPPORTPOST().getQuantity()
        double beamLength = carport.getLength();// * carport.getBEAM().getQuantity()
        double raftLength = carport.getWidth();// * carport.getRAFT().getQuantity()
        double roofTile = carport.getLength();

        double frontShedWood = carport.getShedWidth();
        double sideShedWood = carport.getShedLength();

        CarportPart cheapestBeam = new CarportPart(CarportPart.CarportPartType.BEAM, 0,0, 10000, 0,0,0,"","","","");
        CarportPart cheapestSupport = new CarportPart(CarportPart.CarportPartType.SUPPORTPOST, 0,0, 10000, 0,0,0,"","","","");
        CarportPart cheapestRaft = new CarportPart(CarportPart.CarportPartType.RAFT, 0,0, 10000, 0,0,0,"","","","");
        CarportPart cheapestCrossSupport = new CarportPart(CarportPart.CarportPartType.CROSSSUPPORT, 0,0, 10000, 0,0,0,"","","","");
        CarportPart cheapestRooftile = new CarportPart(CarportPart.CarportPartType.ROOFTILE, 0,0, 10000, 0,0,0,"","","","");

        CarportPart cheapestFrontShedWood = new CarportPart(CarportPart.CarportPartType.SHEDWOOD, 0,0, 10000, 0,0,0,"","","","");
        CarportPart cheapestSideShedWood = new CarportPart(CarportPart.CarportPartType.SHEDWOOD, 0,0, 10000, 0,0,0,"","","","");

        cheapestPartList = new ArrayList<>();

        double distance = 0;
        double bestFit = 10000;
        double bestFitSupportPost = 10000;
        double bestFitRoofTile = 10000;

        for (CarportPart part : dbPartsList){
            if(part.getType() == cheapestBeam.getType()){
                distance = beamLength - part.getDBlength();

                if (distance <= 0 && cheapestBeam.getDBprice() > part.getDBprice()) {
                    // If the part exactly fits and it's cheaper, update cheapestBeam
                    cheapestBeam = part;
                    bestFit = distance;
                } else if (distance > 0 && distance < bestFit) {
                    // If the part is longer but closer to the target length, update cheapestBeam
                    cheapestBeam = part;
                    bestFit = distance;
                }
            }

            if(part.getType() == cheapestSupport.getType()){
                    distance = part.getDBlength() - supportPostLength;

                    if (distance <= 0 && cheapestSupport.getDBprice() > part.getDBprice()) {
                        // If the part exactly fits and it's cheaper, update cheapestBeam
                        cheapestSupport = part;
                        bestFitSupportPost = distance;
                    } else if (distance > 0 && distance < bestFitSupportPost) {
                        // If the part is longer but closer to the target length, update cheapestBeam
                            cheapestSupport = part;
                            bestFitSupportPost = distance;
                    }
            }

            if(part.getType() == cheapestRaft.getType() && part.getDBlength() >= cheapestRaft.getDBlength()){
                distance = raftLength - part.getDBlength();
                if(distance <= 0 && cheapestRaft.getDBprice() > part.getDBprice()){
                    cheapestRaft = part;
                }
            }

            if(part.getType() == cheapestCrossSupport.getType() && part.getDBlength() >= cheapestCrossSupport.getDBlength()){
                distance = carport.getLength()- carport.getShedLength() - part.getDBlength();
                if(distance <= 0 && cheapestCrossSupport.getDBprice() > part.getDBprice()){
                    cheapestCrossSupport = part;
                }
            }

            if(part.getType() == cheapestRooftile.getType() && carport.isWithRoof()){
                distance = roofTile - part.getDBlength();

                if (distance <= 0) {
                    if(cheapestRooftile.getDBwidth() == 0){
                    // If the part exactly fits and it's cheaper, update cheapestBeam
                    cheapestRooftile = part;
                    bestFitRoofTile = distance;
                    } else if (part.getDBprice() < cheapestRooftile.getDBprice() || part.getDBlength() > cheapestRooftile.getDBlength()) {
                        cheapestRooftile = part;
                        bestFitRoofTile = distance;
                    }
                } else if (distance > 0 && distance < bestFitRoofTile) {
                    // If the part is longer but closer to the target length, update cheapestBeam
                    cheapestRooftile = part;
                    bestFitRoofTile = distance;
                }
            }


            if(part.getType() == cheapestFrontShedWood.getType() && carport.isWithShed()){
                distance = frontShedWood - part.getDBlength();

                if (distance <= 0) {
                    if(cheapestFrontShedWood.getDBwidth() == 0){
                        // If the part exactly fits and it's cheaper, update cheapestBeam
                        cheapestFrontShedWood = part;
                        bestFitRoofTile = distance;
                    } else if (part.getDBprice() < cheapestFrontShedWood.getDBprice() || part.getDBlength() > cheapestFrontShedWood.getDBlength()) {
                        cheapestFrontShedWood = part;
                        bestFitRoofTile = distance;
                    }
                } else if (distance > 0 && distance < bestFitRoofTile) {
                    // If the part is longer but closer to the target length, update cheapestBeam
                    cheapestFrontShedWood = part;
                    bestFitRoofTile = distance;
                }
            }


        }
        int beamQuantityNeeded = (int) Math.ceil(beamLength * carport.getBEAM().getQuantity() / cheapestBeam.getDBlength());
        //int supportPostQuantityNeeded = (int) Math.ceil(supportPostLength * carport.getSUPPORTPOST().getQuantity() / cheapestSupport.getDBlength());
        int raftQuantityNeeded = (int) Math.ceil(raftLength * carport.getRAFT().getQuantity() / cheapestRaft.getDBlength());
        int roofTileQuantityNeeded = (int) Math.ceil(carport.getWidth() / cheapestRooftile.getDBwidth());

        int crossSupportQuantityNeeded = (int) Math.ceil((Math.sqrt(carport.getWidth()*carport.getWidth() + carport.getLength()- carport.getShedLength()* carport.getLength()- carport.getShedLength())/cheapestCrossSupport.getDBlength())*2);


        cheapestBeam.setQuantity(beamQuantityNeeded);
        cheapestSupport.setQuantity(carport.getSUPPORTPOST().getQuantity());
        cheapestRaft.setQuantity(raftQuantityNeeded);
        cheapestCrossSupport.setQuantity(crossSupportQuantityNeeded);
        cheapestRooftile.setQuantity(roofTileQuantityNeeded);

        cheapestPartList.add(cheapestBeam);
        cheapestPartList.add(cheapestSupport);
        cheapestPartList.add(cheapestRaft);
        cheapestPartList.add(cheapestCrossSupport);

        if(carport.isWithRoof()){

        cheapestPartList.add(cheapestRooftile);

        totalPrice = (cheapestBeam.getDBprice() * cheapestBeam.getQuantity())
                + (cheapestRaft.getDBprice() * cheapestRaft.getQuantity())
                + (cheapestSupport.getDBprice() * cheapestSupport.getQuantity())
                + (cheapestCrossSupport.getDBprice() * cheapestCrossSupport.getQuantity())
                + (cheapestRooftile.getDBprice() * cheapestRooftile.getQuantity());
        } else {
            totalPrice = (cheapestBeam.getDBprice() * cheapestBeam.getQuantity())
                    + (cheapestRaft.getDBprice() * cheapestRaft.getQuantity())
                    + (cheapestSupport.getDBprice() * cheapestSupport.getQuantity())
                    + (cheapestCrossSupport.getDBprice() * cheapestCrossSupport.getQuantity());
        }
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public ArrayList<CarportPart> getCheapestPartList() {
        return cheapestPartList;
    }
}
