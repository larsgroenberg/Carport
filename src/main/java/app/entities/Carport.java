package app.entities;

import java.util.ArrayList;

public class Carport {
    private ArrayList<CarportPart> CarportPartList;
    private double length;
    private double width;
    private double height;
    private boolean withRoof;
    private boolean withShed;
    private double shedLength;
    private double shedWidth;
    private double price;

    public Carport(double length, double width, double height, boolean withRoof, boolean withShed, double shedLength, double shedWidth, double price) {
        this.CarportPartList = new ArrayList<>();
        this.length = length;
        this.width = width;
        this.height = height;
        this.withRoof = withRoof;
        this.withShed = withShed;
        this.shedLength = shedLength;
        this.shedWidth = shedWidth;
        this.price = price;
    }

    public Carport(ArrayList<CarportPart> carportPartList, int length, int width, int height) {
        CarportPartList = carportPartList;
        this.length = length;
        this.width = width;
        this.height = height;
    }


    @Override
    public String toString() {
        return "Carport{" +
                "CarportPartList=" + CarportPartList +
                ", length=" + length +
                ", width=" + width +
                ", height=" + height +
                ", withRoof=" + withRoof +
                ", withShed=" + withShed +
                ", shedLength=" + shedLength +
                ", shedWidth=" + shedWidth +
                ", price=" + price +
                '}';
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isWithRoof() {
        return withRoof;
    }


    public boolean isWithShed() {
        return withShed;
    }


    public double getShedLength() {
        return shedLength;
    }

    public void setShedLength(int shedLength) {
        this.shedLength = shedLength;
    }

    public double getShedWidth() {
        return shedWidth;
    }

    public void setShedWidth(int shedWidth) {
        this.shedWidth = shedWidth;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
