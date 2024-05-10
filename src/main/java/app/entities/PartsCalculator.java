package app.entities;

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
        MaterialCalculationOnlyMaxLength();
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
    public void chatGPTCalculation(){
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

    public void MaterialCalculationOnlyMaxLength(){
        double supportPostLength = (90 + carport.getHeight()) * carport.getSUPPORTPOST().getQuantity();
        double beamLength = carport.getLength() * carport.getBEAM().getQuantity();
        double raftLength = carport.getWidth() * carport.getRAFT().getQuantity();

        CarportPart cheapestBeam1 = null;
        CarportPart cheapestBeam2 = null;
        CarportPart cheapestSupport1 = null;
        CarportPart cheapestSupport2 = null;
        CarportPart cheapestRaft1 = null;
        CarportPart cheapestRaft2 = null;
        CarportPart cheapestCrossSupport1 = null;
        CarportPart cheapestCrossSupport2 = null;
        cheapestPartList = new ArrayList<>();

        double minCost = Double.MAX_VALUE;

        // Find the combination of two parts with the lowest cost for each component
        for (CarportPart beam1 : dbPartsList) {
            for (CarportPart beam2 : dbPartsList) {
                for (CarportPart support1 : dbPartsList) {
                    for (CarportPart support2 : dbPartsList) {
                        for (CarportPart raft1 : dbPartsList) {
                            for (CarportPart raft2 : dbPartsList) {
                                for (CarportPart crossSupport1 : dbPartsList) {
                                    for (CarportPart crossSupport2 : dbPartsList) {
                                        int beamQuantityNeeded = (int) Math.ceil(beamLength / (beam1.getDBlength() + beam2.getDBlength()));
                                        int supportPostQuantityNeeded = (int) Math.ceil(supportPostLength / (support1.getDBlength() + support2.getDBlength()));
                                        int raftQuantityNeeded = (int) Math.ceil(raftLength / (raft1.getDBlength() + raft2.getDBlength()));
                                        int crossSupportQuantityNeeded = (int) Math.ceil(Math.sqrt(carport.getWidth() * carport.getWidth() + carport.getLength() * carport.getLength()) / (crossSupport1.getDBlength() + crossSupport2.getDBlength())) * 2;

                                        double totalPrice = (beam1.getDBprice() * beamQuantityNeeded * beam1.getQuantity()) + (beam2.getDBprice() * beamQuantityNeeded * beam2.getQuantity()) + (raft1.getDBprice() * raftQuantityNeeded * raft1.getQuantity()) + (raft2.getDBprice() * raftQuantityNeeded * raft2.getQuantity()) + (support1.getDBprice() * supportPostQuantityNeeded * support1.getQuantity()) + (support2.getDBprice() * supportPostQuantityNeeded * support2.getQuantity()) + (crossSupport1.getDBprice() * crossSupportQuantityNeeded * crossSupport1.getQuantity()) + (crossSupport2.getDBprice() * crossSupportQuantityNeeded * crossSupport2.getQuantity());

                                        if (totalPrice < minCost) {
                                            minCost = totalPrice;
                                            cheapestBeam1 = beam1;
                                            cheapestBeam2 = beam2;
                                            cheapestSupport1 = support1;
                                            cheapestSupport2 = support2;
                                            cheapestRaft1 = raft1;
                                            cheapestRaft2 = raft2;
                                            cheapestCrossSupport1 = crossSupport1;
                                            cheapestCrossSupport2 = crossSupport2;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Check if using two pieces is cheaper than using one piece
        double costUsingTwoPieces = minCost;
        double costUsingOnePiece = 0.0;

        for (CarportPart part : dbPartsList) {
            int quantityNeeded = (int) Math.ceil(beamLength / part.getDBlength());
            double totalPrice = part.getDBprice() * quantityNeeded * part.getQuantity();
            if (totalPrice < costUsingOnePiece) {
                costUsingOnePiece = totalPrice;
            }
        }

        if (costUsingOnePiece < costUsingTwoPieces) {
            // Use only one piece for each component
            int beamQuantityNeeded = (int) Math.ceil(beamLength / cheapestBeam1.getDBlength()) * cheapestBeam1.getQuantity();
            int supportPostQuantityNeeded = (int) Math.ceil(supportPostLength / cheapestSupport1.getDBlength()) * cheapestSupport1.getQuantity();
            int raftQuantityNeeded = (int) Math.ceil(raftLength / cheapestRaft1.getDBlength()) * cheapestRaft1.getQuantity();
            int crossSupportQuantityNeeded = (int) Math.ceil(Math.sqrt(carport.getWidth() * carport.getWidth() + carport.getLength() * carport.getLength()) / cheapestCrossSupport1.getDBlength()) * 2 * cheapestCrossSupport1.getQuantity();

            cheapestBeam1.setQuantity(beamQuantityNeeded);
            cheapestSupport1.setQuantity(supportPostQuantityNeeded);
            cheapestRaft1.setQuantity(raftQuantityNeeded);
            cheapestCrossSupport1.setQuantity(crossSupportQuantityNeeded);

            cheapestPartList.add(cheapestBeam1);
            cheapestPartList.add(cheapestSupport1);
            cheapestPartList.add(cheapestRaft1);
            cheapestPartList.add(cheapestCrossSupport1);
        } else {
            // Use combination of two pieces for each component
            cheapestBeam1.setQuantity((int) Math.ceil(beamLength / cheapestBeam1.getDBlength()) * cheapestBeam1.getQuantity());
            cheapestBeam2.setQuantity((int) Math.ceil(beamLength / cheapestBeam2.getDBlength()) * cheapestBeam2.getQuantity());
            cheapestSupport1.setQuantity((int) Math.ceil(supportPostLength / cheapestSupport1.getDBlength()) * cheapestSupport1.getQuantity());
            cheapestSupport2.setQuantity((int) Math.ceil(supportPostLength / cheapestSupport2.getDBlength()) * cheapestSupport2.getQuantity());
            cheapestRaft1.setQuantity((int) Math.ceil(raftLength / cheapestRaft1.getDBlength()) * cheapestRaft1.getQuantity());
            cheapestRaft2.setQuantity((int) Math.ceil(raftLength / cheapestRaft2.getDBlength()) * cheapestRaft2.getQuantity());
            cheapestCrossSupport1.setQuantity((int) Math.ceil(Math.sqrt(carport.getWidth() * carport.getWidth() + carport.getLength() * carport.getLength()) / cheapestCrossSupport1.getDBlength()) * 2 * cheapestCrossSupport1.getQuantity());
            cheapestCrossSupport2.setQuantity((int) Math.ceil(Math.sqrt(carport.getWidth() * carport.getWidth() + carport.getLength() * carport.getLength()) / cheapestCrossSupport2.getDBlength()) * 2 * cheapestCrossSupport2.getQuantity());

            cheapestPartList.add(cheapestBeam1);
            cheapestPartList.add(cheapestBeam2);
            cheapestPartList.add(cheapestSupport1);
            cheapestPartList.add(cheapestSupport2);
            cheapestPartList.add(cheapestRaft1);
            cheapestPartList.add(cheapestRaft2);
            cheapestPartList.add(cheapestCrossSupport1);
            cheapestPartList.add(cheapestCrossSupport2);
        }

        totalPrice = minCost;
    }




    public double getTotalPrice() {
        return totalPrice;
    }

    public ArrayList<CarportPart> getCheapestPartList() {
        return cheapestPartList;
    }
}
