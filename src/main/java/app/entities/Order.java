package app.entities;

public class Order {
    private int orderId;
    private double materialCost;
    private double salesPrice;
    private double carportWidth;
    private double carportLength;
    private double carportHeight;
    private int userId;
    private String orderStatus;
    private double shedWidth;
    private double shedLength;
    private String userEmail;
    private String orderDate;
    private String roof;
    private boolean wall;
    private String mobile;

    public Order(int orderId, double materialCost, double salesPrice, double carportWidth, double carportLength, double carportHeight, int userId, String orderStatus, double shedWidth, double shedLength, String userEmail, String orderDate, String roof, boolean wall) {
        this.orderId = orderId;
        this.materialCost = materialCost;
        this.salesPrice = salesPrice;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.carportHeight = carportHeight;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.shedWidth = shedWidth;
        this.shedLength = shedLength;
        this.userEmail = userEmail;
        this.orderDate = orderDate;
        this.roof = roof;
        this.wall = wall;
    }

    public Order(int orderId, double materialCost, double salesPrice, double carportWidth, double carportLength, double carportHeight, int userId, String orderStatus, double shedWidth, double shedLength, String userEmail, String orderDate, String roof, boolean wall, String mobile) {
        this.orderId = orderId;
        this.materialCost = materialCost;
        this.salesPrice = salesPrice;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.carportHeight = carportHeight;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.shedWidth = shedWidth;
        this.shedLength = shedLength;
        this.userEmail = userEmail;
        this.orderDate = orderDate;
        this.roof = roof;
        this.wall = wall;
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public double getMaterialCost() {
        return materialCost;
    }

    public void setMaterialCost(double materialCost) {
        this.materialCost = materialCost;
    }

    public double getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(double salesPrice) {
        this.salesPrice = salesPrice;
    }

    public double getCarportWidth() {
        return carportWidth;
    }

    public void setCarportWidth(double carportWidth) {
        this.carportWidth = carportWidth;
    }

    public double getCarportLength() {
        return carportLength;
    }

    public void setCarportLength(double carportLength) {
        this.carportLength = carportLength;
    }

    public double getCarportHeight() {
        return carportHeight;
    }

    public void setCarportHeight(double carportHeight) {
        this.carportHeight = carportHeight;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public double getShedWidth() {
        return shedWidth;
    }

    public void setShedWidth(double shedWidth) {
        this.shedWidth = shedWidth;
    }

    public double getShedLength() {
        return shedLength;
    }

    public void setShedLength(double shedLength) {
        this.shedLength = shedLength;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getRoof() {
        return roof;
    }

    public void setRoof(String roof) {
        this.roof = roof;
    }

    public boolean isWall() {
        return wall;
    }

    public void setWall(boolean wall) {
        this.wall = wall;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", materialCost=" + materialCost +
                ", salesPrice=" + salesPrice +
                ", carportWidth=" + carportWidth +
                ", carportLength=" + carportLength +
                ", carportHeight=" + carportHeight +
                ", userId=" + userId +
                ", orderStatus='" + orderStatus + '\'' +
                ", shedWidth=" + shedWidth +
                ", shedLength=" + shedLength +
                ", userEmail='" + userEmail + '\'' +
                ", orderDate='" + orderDate + '\'' +
                ", roof='" + roof + '\'' +
                ", wall=" + wall +
                '}';
    }
}

