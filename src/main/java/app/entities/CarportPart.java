package app.entities;

public class CarportPart {
    public enum CarportPartType {
        STOLPE,
        SPÆR,
        REM,
        HULBÅND,
        TAGPLADER,
        OVERSTERN,
        UNDERSTERN,
        BRÆDDER,
        VANDBRÆDDER,
        REGLAR,
        LÆGTE,
        SKRUER,
        UNIVERSALBESLAG,
        BOLTE,
        FIRKANTSKIVER,
        HÆNGSLER,
        NONE
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
    private String DBtype;
    private double DBtotalQuantityPrice;


    public CarportPart(CarportPartType type, int quantity) {
        this.type = type;
        this.quantity = quantity;
    }

    public CarportPart(CarportPartType type, int quantity,int partId,double DBprice, int DBlength, int DBheight, int DBwidth, String DBdescription, String DBmaterial, String DBunit, String DBname, String DBtype) {
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
        this.DBtype = DBtype;
    }
    public CarportPart(int partId, CarportPartType type, int quantity, double DBprice, int DBlength, int DBheight, int DBwidth,
                       String DBdescription, String DBmaterial, String DBunit, String DBname) {
        this.partId = partId;
        this.type = type;
        this.quantity = quantity;
        this.DBprice = DBprice;
        this.DBlength = DBlength;
        this.DBheight = DBheight;
        this.DBwidth = DBwidth;
        this.DBdescription = DBdescription;
        this.DBmaterial = DBmaterial;
        this.DBunit = DBunit;
        this.DBname = DBname;
        this.DBtotalQuantityPrice = quantity * DBprice;  // Calculate total price
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

    public void setType(CarportPartType type) {
        this.type = type;
    }

    public void setDBprice(double DBprice) {
        this.DBprice = DBprice;
    }

    public void setDBlength(int DBlength) {
        this.DBlength = DBlength;
    }

    public void setDBheight(int DBheight) {
        this.DBheight = DBheight;
    }

    public void setDBwidth(int DBwidth) {
        this.DBwidth = DBwidth;
    }

    public void setDBdescription(String DBdescription) {
        this.DBdescription = DBdescription;
    }

    public void setDBmaterial(String DBmaterial) {
        this.DBmaterial = DBmaterial;
    }

    public void setDBunit(String DBunit) {
        this.DBunit = DBunit;
    }

    public void setDBname(String DBname) {
        this.DBname = DBname;
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
