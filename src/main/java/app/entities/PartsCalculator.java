package app.entities;

public class PartsCalculator {

    public static int calculateNumberOfPosts(double length, double width) {
        //Posts = stolper

        int numberOfPosts = 4;
        double maxSpaceBetweenPosts = 310; //each posts can carry 3.1 meter of roof

        //adds additional posts, since corners are already added
        for (double i = maxSpaceBetweenPosts; length >= i; i = i + maxSpaceBetweenPosts){
            numberOfPosts = numberOfPosts+2;
        }
        return numberOfPosts;
    }

    public static int calculateNumberOfRafts(double length) {
        //Rafts = spær
        //We assume that our rafts are 4.5 cm in width and that there needs to be 55 cm between each

        //numbers are in CM
        double raftWidth = 4.5;
        double spaceBetweenEachRaft = 55;

        int numberOfRafters = 1;

        for(double i = 0; i < length; i = i + raftWidth + spaceBetweenEachRaft){
            numberOfRafters++;
        }

        return numberOfRafters;
    }

    public static int calculateNumberOfRoofPlates(double length, double width) {
        //Roofplates = tagplast
        //we assume that a "standard" plate is 109 cm x 360 cm

        double roofPlatesWidth = 109;
        double roofPlatesLength = 360;

        int numberOfRoofPlates;
        int numberOfPlatesForLength = 0;
        int numberOfPlatesForWidth = 0;

        for (double i = 0; i < length; i=i+roofPlatesLength){
            numberOfPlatesForLength++;
        }

        for (double i = 0; i < width; i=i+roofPlatesWidth){
            numberOfPlatesForWidth++;
        }

        //we multiply the number needed for the width with the number needed in the length to fill the whole area
        numberOfRoofPlates = numberOfPlatesForLength*numberOfPlatesForWidth;

        //use description: tagplader	monteres	på	spær
        return numberOfRoofPlates;
    }


}
