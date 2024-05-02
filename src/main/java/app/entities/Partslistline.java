package app.entities;

public class Partslistline {
    private int materialId;
    private int orderId;
    private int quantity;
    private double partlistlineprice;

    public Partslistline(int materialId, int orderId, int quantity, double partlistlineprice) {
        this.materialId = materialId;
        this.orderId = orderId;
        this.quantity = quantity;
        this.partlistlineprice = partlistlineprice;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
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
