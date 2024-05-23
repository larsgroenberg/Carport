package app.entities;

public class User {
    private int userId;
    private String email;
    private String password;
    private boolean isAdmin;
    private String name;
    private String mobile;
    private String address;
    private String zipcode;

    public User(int userId, String email, String password, boolean isAdmin, String name, String mobile, String address, String zipcode) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.name = name;
        this.mobile = mobile;
        this.address = address;
        this.zipcode = zipcode;
    }

    public String getAddress() {
        return address;
    }


    public String getZipcode() {
        return zipcode;
    }

    public int getUserId() {
        return userId;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }


    public boolean isAdmin() {
        return isAdmin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

}
