package app.controllers;

import app.entities.Carport;
import app.entities.CarportPart;
import app.persistence.CarportPartMapper;
import app.persistence.ConnectionPool;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

public class CalculationController {



   public static double calculateFullCarportPrice(Context ctx, Carport carport, ConnectionPool connectionPool){
      double fullCarportPrice = 0;
      ArrayList<CarportPart> partsList = new ArrayList<>();

      double beamsPrice = calculateBeamsPrice(ctx,carport,connectionPool,partsList);
      double raftersPrice = calculateRaftPrice(ctx,carport,connectionPool,partsList);
      double supportPostsPrice = calculateSupportPostPrice(ctx,carport,connectionPool,partsList);

      fullCarportPrice = beamsPrice + raftersPrice + supportPostsPrice; //+ rafters + other prices

      ctx.sessionAttribute("partslistLukas", partsList);

      System.out.println("Full price for the carport: " + fullCarportPrice);
      return fullCarportPrice;
   }

   public void handlePartsRequest(Context ctx, Carport carport, ConnectionPool connectionPool) {
      List<CarportPart> partsList = new ArrayList<>();

      partsList.add(CarportPartMapper.getBeamDetails((int)carport.getLength(), connectionPool));
      partsList.add(CarportPartMapper.getRaftDetails((int)carport.getWidth(), connectionPool));
      partsList.add(CarportPartMapper.getSupportPostDetails((int)carport.getHeight(), connectionPool));

      ctx.sessionAttribute("partslist", partsList);
   }
private static double calculateBeamsPrice(Context ctx, Carport carport, ConnectionPool connectionPool,ArrayList<CarportPart> partsList){
   double totalBeamsPrice = 0.0;
   int carportLength = (int) carport.getLength();

   int beamQuantity = 0;
   /*for (CarportPart part : carport.getCarportPartList()) {
      if (part.getType() == CarportPart.CarportPartType.BEAM) {
         beamQuantity += part.getQuantity();
      }
   }*/

   beamQuantity = carport.getBEAM().getQuantity();
   if(carportLength > 600){
      beamQuantity++;
   }

   CarportPart beam = CarportPartMapper.getBeamDetails(carportLength,connectionPool);

   ctx.sessionAttribute("beam",beam);

      System.out.println("material name: " + beam.getDBname());
      System.out.println("beam Length: " + carportLength);
      System.out.println("beam Quantity: " + beamQuantity);
      System.out.println("Price per Beam: " + beam.getDBprice());

      totalBeamsPrice = beam.getDBprice() * beamQuantity;
   beam.setQuantity(beamQuantity);
   partsList.add(beam);

   return totalBeamsPrice;
}

   private static double calculateSupportPostPrice(Context ctx, Carport carport, ConnectionPool connectionPool,ArrayList<CarportPart> partsList){
      double totalSupportPostPrice = 0.0;
      int carportHeight = (int) carport.getHeight() + 90; //+ 90, fordi at stolpen skal være 90cm nede i jorden

      int supportPostQuantity = 0;
      /*for (CarportPart part : carport.getCarportPartList()) {
         if (part.getType() == CarportPart.CarportPartType.SUPPORTPOST) {
            supportPostQuantity += part.getQuantity();
         }
      }*/
      supportPostQuantity = carport.getSUPPORTPOST().getQuantity();

      //double oneSupportPostPrice = PartsMapper.getSupportPostPrice(carportHeight, connectionPool);
      CarportPart supportpost = CarportPartMapper.getSupportPostDetails(carportHeight,connectionPool);
      ctx.sessionAttribute("supportpost",supportpost);
      System.out.println("Carport Height: " + carportHeight);
      System.out.println("Support post Quantity: " + supportPostQuantity);
      System.out.println("Price per Support post: " + supportpost.getDBprice());

      totalSupportPostPrice = supportpost.getDBprice() * supportPostQuantity;
      supportpost.setQuantity(supportPostQuantity);
      partsList.add(supportpost);
      return totalSupportPostPrice;
   }

   private static double calculateRaftPrice(Context ctx, Carport carport, ConnectionPool connectionPool,ArrayList<CarportPart> partsList){
      double totalSupportPostPrice = 0.0;
      int carportWidth = (int) carport.getWidth();

      int raftQuantity = 0;
      /*for (CarportPart part : carport.getCarportPartList()) {
         if (part.getType() == CarportPart.CarportPartType.RAFT) {
            raftQuantity += part.getQuantity();
         }
      }*/
      raftQuantity = carport.getRAFT().getQuantity();

      //double oneRaftPrice = PartsMapper.getRaftPrice(carportWidth, connectionPool);
      CarportPart raft = CarportPartMapper.getRaftDetails(carportWidth,connectionPool);
      ctx.sessionAttribute("raft",raft);
      System.out.println("raft Length: " + carportWidth);
      System.out.println("raft Quantity: " + raftQuantity);
      System.out.println("Price per Beam: " + raft.getDBprice());

      totalSupportPostPrice = raft.getDBprice() * raftQuantity;
      raft.setQuantity(raftQuantity);
      partsList.add(raft);
      return totalSupportPostPrice;
   }

}
