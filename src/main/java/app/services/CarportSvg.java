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
    private double length;
    private double width;
    private double height;
    private double shedLength;
    private double shedWidth;
    private Svg carportSvg;
    private Svg carportOuterSvg;
    private String roof;
    private int x1 = 0;
    private int x2 = 0;
    private double y1 = 16.5;
    private double y2 = y1-6.5;
    private double y3 = y1+10;

    int totalPoles;
    int totalBeams;
    int totalRafters;
    int totalCrossSupports;

   public CarportSvg(int width, int length, int height, int shedLength, int shedWidth, String roof) {
       this.length = length;
       this.width = width;
       this.height = height;
       this.shedLength = shedLength;
       this.shedWidth = shedWidth;
       this.roof = roof;
       carportSvg = new Svg(75, 75, "0 0 " + (length+180) + " " + (height+180), "100%");
       carportOuterSvg = new Svg(0, 0, "0 0 " + (length+180) + " " + (height+180), "100%");
       //carportSvg = new Svg(75, 75, "0 0 " + (length+160) + " " + (height+90), "100%");
       //carportOuterSvg = new Svg(0, 0, "0 0 " + (length+170) + " " + (height+200), "100%");
       }

   public CarportSvg(int width, int length, int height) {
       if(!roof.equals("nej")) {
           addRoof(width, length);
       }
       addRaftersFromSide(width, length);
       addBeamsFromTheSide(width, length);
       if(shedLength > 0) {
           addShed(width, length, height, shedWidth, shedLength);
        } else {
           addPolesFromTheSide(width, length, height, shedLength, shedWidth);
       }
       addArrowsForTheSidePainting(width, length, height, shedWidth, shedLength);
       addTextH(40, (height / 2) - 20+75, 0, "" + (height-26.5));
       addTextH(10, (height / 2) - 10+75, 0, "" + (height));


       double y2 = ((12.8*(length))/1000);
       addTextH(895, (height / 2) - 20+75, 0, "" + (height-((12.8*(length))/1000)));
   }
    // Nedenstående konstruktor bruger vi til at tegne Carporten set fra oven
    public CarportSvg(int width, int length, int height, int shedLength, int shedWidth) {
        this.length = length;
        this.width = width;
        this.height = height;
        this.shedLength = shedLength;
        this.shedWidth = shedWidth;
        carportSvg = new Svg(75, 75, "0 0 " + (length+180) + " " + (width+180), "100%");
        carportOuterSvg = new Svg(0, 0, "0 0 " + (length+180) + " " + (width+180), "100%");
        //carportSvg = new Svg(75, 75, "0 0 " + (length+160) + " " + (width+77), "100%");
        //carportOuterSvg = new Svg(75, 75, "0 0 " + (length+170) + " " + (width+200), "100%");

       carportSvg.addRectangle(0, 0, width, length, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        totalPoles = addPoles(width, length, height);
        totalBeams = addBeams(length, width);
        totalRafters = addRafters(width, length);
        totalCrossSupports = addCrossSupport(width, length);
        addLineWithStroke(width, length);
        addArrowsForTheTopPainting(width, length);
        addTextV((length / 2) + 30, width + 55, 0, "" + length + " cm");
        addTextH(40, (width / 2) - 20, 0, "" + width + " cm");
        addTextH(10, (width / 2) - 20, 0, "" + (width-65) + " cm");
   }

    //i så fald kunden har valgt at tilkøbe et skur tilføjer vi det her
    private void addShed(int width, int length, int height, int shedWidth, int shedLength) {

        //Adding boards to the shed
        int x = length - 30 - shedLength;
        double y = ((12.8*x)/1000)+20;
        System.out.println("skurets bredde er "+shedLength);
        System.out.println("Skurets først brædt har koordinaterne "+x+","+y);

        for(int k = 0; k < shedLength; k = k+10) {
            y = (12.8*(k+x))/1000;
            carportSvg.addRectangle(k+x, y+26.5, height-y, 10, "stroke:#000000; fill: #ffffff");
        }
        //Adding poles
        if ((length - 142 - shedLength) > 310) {
            y = ((12.8*100)/1000);
            carportSvg.addRectangle(100, y+26.5, height-y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            y = ((12.8*410)/1000);
            carportSvg.addRectangle(410, y+26.5, height-y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        } else {
            y = ((12.8*100)/1000);
            carportSvg.addRectangle(100, y+26.5, height-y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        }
        // De nedenstående to stolper skal kun med på tegningen set fra oven
        //carportSvg.addRectangle((length-shedLength-30), 32.5, height+y3+6.5, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        //carportSvg.addRectangle(length-42, 34.5, height+y3+8.5, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");

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
   public int addBeams(int width, int length) {
        carportSvg.addRectangle(0, 35, 4.5, width, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        carportSvg.addRectangle(0, (length - 35), 4.5, width, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        return 2;
   }

    // Remme set fra siden med et fald på 12,8%
    private void addBeamsFromTheSide(int width, int length) {
        double y2 = (12.8*(length))/1000;
        carportSvg.addLine(0, 16.5,length+6.5, 16.5+y2, "stroke:#000000; fill: #ffffff");
        carportSvg.addLine(0, 16.5+10,length+6.5, 16.5+y2+10, "stroke:#000000; fill: #ffffff");
        //lodrette streger der forbinder de to linier
        carportSvg.addLine(0, 16.5, 0,16.5+10, "stroke:#000000; fill: #ffffff");
        carportSvg.addLine(length+6.5,16.5+y2,length+6.5,16.5+y2+10, "stroke:#000000; fill: #ffffff");
    }

    // Taget set fra siden med et fald på 12,8%
    private void addRoof(int width, int length) {
        double y2 = (12.8*(length))/1000;
        carportSvg.addLine(0, 0, length+6.5, y2, "stroke:#000000; fill: #ffffff");
        carportSvg.addLine(0, 10, length+6.5, y2+10, "stroke:#000000; fill: #ffffff");
        //lodrette streger der forbinder de to linier
        carportSvg.addLine(0, 0, 0,10, "stroke:#000000; fill: #ffffff");
        carportSvg.addLine(length+6.5, y2, length+6.5,y2+10, "stroke:#000000; fill: #ffffff");
    }

    // Spær er placeret for hver 55.174 cm og går lodret
    private int addRafters(int width, int length) {
        int raftCounter = 0;
       double x = 0;
        int loop = 0;
        for (double i = 0; i < length; i += 55.714) {
            loop += 1;
            x = i+(loop*6.5);
            if((i+75+55.714)<(length+75)) {
                carportOuterSvg.addArrow(i + 75, 50, (i + 75 + 55.714), 50, "stroke:#000000; marker-end: url(#endArrow);");
                addTextV(i + 20 + 75, 30, 0, "55");
            }
            carportOuterSvg.addLine(i+75, 44, i+75,56, "stroke:#000000; fill: #ffffff");
            carportSvg.addRectangle(i, 0.0, width, 6.5, "stroke:#000000; fill: #ffffff");
            raftCounter++;
        }
        return raftCounter;
    }


    // Spær set fra siden igen har taget et fald på 12,8%
    private void addRaftersFromSide(int width, int length) {

        for (double i = 0; i < length; i += 55.714) {
            double y2 = (12.8*(i))/1000;
            carportSvg.addRectangle(i, y2+10, 6.5, 6.5, "stroke:#000000; fill: #ffffff");
        }
    }

    //Kryds er de hulbånd som skaber et kryds. De stabiliserer Carporten
    private int addLineWithStroke(int width, int length) {
        if(shedLength == 0) {
            carportSvg.addLineWithStroke(54, 35, length - 58, width - 30, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(58, 35, length - 54, width - 30, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(54, width - 30.5, length - 58, 35, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(58, width - 30.5, length - 54, 35, "stroke:#000000; fill: #ffffff");
        } else {
            double x = (length - 42 - shedLength);
            carportSvg.addLineWithStroke(54, 35, x, width - 30, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(58, 35, x+4, width - 30, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(54, width - 30.5, x, 35, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(58, width - 30.5, x+4, 35, "stroke:#000000; fill: #ffffff");
            //skurets stiplede linier
            //først de venstreplacerede lodrette
            double y1 = width - shedWidth+31;
            double y2 = width-26;
            x = (length - 30 - shedLength);
            carportSvg.addLineWithStroke(x, y1, x, y2, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(x+1, y1, x+1, y2, "stroke:#000000; fill: #ffffff");
            // Herefter de højreplacerede lodrette
            x = (length-31);
            carportSvg.addLineWithStroke(x, y1, x, y2, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(x+1, y1,x+1, y2, "stroke:#000000; fill: #ffffff");
            //dernæst de øverste vandrette
            y1 = width - shedWidth+31;
            double x1 = (length - 30 - shedLength);
            double x2 = (length-32);
            carportSvg.addLineWithStroke(x1, y1, x2, y1, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(x1, y1+1, x2, y1+1, "stroke:#000000; fill: #ffffff");
            // og til sidst de nederste vandrette
            y2 = width-27;
            carportSvg.addLineWithStroke(x1, y2, x2, y2, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(x1,y2+1,x2, y2+1, "stroke:#000000; fill: #ffffff");
        }
        return 2;
    }

    //Stolperne er placeret for hver 310 cm og går 90 cm. ned i jorden
    //Nedenstående metode bruges til tegning af stolper i tegningen set fra oven
    public int addPoles(int width, int length, int height, int shedLength, int shedWidth) {
        int poleCounter = 0;
       double x = 0;
        double y = 0;
        //Først placerer vi de 4 yderstolper på carporten
        carportSvg.addRectangle(100, 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        carportSvg.addRectangle(100, width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        carportSvg.addRectangle(length - 42, 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        carportSvg.addRectangle(length - 42, width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        poleCounter = 4;
        //Så undersøger vi om kunden har har valgt at få et skur med.
        // Hvis nej er shedLength = 0
        if (shedLength == 0) {
            if ((length - 142) > 310 && (length - 142) <= 620) {
                x = ((length - 142) / 2);
                carportSvg.addRectangle(x + 100, 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                carportSvg.addRectangle(x + 100, width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                poleCounter += 2;
            } else if ((length - 142) > 620) {
                x = (length - 142) / 3;
                carportSvg.addRectangle(100 + x, 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                carportSvg.addRectangle(100 + x, width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                carportSvg.addRectangle(100 + (2 * x), 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                carportSvg.addRectangle(100 + (2 * x), width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                poleCounter += 4;
            }
            //Hvis kunden har valgt at få et skur med
        } else {
            // Her placerer vi skurets bærende yderstolper
            y = width - shedWidth+31;
            x = (length - 30 - shedLength);
            //System.out.println("øverste venstre stolpe til skur har "+x+","+y);
            carportSvg.addRectangle(x, y, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            y = width-38;
            //System.out.println("nederste venstre stolpe til skur har "+x+","+y);
            carportSvg.addRectangle(x, y, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            // Her placerer vi skurets øverste højre stolpe
            y = width - shedWidth+31;
            x = (length - 42);
            //System.out.println("øverste højre stolpe til skur har "+x+","+y);
            carportSvg.addRectangle(x, y, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            poleCounter += 2;

            // Her undersøger vi om skurets bredde er større end 310 cm. I så fald skal vi have placeret 2 bærende midterstolper
            if (shedWidth > 310) {
                y = (shedWidth/2) + (width-shedWidth);
                x = (length - 30 - shedLength);
                //System.out.println("midterste venstre bærende stolpe til skuret har "+x+","+y);
                carportSvg.addRectangle(x, y, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                x = (length - 42);
                //System.out.println("midterste højre bærende stolpe til skuret har "+x+","+y);
                carportSvg.addRectangle(x, y, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                poleCounter += 2;
            }
            // Hvis rummet imellem carportens venstre yderstolper og skurets venstrestolper er større end 322cm(310+stolpens bredde)
            // så tilføjer vi 2 ekstra bærende stolper til carporten
            if ((length - 142 - shedLength) > 310) {
                x = 100 + 310;
                carportSvg.addRectangle(x, 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                carportSvg.addRectangle(x, width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                poleCounter += 2;
            }
        }
        return poleCounter;
    }

    //Stolperne er placeret for hver 310 cm og går 90 cm. ned i jorden
    // Nedenstående metode bruges til tegning af stolper i tegningen set fra siden
    public void addPolesFromTheSide(int width, int length, int height, int shedLength, int shedWidth) {
        double x = 0;
        double y = 0;
        //Først placerer vi de 4 yderstolper på carporten
        y = ((12.8*100)/1000);
        carportSvg.addRectangle(100, y+26.5, height-y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        y = ((12.8*(length-42))/1000);
        carportSvg.addRectangle(length - 42, y+26.5, height-y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        //Så undersøger vi om kunden har har valgt at få et skur med.
        // Hvis nej er shedLength = 0
        if (shedLength == 0) {
            if ((length - 142) > 310 && (length - 142) <= 620) {
                x = ((length - 142) / 2);
                y = ((12.8*(x+100))/1000);
                carportSvg.addRectangle(100+x, y+26.5, height-y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");

            } else if ((length - 142) > 620) {
                x = (length - 142) / 3;
                y = (12.8*(100+x))/1000;
                carportSvg.addRectangle(100 + x, y+26.5, height-y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                y = (12.8*(100+(2*x)))/1000;
                carportSvg.addRectangle(100 + (2 * x), y+26.5, height-y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            }
        //Hvis kunden har valgt at få et skur med
        } else {
            // Her placerer vi skurets bærende yderstolper
            y = width - shedWidth+31;
            x = (length - 30 - shedLength);
            //System.out.println("øverste venstre stolpe til skur har "+x+","+y);
            carportSvg.addRectangle(x, y, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            y = width-38;
            //System.out.println("nederste venstre stolpe til skur har "+x+","+y);
            carportSvg.addRectangle(x, y, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            // Her placerer vi skurets øverste højre stolpe
            y = width - shedWidth+31;
            x = (length - 42);
            //System.out.println("øverste højre stolpe til skur har "+x+","+y);
            carportSvg.addRectangle(x, y, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");

            // Hvis rummet imellem carportens venstre yderstolper og skurets venstrestolper er større end 322cm(310+stolpens bredde)
            // så tilføjer vi 2 ekstra bærende stolper til carporten
            if ((length - 142 - shedLength) > 310) {
                y = ((12.8*100)/1000);
                x = 100 + 310;
                carportSvg.addRectangle(x, 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                carportSvg.addRectangle(x, width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            }
        }
    }
    /*
    public void addPolesFromTheSide(int width, int length, int height) {
       int x1 = length-(shedLength+30);
       double y = ((12.8*100)/1000);
       carportSvg.addRectangle(100, y+y3, height-y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
       y = ((12.8*(length-42))/1000);
       carportSvg.addRectangle(length - 42, y+y3, height-y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        if ((length - 142) < 620 && (length - 142) > 310) {
            //x1 = (length - 142)/2;
            x1 = 310;
            y = ((12.8*(100+x1))/1000);
            carportSvg.addRectangle(100+x1,y+y3, height-y, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        } else if((length -142) > 620) {
            double x2 = (length - 142)/3;
            y = ((12.8*(100+x2))/1000);
            carportSvg.addRectangle(100+x2, y+y3, height-y, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            y = ((12.8*(100+(2*x2)))/1000);
            carportSvg.addRectangle(100+(2*x2), y+y3, height-y, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        }
    }

     */


    //  Pilene er placeret til venstre for y-aksen og neddenfor X-visen og er med til at vise bredde og højde på carporten.
    //  Den tredje pil viser bredden imellem de 20 remme
    public void addArrowsForTheTopPainting(int width, int length) {
        //Pilen som angiver bredden imellem
        carportOuterSvg.addArrow(20, width+75-30, 20, 107, "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addArrow(50, width+75, 50, 75, "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addArrow(75, width + 30 + 75, length + 79, width + 30 + 75, "stroke:#000000; marker-end: url(#endArrow);");
    }

    public void addArrowsForTheSidePainting(int width, int length, int height, int shedWidth, int shedLength) {
        carportOuterSvg.addArrow(20, height+30+75, 20, 75, "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addArrow(50, height+30+75, 50, 91, "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addArrow(880, height+26.5+75, 880, (75+((int)(12.8*(length))/1000)), "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addArrow(75, height+50+75, 175, height+50+75, "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addLine(75, height+50+75-6, 75,height+50+75+6, "stroke:#000000; fill: #ffffff");
        addTextV((100 / 2) + 75-10, height + 55+90, 0, "100");
        System.out.println("pilen der går fra skurets ende til carportens ende har koordinaterne "+(length-30)+","+(height+50+75));
        // Her lægger vi 6.5 cm til da jeg har forlænget taget med 6.5 cm.
        carportOuterSvg.addArrow((length+75)-30, height+50+75, (int)(length+75+6.5), height+50+75, "stroke:#000000; marker-end: url(#endArrow);");
        addTextV((length+75-20), height + 55+90, 0, "30");
        carportOuterSvg.addLine((length+75)-30, height+50+75-6, (length+75)-30,height+50+75+6, "stroke:#000000; fill: #ffffff");
        carportOuterSvg.addLine((length+75+6.5), height+50+75-6, (length+75+6.5),height+50+75+6, "stroke:#000000; fill: #ffffff");
        if(shedWidth > 0) {
            int x1 = (length+75-30-shedLength);
            int x2 = x1 + shedLength;
            System.out.println("pilen som skal befinde sig under skuret har startkoordinatet "+(x1-75)+","+(height+50+75));
            carportOuterSvg.addArrow(x1, height+50+75, x2, height+50+75, "stroke:#000000; marker-end: url(#endArrow);");
            carportOuterSvg.addLine(x1, height+50+75-6, x1,height+50+75+6, "stroke:#000000; fill: #ffffff");
            addTextV((length+75-40-(shedLength/2)), height + 55+90, 0, "" + shedLength);
            if(((length-142)-shedLength) > 310) {
                int x = 75+100+310;
                int x3 = length+75-30-shedLength;
                int x4 = ((x3-x)/2)+x;
                //Pilen der går fra stolpen til start af skur
                System.out.println("Pilen der går fra anden stolpe til starten af skuret har startpunkt "+x+","+(height+50+75)+" og et slutpunkt der hedder ");
                carportOuterSvg.addArrow(x, height+50+75, x3, height+50+75, "stroke:#000000; marker-end: url(#endArrow);");
                carportOuterSvg.addLine(x, height+50+75-6, x,height+50+75+6, "stroke:#000000; fill: #ffffff");
                addTextV(x4-10, height + 55+90, 0, ""+((x3-x)));
                // Pilen er går fra første stolpe til anden stolpe
                 x2 = 75+100;
                 x3 = x2+310;
                System.out.println("Pilen der går fra første stolpe til anden stolpe har startpunkt "+x2+","+(height+50+75));
                carportOuterSvg.addArrow(x2, height+50+75, x3, height+50+75, "stroke:#000000; marker-end: url(#endArrow);");
                carportOuterSvg.addLine(x2, height+50+75-6, x2,height+50+75+6, "stroke:#000000; fill: #ffffff");
                addTextV((310 / 2) + 175-20, height + 55+90, 0, "310");
            } else {
                // Pilen er går fra første stolpe til start af skuret
                x1 = 75+100;
                x2 = length+75-30-shedLength;
                System.out.println("Pilen der går fra første stolpe til start af skuret "+x2+","+(height+50));
                carportOuterSvg.addArrow(x1, height + 50+75, x2, height + 50+75, "stroke:#000000; marker-end: url(#endArrow);");
                addTextV((310 / 2) + 175-20, height + 55+90, 0, ""+((length-30-shedLength)-175));
            }
        } else {
            if ((length - 142) < 620 && (length -142) > 310) {
                x1 = (length - 142)/2;
                carportOuterSvg.addArrow(175, height+50+75, x1+175, height+50+75, "stroke:#000000; marker-end: url(#endArrow);");
                addTextV((((length-142)/2)/2) + 175-20, height + 55+90, 0, ""+x1);
                carportOuterSvg.addArrow(x1+175, height+50+75, x1+x1+175, height+50+75, "stroke:#000000; marker-end: url(#endArrow);");
                addTextV((((length-142)/2)/2) + x1+175-20, height + 55+90, 0, ""+x1);
            } else if((length -142) > 620) {
                int x2 = (length - 142)/3;
                carportOuterSvg.addArrow(175, height+50+75, x2+175, height+50+75, "stroke:#000000; marker-end: url(#endArrow);");
                addTextV((((length-142)/3)/2) + 175-20, height + 55+90, 0, ""+x2);
                carportOuterSvg.addArrow(x2+175, height+50+75, x2+x2+175, height+50+75, "stroke:#000000; marker-end: url(#endArrow);");
                addTextV((((length-142)/3)/2) + x2+175-20, height + 55+90, 0, ""+x2);
                carportOuterSvg.addArrow(x2+x2+175, height+50+75, x2+x2+x2+175, height+50+75, "stroke:#000000; marker-end: url(#endArrow);");
                addTextV((((length-142)/3)/2) + x2+x2+175-20, height + 55+90, 0, ""+x2);
            }
        }

        carportOuterSvg.addArrow(75, width + 30, 175, width + 30, "stroke:#000000; marker-end: url(#endArrow);");
    }

    // Den horizontale tekst
    public void addTextH(double width, int length, int rotate, String text) {
        carportOuterSvg.addTextH(width, length, rotate, text, "text-anchor: middle;");
    }

    //Den vertikale tekst
    public void addTextV(double width, int length, int rotate, String text) {
        carportOuterSvg.addTextV(width, length, rotate, text, "text-anchor: middle;");
    }

    @Override
    public String toString() {
        carportOuterSvg.addSvg(carportSvg);
        return carportOuterSvg.toString();
    }

}