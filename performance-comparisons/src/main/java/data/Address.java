package data;

public class Address {
    String addressLine1;
    String addressLine2;
    String city;
    String street;
    String streetNumber;

    public Address() {
    }

    public Address(String addressLine1, String addressLine2, String city, String street, String streetNumber) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.street = street;
        this.streetNumber = streetNumber;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }
}
