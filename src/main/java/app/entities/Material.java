package app.entities;

public class Material {

    private int materialId;    private String name;
    private int price;
    private String description;
    private int length;
    private int height;
    private int width;
    private String type;
    private String material;
    private String unit;

    public Material(int materialId, int price, String description, int length, int height, int width, String type, String material, String unit) {
        this.materialId = materialId;
        this.price = price;
        this.description = description;
        this.length = length;
        this.height = height;
        this.width = width;
        this.type = type;
        this.material = material;
        this.unit = unit;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}

