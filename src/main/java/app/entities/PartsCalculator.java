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

        CarportPart cheapestBeam = new CarportPart(CarportPart.CarportPartType.BEAM, 0,0, 10000, 0,0,0,"","","","");
        CarportPart cheapestSupport = new CarportPart(CarportPart.CarportPartType.SUPPORTPOST, 0,0, 10000, 0,0,0,"","","","");
        CarportPart cheapestRaft = new CarportPart(CarportPart.CarportPartType.RAFT, 0,0, 10000, 0,0,0,"","","","");
        CarportPart cheapestCrossSupport = new CarportPart(CarportPart.CarportPartType.CROSSSUPPORT, 0,0, 10000, 0,0,0,"","","","");
        cheapestPartList = new ArrayList<>();

        double distance = 0;
        double bestFit = 10000;
        double bestFitSupportPost = 10000;
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

                //cheapestRaft = part;
            }
            if(part.getType() == cheapestCrossSupport.getType() && part.getDBlength() >= cheapestCrossSupport.getDBlength()){
                distance = beamLength - part.getDBlength();
                if(distance <= 0 && cheapestCrossSupport.getDBprice() > part.getDBprice()){
                    cheapestCrossSupport = part;
                }
                //cheapestCrossSupport = part;
            }
        }
        int beamQuantityNeeded = (int) Math.ceil(beamLength * carport.getBEAM().getQuantity() / cheapestBeam.getDBlength());
        int supportPostQuantityNeeded = (int) Math.ceil(supportPostLength * carport.getSUPPORTPOST().getQuantity() / cheapestSupport.getDBlength());
        int raftQuantityNeeded = (int) Math.ceil(raftLength * carport.getRAFT().getQuantity() / cheapestRaft.getDBlength());

        int crossSupportQuantityNeeded = (int) Math.ceil(Math.sqrt(carport.getWidth()*carport.getWidth() + carport.getLength()* carport.getLength())/cheapestCrossSupport.getDBlength()) *2;


        cheapestBeam.setQuantity(beamQuantityNeeded);
        cheapestSupport.setQuantity(carport.getSUPPORTPOST().getQuantity());
        cheapestRaft.setQuantity(raftQuantityNeeded);
        cheapestCrossSupport.setQuantity(crossSupportQuantityNeeded);


        cheapestPartList.add(cheapestBeam);
        cheapestPartList.add(cheapestSupport);
        cheapestPartList.add(cheapestRaft);
        cheapestPartList.add(cheapestCrossSupport);

        totalPrice = (cheapestBeam.getDBprice() * cheapestBeam.getQuantity()) + (cheapestRaft.getDBprice() * cheapestRaft.getQuantity()) + (cheapestSupport.getDBprice() * cheapestSupport.getQuantity()) + (cheapestCrossSupport.getDBprice() * cheapestCrossSupport.getQuantity());
    }

