package app.entities;

import app.persistence.ConnectionPool;
import io.javalin.http.Context;
import java.util.*;

public class PartsCalculator {
    private Carport carport;
    private List<CarportPart> dbPartsList;
    private List<CarportPart> partsList = new ArrayList<>();
    private List<Integer> availableLengths;
    private List<CarportPart> partsListOfRequiredType;
    private double totalPrice = 0;
    private int longestBoard = 0;
    private ConnectionPool connectionPool;
    private int requiredLength = 0;
    private int quantityOfType = 0;
    private int doubleLength = 0;
    private int closestLength = 0;
    private int quantityOfDoubleLength = 0;
    private int quantityOfClosestLength = 0;
    private double priceOfOptimalLength = 0.0;
    private double priceOfClosestLength = 0.0;
    private String descriptionText = "";

    public PartsCalculator(Carport carport, ArrayList<CarportPart> dbPartsList, Context ctx, ConnectionPool connectionPool) {
        this.carport = carport;
        this.dbPartsList = dbPartsList;
        this.connectionPool = connectionPool;
    }

    public void calculateCarport (Context ctx) {

        calculateCombinationOfBoards("understern", 2, (int)((carport.getWidth()+10)+(carport.getLength()+10)), descriptionText);
        calculateCombinationOfBoards("overstern", 2,  (int)((carport.getWidth()+10)+(carport.getLength()+10)), descriptionText);
        calculateCombinationOfBoards("stolpe", ctx.sessionAttribute("totalPoles"), (int)(ctx.sessionAttribute("postlength")), descriptionText);
        calculateCombinationOfBoards("rem", 2, ctx.sessionAttribute("carport_length"), descriptionText);
        calculateCombinationOfBoards("spær", ctx.sessionAttribute("totalRafters"), ctx.sessionAttribute("carport_width"), descriptionText);
        calculateCombinationOfBoards("skurbrædt", ctx.sessionAttribute("totalBoards"), (int)(ctx.sessionAttribute("lengthOfBoard")), descriptionText);
        calculateCombinationOfBoards("reglar", ctx.sessionAttribute("quantityOfWidthShedPoles"), (int)(ctx.sessionAttribute("widthShedPole")), "løsholter til skur sider");
        if(carport.isWithRoof()) {
           calculateCombinationOfBoards("tagplader", (int)(Math.ceil(carport.getWidth()/109)), (int)(carport.getLength()+30), descriptionText);
        }
        calculateCrosssupport(ctx);
        calculateCombinationOfBoards("reglar", ctx.sessionAttribute("quantityOfLengthShedPoles"), (int)(ctx.sessionAttribute("lengthShedPole")), "løsholter til skur gavle");
        calculateCombinationOfBoards("reglar", ctx.sessionAttribute("quantityOfWidthShedPoles"), (int)(ctx.sessionAttribute("widthShedPole")), "løsholter til skur sider");
        calculateCombinationOfBoards("vandbrædder", 2, (int)((carport.getWidth()+10)+(carport.getLength()+10)), descriptionText);
        calculateBracketsAndBolts("bolte",  ctx.sessionAttribute("polesWithRemConnection"), 25, "10x120 mm bræddebolt, varmeforzinket, 25 styks","til montering af rem på stolper");
        calculateBracketsAndBolts("firkantskiver",  ctx.sessionAttribute("polesWithRemConnection"), 1, "40x40x11 mm firkantskiver, varmeforzinket", "til montering af rem på stolper");
        calculateBracketsAndBolts("universalbeslag", ctx.sessionAttribute("totalRafters"), 1, "190 mm universalbeslag, venstre, varmeforzinket", "til montering af spær på rem");
        calculateBracketsAndBolts("universalbeslag", ctx.sessionAttribute("totalRafters"), 1, "190 mm universalbeslag, højre, varmeforzinket", "til montering af spær på rem");
        calculateBracketsAndBolts("vinkelbeslag", 2*((int)(ctx.sessionAttribute("quantityOfWidthShedPoles"))+(int)(ctx.sessionAttribute("quantityOfLengthShedPoles"))),1, "35x35 mm vinkelbeslag med rib, varmeforzinket", "til montering af løsholter i skur");
        calculateScrews("skruer", 200,200, "","til montering af stern & vandbrædt" );
        calculateScrews("skruer", 800,200, "4,5x50 mm beslagskruer, 200 styks.","til montering af universalbeslag + hulbånd");
        if(carport.isWithShed()) {
            calculateScrews("skruer", 800, 200, "", "til montering af den yderste beklædning");
            calculateScrews("skruer", 600, 200, "4,5x50 mm universalskruer, 200 styks", "til montering af den inderste beklædning");
            calculateBracketsAndBolts("hængsel", 1, 1, "50x75 mm stalddørsgreb til hængelås, galvaniseret.","til lås på dør i skur");
            calculateBracketsAndBolts("hængsel", 2, 1,"390 mm t-hængsel til stalddør, varmeforzinket", "til skurdør");
            calculateBracketsAndBolts("lægte", 1, 1, "38x73mm lægte trykimprægneret,70% PEFC", "til z på bagside af dør");
        }
        if(carport.isWithRoof()) {
            calculateScrews("bundskruer", 600, 50, "", "skruer til tagplader");
        }
        ctx.sessionAttribute("partsList", partsList);
    }

