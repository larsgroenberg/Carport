package app.entities;

public class CarportPart {
    public enum CarportPartType {
        SUPPORTPOST,
        RAFT,
        BEAM,
        CROSSSUPPORT,
        ROOFTILE
    }

    private CarportPartType type;
    private int quantity;
    private double DBprice;
    private int DBlength;
    private int DBheight;
    private int DBwidth;

    public CarportPart(CarportPartType type, int quantity) {
        this.type = type;
        this.quantity = quantity;
    }

    public CarportPart(CarportPartType type, int quantity, double DBprice, int DBlength, int DBheight, int DBwidth) {
        this.type = type;
        this.quantity = quantity;
        this.DBprice = DBprice;
        this.DBlength = DBlength;
        this.DBheight = DBheight;
        this.DBwidth = DBwidth;
    }

    public CarportPartType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getDBprice() {
        return DBprice;
    }

    public int getDBlength() {
        return DBlength;
    }

    public int getDBheight() {
        return DBheight;
    }

    public int getDBwidth() {
        return DBwidth;
    }
}
