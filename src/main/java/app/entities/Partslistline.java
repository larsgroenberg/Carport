package app.entities;

public class Partslistline {
    private int partId;
    private int orderId;
    private int quantity;
    private double partlistlineprice;
    private String description;
    private String unit;
    private int partLength;


    public Partslistline(int partId, int orderId, int quantity, double partlistlineprice, String description, String unit, int partLength) {
        this.partId = partId;
        this.orderId = orderId;
        this.quantity = quantity;
        this.partlistlineprice = partlistlineprice;
        this.description = description;
        this.unit = unit;
        this.partLength = partLength;

    }

    public int getPartId() {
        return partId;
    }

    public void setPartId(int partId) {
        this.partId = partId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPartlistlineprice() {
        return partlistlineprice;
    }

    public void setPartlistlineprice(double partlistlineprice) {
        this.partlistlineprice = partlistlineprice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getPartLength() {
        return partLength;
    }

    public void setPartLength(int partLength) {
        this.partLength = partLength;
    }
}
