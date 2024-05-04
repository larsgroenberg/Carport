package app.entities;

public class Partslistline {
    private int partId;
    private int orderId;
    private int quantity;
    private double partlistlineprice;

    public Partslistline(int partId, int orderId, int quantity, double partlistlineprice) {
        this.partId = partId;
        this.orderId = orderId;
        this.quantity = quantity;
        this.partlistlineprice = partlistlineprice;
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
}
