package app.entities;

public class Carport {
    private int orderId;
    private int userId;
    private String orderdate;
    private int orderprice;
    private String status;
    private int length;
    private int width;
    private boolean withRoof;

    public Carport(int orderId, int userId, String orderdate, int orderprice, String status, int length, int width, boolean withRoof) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderdate = orderdate;
        this.orderprice = orderprice;
        this.status = status;
        this.length = length;
        this.width = width;
        this.withRoof = withRoof;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getUserId() {
        return userId;
    }

    public String getOrderdate() {
        return orderdate;
    }

    public int getOrderprice() {
        return orderprice;
    }

    public String getStatus() {
        return status;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public boolean isWithRoof() {
        return withRoof;
    }
}