    private void calculateBracketsAndBolts(String type, int quantity, int quantityPerPacket,  String descriptionText, String nameText) {
        int quantityOfPackets = (int) Math.ceil((double) quantity/quantityPerPacket);
        createDBLists(type);
        for (CarportPart part : partsListOfRequiredType) {
            System.out.println("partType : "+part.getType());
            if (String.valueOf(part.getType()).equalsIgnoreCase(type) && part.getDBdescription().equalsIgnoreCase(descriptionText)) {
                if (descriptionText.length() > 2) part.setDBname(nameText);
                part.setQuantity(quantityOfPackets);
                partsList.add(new CarportPart(part.getTypen(), part.getQuantity(), part.getPartId(), part.getDBprice(), part.getDBlength(), part.getDBheight(), part.getDBwidth(), part.getDBdescription(), part.getDBmaterial(), part.getDBunit(), part.getDBname()));
            }
        }
    }

    private void calculateScrews(String type, int quantity, int quantityPerPacket, String descriptionText, String nameText) {
        int quantityOfPackets = (int) Math.ceil((double) quantity/quantityPerPacket);

        createDBLists(type);
        for (CarportPart part : partsListOfRequiredType) {
            System.out.println("partType : "+part.getType());
            if (String.valueOf(part.getType()).equalsIgnoreCase(type) && part.getDBname().equalsIgnoreCase(nameText)) {
                if (descriptionText.length() > 2) {
                    part.setDBdescription(descriptionText);
                    part.setDBname(nameText);
                }
                part.setQuantity(quantityOfPackets);
                partsList.add(new CarportPart(part.getTypen(), part.getQuantity(), part.getPartId(), part.getDBprice(), part.getDBlength(), part.getDBheight(), part.getDBwidth(), part.getDBdescription(), part.getDBmaterial(), part.getDBunit(), part.getDBname()));
            }
        }
    }

    // Denne metode tager typen af materiale, det ønskede antal, den ønskede længde og en beskrivelse.
    private void calculateCombinationOfBoards(String type, int quantity, int requiredLength, String descriptionText) {

        int restLength = 0;
        int numberOfLongestBoards = 0;
        longestBoard = 0;
        closestLength = 0;
        boolean singleBoard = false;

        // Her laver vi en liste af Integer med de tilgængelige længder og en liste med den type parts vi søger efter
        createDBLists(type);

        // Her sorterer vi de tilgængelige længder i faldende rækkefølge så vi starter med det største
        // Det gør vi for at trække den længst mulige variant ud.
        Collections.sort(availableLengths, Collections.reverseOrder());
        longestBoard = availableLengths.get(0);

        // Her sorterer vi listen så den er stigende
        Collections.sort(availableLengths);
        System.out.println("\nType : "+type);
        System.out.println("The longestBoard in the availablelist is: "+longestBoard+" cm., requiredLength : "+requiredLength+", quantity : "+quantity);

        // Hvis den ønskede længde overstiger længden af det længste brædt vi kan levere
        if(longestBoard < requiredLength) {
            numberOfLongestBoards++;
            restLength = requiredLength-longestBoard;
            while(restLength>longestBoard){
                numberOfLongestBoards++;
                restLength -= longestBoard;
            }
            if(restLength > 0) {
                for (Integer length: availableLengths) {
                    if (length >= restLength) {
                        closestLength = length;
                        break;
                    }
                }
            }
        } else {
            for (Integer length: availableLengths) {
                if (length >= requiredLength) {
                    closestLength = length;
                    singleBoard = true;
                    break;
                }
            }
        }

        for (CarportPart part : partsListOfRequiredType) {
            if(quantity > 0) {
                if (part.getDBlength() == longestBoard && !singleBoard) {
                    System.out.println("Doublelength solution:\nType : " + part.getType() + ", Length : " + part.getDBlength() + ", Antal : " + numberOfLongestBoards + ", Pris pr. styk. : " + part.getDBprice());
                    if (descriptionText.length() > 2) part.setDBname(descriptionText);
                    part.setQuantity((numberOfLongestBoards * quantity));
                    // Herved sikrer jeg at ens part.objekter bliver behandlet korrekt
                    partsList.add(new CarportPart(part.getTypen(), part.getQuantity(), part.getPartId(), part.getDBprice(), part.getDBlength(), part.getDBheight(), part.getDBwidth(), part.getDBdescription(), part.getDBmaterial(), part.getDBunit(), part.getDBname()));
                }
                if (part.getDBlength() == closestLength && !singleBoard) {
                    System.out.println("ClosestLength and Doublelength solutions:\nType: " + part.getType() + ", Length: " + part.getDBlength() + ", Antal : 1, pris : " + part.getDBprice());
                    if (descriptionText.length() > 2) part.setDBname(descriptionText);
                    part.setQuantity(quantity);
                    // Herved sikrer jeg at ens part.objekter bliver behandlet korrekt
                    partsList.add(new CarportPart(part.getTypen(), part.getQuantity(), part.getPartId(), part.getDBprice(), part.getDBlength(), part.getDBheight(), part.getDBwidth(), part.getDBdescription(), part.getDBmaterial(), part.getDBunit(), part.getDBname()));
                }
                if (part.getDBlength() == closestLength && singleBoard) {
                    System.out.println("ClosestLength solution:\nType: " + part.getType() + ", Length: " + part.getDBlength() + ", Antal : " + quantity + ", Pris : " + part.getDBprice());
                    if (descriptionText.length() > 2) part.setDBname(descriptionText);
                    part.setQuantity(quantity);
                    System.out.println("part.getQuantity() : " + part.getQuantity());
                    // Herved sikrer jeg at ens part.objekter bliver behandlet korrekt
                    partsList.add(new CarportPart(part.getTypen(), part.getQuantity(), part.getPartId(), part.getDBprice(), part.getDBlength(), part.getDBheight(), part.getDBwidth(), part.getDBdescription(), part.getDBmaterial(), part.getDBunit(), part.getDBname()));
                }
            }
        }
    }

