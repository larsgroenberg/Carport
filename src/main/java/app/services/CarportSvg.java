package app.services;

import io.javalin.http.Context;

public class CarportSvg {
    private final double length;
    private final double width;
    private final double height;
    private final double shedLength;
    private final double shedWidth;
    private final Svg carportSvg;
    private final Svg carportOuterSvg;
    private String roof;
    private int x1 = 0;
    private final int x2 = 0;
    private final double y1 = 16.5;
    private final double y2 = y1 - 6.5;
    private final double y3 = y1 + 10;
    private int totalPoles;
    private int totalBeams;
    private int totalRafters;
    private int totalCrossSupports;
    private double crossSupportLength;
    private int totalBoards;
    private double totalHeightOfBoards;
    private double lengthOfBoard;

    public CarportSvg(Context ctx, int width, int length, int height, double shedLength, double shedWidth, String roof) {
        this.length = length;
        this.width = width;
        this.height = height;
        this.shedLength = shedLength;
        this.shedWidth = shedWidth;
        this.roof = roof;
        carportSvg = new Svg(75, 75, "0 0 " + (length + 180) + " " + (height + 180), "100%");
        carportOuterSvg = new Svg(0, 0, "0 0 " + (length + 180) + " " + (height + 180), "100%");

        if (!roof.equals("Uden tagplader")) {
            addRoof(width, length);
        }
        addRaftersFromSide(width, length);
        addBeamsFromTheSide(length);
        if (shedLength > 0) {
            totalBoards = addShed(ctx, length, height, shedWidth, shedLength);
        } else {
            addPolesFromTheSide(width, length, height, shedLength, shedWidth);
        }
        addArrowsForTheSidePainting(width, length, height, shedWidth, shedLength);
        addTextH(40, (height / 2) - 20 + 75, 0, "" + (height - 26.5));
        addTextH(10, (height / 2) - 10 + 75, 0, "" + (height));

        double y2 = ((12.8 * (length)) / 1000);
        addTextH(length + 120, (height / 2) - 20 + 75, 0, "" + (height - ((12.8 * (length)) / 1000)));
    }

