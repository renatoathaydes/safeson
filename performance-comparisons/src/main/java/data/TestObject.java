package data;

import java.util.List;
import java.util.Map;

public class TestObject {
    String name;
    int age;
    Address address;
    List<String> hobbies;
    boolean subscribed;
    Map<String, String> mappings;

    public TestObject() {
    }

    public TestObject(String name, int age, Address address, List<String> hobbies, boolean subscribed,
                      Map<String, String> mappings) {
        this.name = name;
        this.age = age;
        this.address = address;
        this.hobbies = hobbies;
        this.subscribed = subscribed;
        this.mappings = mappings;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setHobbies(List<String> hobbies) {
        this.hobbies = hobbies;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }
}