    private void createDBLists(String type) {
        availableLengths = new ArrayList<>();
        partsListOfRequiredType = new ArrayList<>();
        for (CarportPart part : dbPartsList) {
            System.out.println("partType : "+part.getType()+" length : "+part.getDBlength()+"description : "+part.getDBname());
            if (String.valueOf(part.getType()).equalsIgnoreCase(type)) {
                //System.out.println("part : "+part.getType()+" length : "+part.getDBlength()+"description : "+part.getDBname());
                availableLengths.add(part.getDBlength());
                partsListOfRequiredType.add(part);
            }
        }
    }

    // Hulbånd
    private void calculateCrosssupport(Context ctx) {
        quantityOfType = ctx.sessionAttribute("totalCrossSupports");
        requiredLength = ctx.sessionAttribute("crossSupportLength");
        insertCarportPartIntoPartslist("hulbånd");
    }

    private void insertCarportPartIntoPartslist(String type) {
        ArrayList<Integer> availableLengths = new ArrayList<>();
        ArrayList<CarportPart> partsListOfRequiredType = new ArrayList<>();
        for (CarportPart part : dbPartsList) {
            if (String.valueOf(part.getType()).equalsIgnoreCase(type)) {
                availableLengths.add(part.getDBlength());
                partsListOfRequiredType.add(part);
            }
        }

        findOptimalLength(type, availableLengths);

        for (CarportPart part : partsListOfRequiredType) {
            if (part.getDBlength() == doubleLength) {
                priceOfOptimalLength = part.getDBprice();
                if (quantityOfDoubleLength > 0) {
                    partsList.add(new CarportPart(part.getType(), quantityOfDoubleLength, part.getPartId(), part.getDBprice(), part.getDBlength(), part.getDBheight(), part.getDBwidth(), part.getDBdescription(), part.getDBmaterial(), part.getDBunit(), part.getDBname()));
                }
            }
            if (part.getDBlength() == closestLength) {
                priceOfClosestLength = part.getDBprice();
                if (quantityOfClosestLength > 0) {
                    partsList.add(new CarportPart(part.getType(), quantityOfClosestLength, part.getPartId(), part.getDBprice(), part.getDBlength(), part.getDBheight(), part.getDBwidth(), part.getDBdescription(), part.getDBmaterial(), part.getDBunit(), part.getDBname()));
                }
            }
        }
    }

    public void findOptimalLength(String type, ArrayList<Integer> availableLengths) {
        // Her sorterer jeg listen med de tilgængelige længder
        Collections.sort(availableLengths);
        // Her finder jeg den mindste længde, der er mindst lig med påkrævetLængde * 2
        doubleLength = 0;
        closestLength = 0;
        quantityOfDoubleLength = 0;
        quantityOfClosestLength = 0;
        for (Integer length : availableLengths) {
            if (length >= (requiredLength * 2)) {
                doubleLength = length;
                break;
            }
        }

        for (Integer length: availableLengths) {
            if (length >= requiredLength) {
                closestLength = length;
                break;
            }
        }

        if (doubleLength > 0) {
            quantityOfDoubleLength = (int)(quantityOfType/2);
            quantityOfClosestLength = quantityOfType-(quantityOfDoubleLength*2);
        } else {
            quantityOfClosestLength = quantityOfType;
        }
    }
}