    // Nedenstående konstruktor bruger vi til at tegne Carporten set fra oven
    public CarportSvg(Context ctx, int width, int length, int height, double shedLength, double shedWidth) {
        this.length = length;
        this.width = width;
        this.height = height;
        this.shedLength = shedLength;
        this.shedWidth = shedWidth;
        carportSvg = new Svg(75, 75, "0 0 " + (length + 180) + " " + (width + 180), "100%");
        carportOuterSvg = new Svg(0, 0, "0 0 " + (length + 180) + " " + (width + 180), "100%");

        carportSvg.addRectangle(0, 0, width, length, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        totalPoles = addPoles(ctx, width, length, height, shedLength, shedWidth);
        totalBeams = addBeams(length, width);
        totalRafters = addRafters(width, length);
        totalCrossSupports = addLineWithStroke(ctx, width, length);

        //addLineWithStroke(width, length);
        addArrowsForTheTopPainting(width, length);
        addTextV((length / 2) + 30, width + 125, 0, length + " cm");
        addTextH(40, (width / 2) + 50, 0, width + " cm");
        addTextH(10, (width / 2) + 40, 0, (shedWidth) + " cm");
    }

    public int getTotalBoards() {
        return totalBoards;
    }

    public int getTotalPoles() {
        return totalPoles;
    }

    public int getTotalBeams() {
        return totalBeams;
    }

    public int getTotalRafters() {
        return totalRafters;
    }

    public int getTotalCrossSupports() {
        return totalCrossSupports;
    }


    //i så fald kunden har valgt at tilkøbe et skur tilføjer vi det her
    private int addShed(Context ctx, int length, int height, double shedWidth, double shedLength) {

        //Adding boards to the shed
        double x = length - 30 - shedLength;
        double y = ((12.8 * x) / 1000) + 20;
        totalBoards = (int) ((shedLength / 10) + (shedWidth / 10)) * 2;

        for (int k = 0; k < shedLength; k = k + 10) {
            y = (12.8 * (k + x)) / 1000;
            carportSvg.addRectangle(k + x, y + 16.5, height - y + 10, 10, "stroke:#000000; fill: #ffffff");
        }

        totalHeightOfBoards = totalHeightOfBoards * 2;
        totalHeightOfBoards += ((shedWidth / 10)) * (height - ((12.8 * (length - 30)) / 1000));
        totalHeightOfBoards += ((shedWidth / 10)) * (height - ((12.8 * x) / 1000));
        lengthOfBoard = height - ((12.8 * x) / 1000);
        ctx.sessionAttribute("lengthOfBoard", (int) lengthOfBoard);

        //Adding poles to the shed
        if ((length - 142 - shedLength) > 310) {
            y = ((12.8 * 100) / 1000);
            carportSvg.addRectangle(100, y + 26.5, height - y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            y = ((12.8 * 410) / 1000);
            carportSvg.addRectangle(410, y + 26.5, height - y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        } else {
            y = ((12.8 * 100) / 1000);
            carportSvg.addRectangle(100, y + 26.5, height - y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        }
        return totalBoards;
    }

    // Remme er placeret 35 nede og går på tværs og er lige så lange spm den indtastede længde
    public int addBeams(int width, int length) {
        carportSvg.addRectangle(0, 35, 4.5, width, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        carportSvg.addRectangle(0, (length - 35), 4.5, width, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        return 2;
    }

    // Remme set fra siden med et fald på 12,8%
    private void addBeamsFromTheSide(int length) {
        double y2 = (12.8 * (length)) / 1000;
        carportSvg.addLine(0, 16.5, length + 6.5, 16.5 + y2, "stroke:#000000; fill: #ffffff");
        carportSvg.addLine(0, 16.5 + 10, length + 6.5, 16.5 + y2 + 10, "stroke:#000000; fill: #ffffff");
        //lodrette streger der forbinder de to linier
        carportSvg.addLine(0, 16.5, 0, 16.5 + 10, "stroke:#000000; fill: #ffffff");
        carportSvg.addLine(length + 6.5, 16.5 + y2, length + 6.5, 16.5 + y2 + 10, "stroke:#000000; fill: #ffffff");
    }


    // Taget set fra siden med et fald på 12,8%
    private void addRoof(int width, int length) {
        double y2 = (12.8 * (length)) / 1000;
        carportSvg.addLine(0, 0, length + 6.5, y2, "stroke:#000000; fill: #ffffff");
        carportSvg.addLine(0, 10, length + 6.5, y2 + 10, "stroke:#000000; fill: #ffffff");
        //lodrette streger der forbinder de to linier
        carportSvg.addLine(0, 0, 0, 10, "stroke:#000000; fill: #ffffff");
        carportSvg.addLine(length + 6.5, y2, length + 6.5, y2 + 10, "stroke:#000000; fill: #ffffff");
    }

    // Spær er placeret for hver 55.174 cm og går lodret
    private int addRafters(int width, int length) {
        int raftCounter = 0;
        double x = 0;
        int loop = 0;
        for (double i = 0; i < length; i += 55.714) {
            loop += 1;
            x = i + (loop * 6.5);
            if ((i + 75 + 55.714) < (length + 75)) {
                carportOuterSvg.addArrow(i + 75, 50, (i + 75 + 55.714), 50, "stroke:#000000; marker-end: url(#endArrow);");
                addTextV(i + 20 + 75, 30, 0, "55");
            }
            carportOuterSvg.addLine(i + 75, 40, i + 75, 60, "stroke:#000000; fill: #ffffff");
            carportSvg.addRectangle(i, 0.0, width, 6.5, "stroke:#000000; fill: #ffffff");
            raftCounter++;
            if (i > (length - 55.714) && i < (length - 6.5)) {
                raftCounter++;
                carportSvg.addRectangle(length, 0.0, width, 6.5, "stroke:#000000; fill: #ffffff");
            }
        }
        return raftCounter;
    }

    // Spær set fra siden igen har taget et fald på 12,8%
    private void addRaftersFromSide(int width, int length) {
        for (double i = 0; i < length; i += 55.714) {
            double y2 = (12.8 * (i)) / 1000;
            carportSvg.addRectangle(i, y2 + 10, 6.5, 6.5, "stroke:#000000; fill: #ffffff");
            if (i > (length - 55.714) && i < (length - 6.5)) {
                carportSvg.addRectangle(length, y2 + 10, 6.5, 6.5, "stroke:#000000; fill: #ffffff");
            }
        }
    }

    //Kryds er de hulbånd som skaber et kryds. De stabiliserer Carporten
    private int addLineWithStroke(Context ctx, int width, int length) {
        if (shedLength == 0) {
            carportSvg.addLineWithStroke(54, 35, length - 58, width - 30, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(58, 35, length - 54, width - 30, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(54, width - 30.5, length - 58, 35, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(58, width - 30.5, length - 54, 35, "stroke:#000000; fill: #ffffff");
            crossSupportLength = ((length - 58) - 54) * 2;
        } else {
            double x = (length - 42 - shedLength);
            carportSvg.addLineWithStroke(54, 35, x, width - 30, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(58, 35, x + 4, width - 30, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(54, width - 30.5, x, 35, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(58, width - 30.5, x + 4, 35, "stroke:#000000; fill: #ffffff");
            crossSupportLength = ((length - 42 - shedLength) - 54) * 2;
            //skurets stiplede linier
            //først de venstreplacerede lodrette
            double y1 = width - 38 - shedWidth + 9;
            double y2 = width - 26;
            x = (length - 30 - shedLength);
            carportSvg.addLineWithStroke(x, y1, x, y2, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(x + 1, y1, x + 1, y2, "stroke:#000000; fill: #ffffff");
            // Herefter de højreplacerede lodrette
            x = (length - 31);
            carportSvg.addLineWithStroke(x, y1, x, y2, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(x + 1, y1, x + 1, y2, "stroke:#000000; fill: #ffffff");
            //dernæst de øverste vandrette
            y1 = width - 38 - shedWidth + 9;
            double x1 = (length - 30 - shedLength);
            double x2 = (length - 32);
            carportSvg.addLineWithStroke(x1, y1, x2, y1, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(x1, y1 + 1, x2, y1 + 1, "stroke:#000000; fill: #ffffff");
            // og til sidst de nederste vandrette
            y2 = width - 27;
            carportSvg.addLineWithStroke(x1, y2, x2, y2, "stroke:#000000; fill: #ffffff");
            carportSvg.addLineWithStroke(x1, y2 + 1, x2, y2 + 1, "stroke:#000000; fill: #ffffff");
        }
        ctx.sessionAttribute("crossSupportLength", (int) crossSupportLength);
        return 2;
    }

    //Stolperne er placeret for hver 310 cm og går 90 cm. ned i jorden
    //Nedenstående metode bruges til tegning af stolper i tegningen set fra oven
    public int addPoles(Context ctx, int width, int length, int height, double shedLength, double shedWidth) {
        int poleCounter = 0;
        int polesWithRemConnection = 0;
        double widthShedPole = 0;
        double lengthShedPole = 0;
        int quantityOfLengthShedPoles = 0;
        int quantityOfWidthShedPoles = 0;
        double x = 0;
        double y = 0;
        //Først placerer vi de 4 yderstolper på carporten
        carportSvg.addRectangle(100, 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        carportSvg.addRectangle(100, width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        carportSvg.addRectangle(length - 42, 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        carportSvg.addRectangle(length - 42, width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        poleCounter = 4;
        polesWithRemConnection = 4;
        //Så undersøger vi om kunden har har valgt at få et skur med.
        // Hvis nej er shedLength = 0
        if (shedLength == 0) {
            if ((length - 142) > 310 && (length - 142) <= 620) {
                x = ((length - 142) / 2);
                carportSvg.addRectangle(x + 100, 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                carportSvg.addRectangle(x + 100, width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                poleCounter += 2;
                polesWithRemConnection += 2;
            } else if ((length - 142) > 620) {
                x = (length - 142) / 3;
                carportSvg.addRectangle(100 + x, 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                carportSvg.addRectangle(100 + x, width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                carportSvg.addRectangle(100 + (2 * x), 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                carportSvg.addRectangle(100 + (2 * x), width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                poleCounter += 4;
                polesWithRemConnection += 4;
            }
            //Hvis kunden har valgt at få et skur med
        } else {
            // Her placerer vi skurets bærende yderstolper
            y = width - 38 - shedWidth + 9;
            x = (length - 30 - shedLength);
            widthShedPole = (width - 38) - (width - shedWidth + 31);
            lengthShedPole = (length - 42) - (length - 42 - shedLength + 12);
            quantityOfLengthShedPoles = 6;
            quantityOfWidthShedPoles = 6;
            //System.out.println("øverste venstre stolpe til skur har "+x+","+y);
            carportSvg.addRectangle(x, y, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            y = width - 38;
            //System.out.println("nederste venstre stolpe til skur har "+x+","+y);
            carportSvg.addRectangle(x, y, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");

            // Her placerer vi skurets øverste højre stolpe
            y = width - 38 - shedWidth + 9;
            x = (length - 42);
            //System.out.println("øverste højre stolpe til skur har "+x+","+y);
            carportSvg.addRectangle(x, y, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            polesWithRemConnection += 2;
            poleCounter += 2;
            if (width - shedWidth > 60) {
                poleCounter++;
            }

            // Her undersøger vi om skurets bredde er større end 310 cm. I så fald skal vi have placeret 2 bærende midterstolper
            if (shedWidth > 310) {
                y = (shedWidth / 2) + (width - shedWidth - 38);
                x = (length - 30 - shedLength);
                widthShedPole = (shedWidth - 36) / 2; // afstanden imellem midterstolpen og yderstolperne
                lengthShedPole = shedLength - (2 * 12); // afstanden imellem skurets 2 stolper
                quantityOfLengthShedPoles = 6;
                quantityOfWidthShedPoles = 12;
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
                polesWithRemConnection += 2;
            }
        }
        ctx.sessionAttribute("widthShedPole", (int) widthShedPole);
        ctx.sessionAttribute("lengthShedPole", (int) lengthShedPole);
        ctx.sessionAttribute("quantityOfLengthShedPoles", quantityOfLengthShedPoles);
        ctx.sessionAttribute("quantityOfWidthShedPoles", quantityOfWidthShedPoles);
        if (shedWidth + 60 < width) {
            polesWithRemConnection -= 1;
        }
        ctx.sessionAttribute("polesWithRemConnection", (polesWithRemConnection * 2));
        return poleCounter;
    }

    //Stolperne er placeret for hver 310 cm og går 90 cm. ned i jorden
    // Nedenstående metode bruges til tegning af stolper i tegningen set fra siden
    public void addPolesFromTheSide(int width, int length, int height, double shedLength, double shedWidth) {
        double x = 0;
        double y = 0;
        //Først placerer vi de 4 yderstolper på carporten
        y = ((12.8 * 100) / 1000);
        carportSvg.addRectangle(100, y + 26.5, height - y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        y = ((12.8 * (length - 42)) / 1000);
        carportSvg.addRectangle(length - 42, y + 26.5, height - y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        //Så undersøger vi om kunden har har valgt at få et skur med.
        // Hvis nej er shedLength = 0
        if (shedLength == 0) {
            if ((length - 142) > 310 && (length - 142) <= 620) {
                x = ((length - 142) / 2);
                y = ((12.8 * (x + 100)) / 1000);
                carportSvg.addRectangle(100 + x, y + 26.5, height - y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");

            } else if ((length - 142) > 620) {
                x = (length - 142) / 3;
                y = (12.8 * (100 + x)) / 1000;
                carportSvg.addRectangle(100 + x, y + 26.5, height - y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                y = (12.8 * (100 + (2 * x))) / 1000;
                carportSvg.addRectangle(100 + (2 * x), y + 26.5, height - y, 12.0, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            }
            //Hvis kunden har valgt at få et skur med
        } else {
            // Her placerer vi skurets bærende yderstolper
            y = width - shedWidth + 31;
            x = (length - 30 - shedLength);
            //System.out.println("øverste venstre stolpe til skur har "+x+","+y);
            carportSvg.addRectangle(x, y, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            y = width - 38;
            //System.out.println("nederste venstre stolpe til skur har "+x+","+y);
            carportSvg.addRectangle(x, y, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            // Her placerer vi skurets øverste højre stolpe
            y = width - shedWidth + 31;
            x = (length - 42);
            //System.out.println("øverste højre stolpe til skur har "+x+","+y);
            carportSvg.addRectangle(x, y, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");

            // Hvis rummet imellem carportens venstre yderstolper og skurets venstrestolper er større end 322cm(310+stolpens bredde)
            // så tilføjer vi 2 ekstra bærende stolper til carporten
            if ((length - 142 - shedLength) > 310) {
                y = ((12.8 * 100) / 1000);
                x = 100 + 310;
                carportSvg.addRectangle(x, 31, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
                carportSvg.addRectangle(x, width - 38, 12, 12, "stroke-width:1px; stroke:#000000; fill: #ffffff");
            }
        }
    }

    //  Pilene er placeret til venstre for y-aksen og neddenfor X-visen og er med til at vise bredde og højde på carporten.
    //  Den tredje pil viser bredden imellem de 20 remme
    public void addArrowsForTheTopPainting(int width, int length) {
        //Pilen som angiver bredden imellem
        carportOuterSvg.addArrow(20, width + 75 - 30, 20, 107, "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addLine(10, width + 75 - 30, 30, width + 75 - 30, "stroke:#000000; fill: #ffffff");
        carportOuterSvg.addLine(10, 107, 30, 107, "stroke:#000000; fill: #ffffff");
        carportOuterSvg.addArrow(50, width + 75, 50, 75, "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addLine(40, width + 75, 60, width + 75, "stroke:#000000; fill: #ffffff");
        carportOuterSvg.addLine(40, 75, 60, 75, "stroke:#000000; fill: #ffffff");
        carportOuterSvg.addArrow(75, width + 30 + 75, length + 79, width + 30 + 75, "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addLine(75, width + 30 + 65, 75, width + 30 + 85, "stroke:#000000; fill: #ffffff");
        carportOuterSvg.addLine((length + 79), width + 30 + 65, (length + 79), width + 30 + 85, "stroke:#000000; fill: #ffffff");
    }

    public void addArrowsForTheSidePainting(int width, int length, int height, double shedWidth, double shedLength) {
        carportOuterSvg.addArrow(20, height + 30 + 75, 20, 75, "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addLine(10, height + 30 + 75, 30, height + 30 + 75, "stroke:#000000; fill: #ffffff");
        carportOuterSvg.addLine(10, 75, 30, 75, "stroke:#000000; fill: #ffffff");
        carportOuterSvg.addArrow(50, height + 30 + 75, 50, 91, "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addLine(40, height + 30 + 75, 60, height + 30 + 75, "stroke:#000000; fill: #ffffff");
        carportOuterSvg.addLine(40, 91, 60, 91, "stroke:#000000; fill: #ffffff");
        carportOuterSvg.addArrow(length + 105, height + 26.5 + 75, length + 105, (75 + ((int) (12.8 * (length)) / 1000)), "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addLine(length + 95, (75 + ((int) (12.8 * (length)) / 1000)), length + 115, (75 + ((int) (12.8 * (length)) / 1000)), "stroke:#000000; fill: #ffffff");
        carportOuterSvg.addLine(length + 95, height + 26.5 + 75, length + 115, height + 26.5 + 75, "stroke:#000000; fill: #ffffff");
        carportOuterSvg.addArrow(75, height + 50 + 75, 175, height + 50 + 75, "stroke:#000000; marker-end: url(#endArrow);");
        carportOuterSvg.addLine(75, height + 50 + 75 - 10, 75, height + 50 + 75 + 10, "stroke:#000000; fill: #ffffff");
        carportOuterSvg.addLine(175, height + 50 + 75 - 10, 175, height + 50 + 75 + 10, "stroke:#000000; fill: #ffffff");
        addTextV((100 / 2) + 75 - 10, height + 55 + 90, 0, "100");

        // Pilen der går fra skurets ende til carportens ende har koordinaterne
        // Her lægger vi 6.5 cm til da jeg har forlænget taget med 6.5 cm.
        carportOuterSvg.addArrow((length + 75) - 30, height + 50 + 75, (int) (length + 75 + 6.5), height + 50 + 75, "stroke:#000000; marker-end: url(#endArrow);");
        addTextV((length + 75 - 20), height + 55 + 90, 0, "30");
        carportOuterSvg.addLine((length + 75) - 30, height + 50 + 75 - 10, (length + 75) - 30, height + 50 + 75 + 10, "stroke:#000000; fill: #ffffff");
        carportOuterSvg.addLine((length + 75 + 6.5), height + 50 + 75 - 10, (length + 75 + 6.5), height + 50 + 75 + 10, "stroke:#000000; fill: #ffffff");
        if (shedWidth > 0) {
            double x1 = (length + 75 - 30 - shedLength);
            double x2 = x1 + shedLength;

            // Pilen som skal befinde sig under skuret har startkoordinatet
            carportOuterSvg.addArrow(x1, height + 50 + 75, x2, height + 50 + 75, "stroke:#000000; marker-end: url(#endArrow);");
            carportOuterSvg.addLine(x1, height + 50 + 75 - 10, x1, height + 50 + 75 + 10, "stroke:#000000; fill: #ffffff");
            addTextV((length + 75 - 40 - (shedLength / 2)), height + 55 + 90, 0, "" + shedLength);
            if (((length - 142) - shedLength) > 310) {
                int x = 75 + 100 + 310;
                double x3 = length + 75 - 30 - shedLength;
                double x4 = ((x3 - x) / 2) + x;

                // Pilen der går fra anden stolpe til starten af skuret
                carportOuterSvg.addArrow(x, height + 50 + 75, x3, height + 50 + 75, "stroke:#000000; marker-end: url(#endArrow);");
                carportOuterSvg.addLine(x, height + 50 + 75 - 10, x, height + 50 + 75 + 10, "stroke:#000000; fill: #ffffff");
                addTextV(x4 - 10, height + 55 + 90, 0, "" + ((x3 - x)));

                // Pilen er går fra første stolpe til anden stolpe
                x2 = 75 + 100;
                x3 = x2 + 310;

                // Pilen der går fra første stolpe til anden stolpe
                carportOuterSvg.addArrow(x2, height + 50 + 75, x3, height + 50 + 75, "stroke:#000000; marker-end: url(#endArrow);");
                carportOuterSvg.addLine(x2, height + 50 + 75 - 10, x2, height + 50 + 75 + 10, "stroke:#000000; fill: #ffffff");
                addTextV((310 / 2) + 175 - 20, height + 55 + 90, 0, "310");
            } else {

                x1 = 75 + 100;
                x2 = length + 75 - 30 - shedLength;
                // Pilen der går fra første stolpe til start af skuret
                carportOuterSvg.addArrow(x1, height + 50 + 75, x2, height + 50 + 75, "stroke:#000000; marker-end: url(#endArrow);");
                addTextV((((length - 130 - shedLength) / 2) + 165), height + 55 + 90, 0, "" + ((length - shedLength - 130)));
            }
        } else {
            if ((length - 142) < 620 && (length - 142) > 310) {
                x1 = (length - 142) / 2;
                carportOuterSvg.addArrow(175, height + 50 + 75, x1 + 175, height + 50 + 75, "stroke:#000000; marker-end: url(#endArrow);");
                addTextV((((length - 142) / 2) / 2) + 175 - 20, height + 55 + 90, 0, "" + x1);
                carportOuterSvg.addArrow(x1 + 175, height + 50 + 75, x1 + x1 + 175, height + 50 + 75, "stroke:#000000; marker-end: url(#endArrow);");
                addTextV((((length - 142) / 2) / 2) + x1 + 175 - 20, height + 55 + 90, 0, "" + x1);
            } else if ((length - 142) > 620) {
                int x2 = (length - 142) / 3;
                carportOuterSvg.addArrow(175, height + 50 + 75, x2 + 175, height + 50 + 75, "stroke:#000000; marker-end: url(#endArrow);");
                addTextV((((length - 142) / 3) / 2) + 175 - 20, height + 55 + 90, 0, "" + x2);
                carportOuterSvg.addArrow(x2 + 175, height + 50 + 75, x2 + x2 + 175, height + 50 + 75, "stroke:#000000; marker-end: url(#endArrow);");
                addTextV((((length - 142) / 3) / 2) + x2 + 175 - 20, height + 55 + 90, 0, "" + x2);
                carportOuterSvg.addArrow(x2 + x2 + 175, height + 50 + 75, x2 + x2 + x2 + 175, height + 50 + 75, "stroke:#000000; marker-end: url(#endArrow);");
                addTextV((((length - 142) / 3) / 2) + x2 + x2 + 175 - 20, height + 55 + 90, 0, "" + x2);
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

