package app.entities;

import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PartsCalculator {
    private final Carport carport;
    private final List<CarportPart> dbPartsList;
    private final List<CarportPart> partsList = new ArrayList<>();
    private List<Integer> availableLengths;
    private List<CarportPart> partsListOfRequiredType;
    private int longestBoard = 0;
    private int closestLength = 0;
    private final String descriptionText = "";

    public PartsCalculator(Carport carport, ArrayList<CarportPart> dbPartsList) {
        this.carport = carport;
        this.dbPartsList = dbPartsList;
    }

    public void calculateCarport(Context ctx) {

        calculateCombinationOfBoards("understern", 2, (int) ((carport.getWidth() + 10) + (carport.getLength() + 10)), descriptionText);
        calculateCombinationOfBoards("overstern", 2, (int) ((carport.getWidth() + 10) + (carport.getLength() + 10)), descriptionText);
        calculateCombinationOfBoards("stolpe", ctx.sessionAttribute("totalPoles"), ctx.sessionAttribute("postlength"), descriptionText);
        calculateCombinationOfBoards("rem", 2, ctx.sessionAttribute("carport_length"), descriptionText);
        calculateCombinationOfBoards("spær", ctx.sessionAttribute("totalRafters"), ctx.sessionAttribute("carport_width"), descriptionText);
        if (carport.isWithShed()) {
            calculateCombinationOfBoards("skurbrædt", ctx.sessionAttribute("totalBoards"), ctx.sessionAttribute("lengthOfBoard"), descriptionText);
            calculateCombinationOfBoards("reglar", ctx.sessionAttribute("quantityOfLengthShedPoles"), ctx.sessionAttribute("lengthShedPole"), "løsholter til skur gavle");
            calculateCombinationOfBoards("reglar", ctx.sessionAttribute("quantityOfWidthShedPoles"), ctx.sessionAttribute("widthShedPole"), "løsholter til skur sider");
        }
        if (carport.isWithRoof()) {
            calculateCombinationOfBoards("tagplader", (int) (Math.ceil(carport.getWidth() / 109)), (int) (carport.getLength() + 30), descriptionText);
            calculateScrews("bundskruer", 600, 50, "", "skruer til tagplader");
        }
        calculateScrews("hulbånd", ctx.sessionAttribute("crossSupportLength"), 1000, "1x20 mm hulbånd HB 20-1", "til vindkryds på spær");
        calculateCombinationOfBoards("vandbrædder", 2, (int) ((carport.getWidth() + 10) + (carport.getLength() + 10)), descriptionText);
        calculateBracketsAndBolts("bolte", ctx.sessionAttribute("polesWithRemConnection"), 25, "10x120 mm bræddebolt, varmeforzinket, 25 styks", "til montering af rem på stolper");
        calculateBracketsAndBolts("firkantskiver", ctx.sessionAttribute("polesWithRemConnection"), 50, "40x40x11 mm firkantskiver, varmeforzinket, 50 styks", "til montering af rem på stolper");
        calculateBracketsAndBolts("universalbeslag", ctx.sessionAttribute("totalRafters"), 1, "190 mm universalbeslag, venstre, varmeforzinket", "til montering af spær på rem");
        calculateBracketsAndBolts("universalbeslag", ctx.sessionAttribute("totalRafters"), 1, "190 mm universalbeslag, højre, varmeforzinket", "til montering af spær på rem");
        calculateScrews("skruer", 200, 200, "4,5x60 mm universalskruer, 200 styks", "til montering af stern & vandbrædt");
        calculateScrews("skruer", 800, 200, "4,5x50 mm beslagskruer, 200 styks.", "til montering af universalbeslag og hulbånd");
        if (carport.isWithShed()) {
            calculateBracketsAndBolts("vinkelbeslag", 2 * ((int) (ctx.sessionAttribute("quantityOfWidthShedPoles")) + (int) (ctx.sessionAttribute("quantityOfLengthShedPoles"))), 1, "35x35 mm vinkelbeslag med rib, varmeforzinket", "til montering af løsholter i skur");
            calculateScrews("skruer", 800, 200, "", "til montering af den yderste beklædning");
            calculateScrews("skruer", 600, 200, "4,5x50 mm universalskruer, 200 styks", "til montering af den inderste beklædning");
            calculateBracketsAndBolts("hængsel", 1, 1, "50x75 mm stalddørsgreb til hængelås, galvaniseret.", "til lås på dør i skur");
            calculateBracketsAndBolts("hængsel", 2, 1, "390 mm t-hængsel til stalddør, varmeforzinket", "til skurdør");
            calculateBracketsAndBolts("lægte", 1, 1, "38x73mm lægte trykimprægneret,70% PEFC", "til z på bagside af dør");
        }
        ctx.sessionAttribute("partsList", partsList);
    }

    private void calculateBracketsAndBolts(String type, int quantity, int quantityPerPacket, String descriptionText, String nameText) {
        int quantityOfPackets = (int) Math.ceil((double) quantity / quantityPerPacket);
        createDBLists(type);
        for (CarportPart part : partsListOfRequiredType) {
            if (String.valueOf(part.getType()).equalsIgnoreCase(type) && part.getDBdescription().equalsIgnoreCase(descriptionText)) {
                if (descriptionText.length() > 2) part.setDBname(nameText);
                part.setQuantity(quantityOfPackets);
                partsList.add(new CarportPart(part.getPartId(), part.getType(), part.getQuantity(), part.getDBprice(), part.getDBlength(), part.getDBheight(), part.getDBwidth(), part.getDBdescription(), part.getDBmaterial(), part.getDBunit(), part.getDBname(), part.getDBtype()));
            }
        }
    }

    private void calculateScrews(String type, int quantity, int quantityPerPacket, String descriptionText, String nameText) {
        int quantityOfPackets = (int) Math.ceil((double) quantity / quantityPerPacket);
        createDBLists(type);
        for (CarportPart part : partsListOfRequiredType) {
            if (String.valueOf(part.getType()).equalsIgnoreCase(type) && part.getDBname().equalsIgnoreCase(nameText)) {
                if (descriptionText.length() > 2) {
                    part.setDBdescription(descriptionText);
                    part.setDBname(nameText);
                }
                part.setQuantity(quantityOfPackets);
                partsList.add(new CarportPart(part.getPartId(), part.getType(), part.getQuantity(), part.getDBprice(), part.getDBlength(), part.getDBheight(), part.getDBwidth(), part.getDBdescription(), part.getDBmaterial(), part.getDBunit(), part.getDBname(), part.getDBtype()));
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

        // Hvis den ønskede længde overstiger længden af det længste brædt vi kan levere
        if (longestBoard < requiredLength) {
            numberOfLongestBoards++;
            restLength = requiredLength - longestBoard;
            while (restLength > longestBoard) {
                numberOfLongestBoards++;
                restLength -= longestBoard;
            }
            if (restLength > 0) {
                for (Integer length : availableLengths) {
                    if (length >= restLength) {
                        closestLength = length;
                        break;
                    }
                }
            }
        } else {
            for (Integer length : availableLengths) {
                if (length >= requiredLength) {
                    closestLength = length;
                    singleBoard = true;
                    break;
                }
            }
        }

        for (CarportPart part : partsListOfRequiredType) {
            if (quantity > 0) {
                if (part.getDBlength() == longestBoard && !singleBoard) {
                    if (descriptionText.length() > 2) part.setDBname(descriptionText);
                    part.setQuantity((numberOfLongestBoards * quantity));
                    // Herved sikrer jeg at ens part.objekter bliver behandlet korrekt
                    partsList.add(new CarportPart(part.getPartId(), part.getType(), part.getQuantity(), part.getDBprice(), part.getDBlength(), part.getDBheight(), part.getDBwidth(), part.getDBdescription(), part.getDBmaterial(), part.getDBunit(), part.getDBname(), part.getDBtype()));
                }
                if (part.getDBlength() == closestLength && !singleBoard) {
                    if (descriptionText.length() > 2) part.setDBname(descriptionText);
                    part.setQuantity(quantity);
                    // Herved sikrer jeg at ens part.objekter bliver behandlet korrekt
                    partsList.add(new CarportPart(part.getPartId(), part.getType(), part.getQuantity(), part.getDBprice(), part.getDBlength(), part.getDBheight(), part.getDBwidth(), part.getDBdescription(), part.getDBmaterial(), part.getDBunit(), part.getDBname(), part.getDBtype()));
                }
                if (part.getDBlength() == closestLength && singleBoard) {
                    if (descriptionText.length() > 2) part.setDBname(descriptionText);
                    part.setQuantity(quantity);
                    // Herved sikrer jeg at ens part.objekter bliver behandlet korrekt
                    partsList.add(new CarportPart(part.getPartId(), part.getType(), part.getQuantity(), part.getDBprice(), part.getDBlength(), part.getDBheight(), part.getDBwidth(), part.getDBdescription(), part.getDBmaterial(), part.getDBunit(), part.getDBname(), part.getDBtype()));
                }
            }
        }
    }

    // Her genererer vi 2 lister, en med de materialer som er lig med type og en Integer-liste med tilgængelige størrelser
    private void createDBLists(String type) {
        availableLengths = new ArrayList<>();
        partsListOfRequiredType = new ArrayList<>();
        for (CarportPart part : dbPartsList) {
            if (String.valueOf(part.getType()).equalsIgnoreCase(type)) {
                availableLengths.add(part.getDBlength());
                partsListOfRequiredType.add(part);
            }
        }
    }
}