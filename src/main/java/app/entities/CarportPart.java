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
    private int partId;
    private int quantity;
    private double DBprice;
    private int DBlength;
    private int DBheight;
    private int DBwidth;
    private String DBdescription;
    private String DBmaterial;
    private String DBunit;
    private String DBname;
    private double DBtotalQuantityPrice;

    public CarportPart(CarportPartType type, int quantity) {
        this.type = type;
        this.quantity = quantity;
    }

    public CarportPart(CarportPartType type, int quantity,int partId,double DBprice, int DBlength, int DBheight, int DBwidth, String DBdescription, String DBmaterial, String DBunit, String DBname) {
        this.type = type;
        this.quantity = quantity;
        this.partId = partId;
        this.DBprice = DBprice;
        this.DBlength = DBlength;
        this.DBheight = DBheight;
        this.DBwidth = DBwidth;
        this.DBdescription = DBdescription;
        this.DBmaterial = DBmaterial;
        this.DBunit = DBunit;
        this.DBname = DBname;
    }

    public int getPartId() {
        return partId;
    }

    public void setPartId(int partId) {
        this.partId = partId;
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

    public String getDBdescription() {
        return DBdescription;
    }

    public String getDBmaterial() {
        return DBmaterial;
    }

    public String getDBunit() {
        return DBunit;
    }

    public String getDBname() {
        return DBname;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getDBtotalQuantityPrice() {
        return DBprice * quantity;
    }

    public void setDBtotalQuantityPrice(double DBtotalQuantityPrice) {
        this.DBtotalQuantityPrice = DBtotalQuantityPrice;
    }

    @Override
    public String toString() {
        return "CarportPart{" +
                "type=" + type +
                ", quantity=" + quantity +
                ", DBprice=" + DBprice +
                ", DBlength=" + DBlength +
                ", DBheight=" + DBheight +
                ", DBwidth=" + DBwidth +
                '}';
    }
}
