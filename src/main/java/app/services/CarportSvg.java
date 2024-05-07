package app.services;

//import app.controllers.QuantityOFEachMaterialController;
import app.entities.Carport;
import app.entities.CarportPart;
import app.persistence.ConnectionPool;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.HashMap;

public class CarportSvg {
    private Context ctx;
    ConnectionPool connectionPool;
    String type;
    private int length;
    private int width;
    private int height;
    private Svg carportSvg;
    private Svg carportOuterSvg;
    private int x1 = 0;
    private int x2 = 0;

    int totalPoles;
    int totalBeams;
    int totalRafters;
    int totalCrossSupports;

   public CarportSvg(int width, int length, int height) {
        this.length = length;
        this.width = width;
        this.height = height;
       carportSvg = new Svg(75, 0, "0 0 " + (length+80) + " " + (width+80), "100%");
       carportOuterSvg = new Svg(0, 0, "0 0 " + (length+90) + " " + (width+80), "100%");

       carportSvg.addRectangle(0, 0, width, length, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        totalPoles = addPoles(width, length, height);
        totalBeams = addBeams(length, width);
        totalRafters = addRafters(width, length);
        totalCrossSupports = addCrossSupport(width, length);
        addArrows(width, length);
        addTextV((length / 2) + 30, width + 55, 0, "" + length + " cm");
        addTextH(40, (width / 2) - 20, 0, "" + width + " cm");
        addTextH(10, (width / 2) - 20, 0, "" + (width-65) + " cm");
   }


   public HashMap<String, Integer> getMaterialQuantity(){
        HashMap<String, Integer> quantityMap = new HashMap<String, Integer>();
        quantityMap.put("totalPoles", totalPoles);
        quantityMap.put("totalBeams", totalBeams);
        quantityMap.put("totalRafters", totalRafters);
        quantityMap.put("totalCrossSupports", totalCrossSupports);

       return quantityMap;
    }
   // Remme er placeret 35 nede og går på tværs og er lige så lange spm den indtastede længde
   private int addBeams(int width, int length) {
        carportSvg.addRectangle(0, 35, 4.5, width, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        carportSvg.addRectangle(0, (length - 35), 4.5, width, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        return 2;
   }

    // Spær er placeret for hver 55.174 cm og går lodret
    private int addRafters(int width, int length) {
       int raftCounter = 0;
        for (double i = 0; i < length; i += 55.714) {
            carportSvg.addRectangle(i, 0.0, width, 6.5, "stroke:#000000; fill: #ffffff");
            raftCounter++;
        }
        return raftCounter;
    }
    //Kryds er de hulbånd som skaber et kryds. De stabiliserer Carporten
    private int addCrossSupport(int width, int length) {
        carportSvg.addLine(54, 35, length - 58, width - 30, "stroke:#000000; fill: #ffffff");
        carportSvg.addLine(58, 35, length - 54, width - 30, "stroke:#000000; fill: #ffffff");
        carportSvg.addLine(54, width - 30.5, length - 58, 35, "stroke:#000000; fill: #ffffff");
        carportSvg.addLine(58, width - 30.5, length - 54, 35, "stroke:#000000; fill: #ffffff");
        return 2;
    }

    //Stolperne er placeret for hver 310 cm og går 90 cm. ned i jorden
    public int addPoles(int width, int length, int height) {
       int poleCounter = 0;
        if ((length - 102) > 310) {
            for (int h = 51; h < length; h += 310) {
                x1 = h;
                carportSvg.addRectangle(h, 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                carportSvg.addRectangle(h, width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                poleCounter += 2;
                System.out.println(poleCounter);
            }
        } else {
            poleCounter = 4;
            x2 = length-51;
            carportSvg.addRectangle(51, 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            carportSvg.addRectangle(51, width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            carportSvg.addRectangle(length - 51, 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            carportSvg.addRectangle(length - 51, width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        }
        return poleCounter;
    }

    //  Pilene er placeret til venstre for y-aksen og neddenfor X-visen og er med til at vise bredde og højde på carporten.
    //  Den tredje pil viser bredden imellem de 20 remme
    public void addArrows(int width, int length) {
        carportOuterSvg.addArrow(50, width, 50, 0, "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addArrow(75, width + 30, length + 75, width + 30, "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addArrow(20, width-28, 20, 32, "stroke:#000000; marker-end: url(#endArrow);");

    }

    // Den horizontale tekst
    public void addTextH(int width, int length, int rotate, String text) {
        carportOuterSvg.addTextH(width, length, rotate, text, "text-anchor: middle;");
    }

    //Den vertikale tekst
    public void addTextV(int width, int length, int rotate, String text) {
        carportOuterSvg.addTextV(width, length, rotate, text, "text-anchor: middle;");
    }

    @Override
    public String toString() {
        carportOuterSvg.addSvg(carportSvg);
        return carportOuterSvg.toString();
    }

}