//**********************************CHATGPT CALC******************************************************************************************************
    public void chatGPTCalculation(){
        double beamLength = carport.getLength();
        double supportPostLength = 90 + carport.getHeight();
        double raftLength = carport.getWidth();

        // Initialize variables for cheapest one-piece and two-piece combinations
        CarportPart cheapestOnePiece = null;
        CarportPart cheapestTwoPiecesPart1 = null;
        CarportPart cheapestTwoPiecesPart2 = null;

        // Calculate quantities needed for each component
        int beamQuantityNeeded = (int) Math.ceil(beamLength / getCheapestPartLengthOfType(CarportPart.CarportPartType.BEAM));
        int supportPostQuantityNeeded = (int) Math.ceil(supportPostLength / getCheapestPartLengthOfType(CarportPart.CarportPartType.SUPPORTPOST));
        int raftQuantityNeeded = (int) Math.ceil(raftLength / getCheapestPartLengthOfType(CarportPart.CarportPartType.RAFT));
        int crossSupportQuantityNeeded = (int) Math.ceil(Math.sqrt(beamLength * beamLength + raftLength * raftLength) / getCheapestPartLengthOfType(CarportPart.CarportPartType.CROSSSUPPORT)) * 2;

        // Calculate total cost using one piece for each component
        double totalPriceOnePiece = calculateTotalPriceOnePiece(beamQuantityNeeded, supportPostQuantityNeeded, raftQuantityNeeded, crossSupportQuantityNeeded);

        // Iterate through each part to find the cheapest two-piece combination
        for (CarportPart part1 : dbPartsList) {
            for (CarportPart part2 : dbPartsList) {
                if (part1 != part2) {
                    double totalPriceTwoPieces = calculateTotalPriceTwoPieces(part1, part2, beamLength, supportPostLength, raftLength, crossSupportQuantityNeeded);
                    if (totalPriceTwoPieces < totalPriceOnePiece) {
                        // Update cheapest two-piece combination if found
                        totalPriceOnePiece = totalPriceTwoPieces;
                        cheapestOnePiece = null;
                        cheapestTwoPiecesPart1 = part1;
                        cheapestTwoPiecesPart2 = part2;
                    }
                }
            }
        }

        // Set quantities and add parts to the list based on the cheapest option
        if (cheapestOnePiece != null) {
            setQuantityAndAddToCheapestList(cheapestOnePiece, beamQuantityNeeded);
            setQuantityAndAddToCheapestList(cheapestOnePiece, supportPostQuantityNeeded);
            setQuantityAndAddToCheapestList(cheapestOnePiece, raftQuantityNeeded);
            setQuantityAndAddToCheapestList(cheapestOnePiece, crossSupportQuantityNeeded);
        } else if (cheapestTwoPiecesPart1 != null && cheapestTwoPiecesPart2 != null) {
            setQuantityAndAddToCheapestList(cheapestTwoPiecesPart1, (int) Math.ceil(beamLength / (2 * cheapestTwoPiecesPart1.getDBlength())));
            setQuantityAndAddToCheapestList(cheapestTwoPiecesPart2, (int) Math.ceil(beamLength / (2 * cheapestTwoPiecesPart2.getDBlength())));
            setQuantityAndAddToCheapestList(cheapestTwoPiecesPart1, supportPostQuantityNeeded);
            setQuantityAndAddToCheapestList(cheapestTwoPiecesPart2, supportPostQuantityNeeded);
            setQuantityAndAddToCheapestList(cheapestTwoPiecesPart1, raftQuantityNeeded);
            setQuantityAndAddToCheapestList(cheapestTwoPiecesPart2, raftQuantityNeeded);
            setQuantityAndAddToCheapestList(cheapestTwoPiecesPart1, crossSupportQuantityNeeded);
            setQuantityAndAddToCheapestList(cheapestTwoPiecesPart2, crossSupportQuantityNeeded);
        }

        // Calculate total price
        totalPrice = calculateTotalPrice();
    }

    // Helper method to get the cheapest part length of a specific type
    private double getCheapestPartLengthOfType(CarportPart.CarportPartType type) {
        double minDBLength = Double.MAX_VALUE;
        for (CarportPart part : dbPartsList) {
            if (part.getType() == type && part.getDBlength() < minDBLength) {
                minDBLength = part.getDBlength();
            }
        }
        return minDBLength;
    }

    // Helper method to calculate total price using one piece for each component
    private double calculateTotalPriceOnePiece(int beamQuantity, int supportPostQuantity, int raftQuantity, int crossSupportQuantity) {
        double totalPrice = 0.0;
        for (CarportPart part : dbPartsList) {
            int quantity = 0;
            switch (part.getType()) {
                case BEAM:
                    quantity = beamQuantity;
                    break;
                case SUPPORTPOST:
                    quantity = supportPostQuantity;
                    break;
                case RAFT:
                    quantity = raftQuantity;
                    break;
                case CROSSSUPPORT:
                    quantity = crossSupportQuantity;
                    break;
            }
            totalPrice += part.getDBprice() * quantity;
        }
        return totalPrice;
    }

    // Helper method to calculate total price using two pieces for each component
    private double calculateTotalPriceTwoPieces(CarportPart part1, CarportPart part2, double beamLength, double supportPostLength, double raftLength, int crossSupportQuantity) {
        int quantity1 = (int) Math.ceil(beamLength / (2 * part1.getDBlength()));
        int quantity2 = (int) Math.ceil(beamLength / (2 * part2.getDBlength()));
        double totalPrice = (part1.getDBprice() * quantity1 + part2.getDBprice() * quantity2) * 2; // Multiplying by 2 because we're using two parts
        totalPrice += part1.getDBprice() * supportPostLength / part1.getDBlength(); // Support post cost
        totalPrice += part2.getDBprice() * supportPostLength / part2.getDBlength(); // Support post cost
        totalPrice += part1.getDBprice() * raftLength / part1.getDBlength(); // Raft cost
        totalPrice += part2.getDBprice() * raftLength / part2.getDBlength(); // Raft cost
        totalPrice += part1.getDBprice() * crossSupportQuantity / part1.getDBlength(); // Cross support cost
        totalPrice += part2.getDBprice() * crossSupportQuantity / part2.getDBlength(); // Cross support cost
        return totalPrice;
    }

    // Helper method to set quantity and add to cheapest list
    private void setQuantityAndAddToCheapestList(CarportPart part, int quantity) {
        part.setQuantity(quantity);
        cheapestPartList.add(part);
    }

    // Helper method to calculate total price
    private double calculateTotalPrice() {
        double totalPrice = 0.0;
        for (CarportPart part : cheapestPartList) {
            totalPrice += part.getDBprice() * part.getQuantity();
        }
        return totalPrice;
    }


//********************************************************************************************************************


    public double getTotalPrice() {
        return totalPrice;
    }

    public ArrayList<CarportPart> getCheapestPartList() {
        return cheapestPartList;
    }
}
