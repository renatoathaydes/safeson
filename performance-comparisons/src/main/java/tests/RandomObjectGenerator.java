package tests;

import com.devskiller.jfairy.Fairy;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

final class RandomObjectGenerator {

    private static final Random rand = new Random();

    private static final Gson gson = new Gson();

    private static final Fairy[] fairies = new Fairy[]{
            Fairy.create(Locale.ENGLISH),
            Fairy.create(Locale.forLanguageTag("zh")),
            Fairy.create(Locale.forLanguageTag("pl")),
            Fairy.create(Locale.forLanguageTag("ka")),
            Fairy.create(Locale.forLanguageTag("es")),
            Fairy.create(Locale.forLanguageTag("sv")),
            Fairy.create(Locale.forLanguageTag("de")),
    };

    private static final String[] hobbies = new String[]{
            "football", "cycling", "kayak", "tv", "programming", "cooking", "sports", "travelling",
            "swimming", "learning", "languages", "profiling", "json performance testing", "horse riding",
            "movies", "soap opera", "singing", "baseball", "volleyball", "basketball", "sword fighting",
            "wrestling", "piano", "guitar", "violin", "video game", "gaming", "fighting", "cinema", "跑步",
            "體育", "唱歌", "樹擁抱", "電子遊戲", "園藝", "盆栽", "忍者", "釣り", "偽のガールフレンド", "ロボット"
    };

    static String generateRandom() {
        Fairy fairy = fairies[rand.nextInt(fairies.length)];
        var person = fairy.person();
        var address = person.getAddress();
        var object = new TestObject(person.getFirstName(), person.getAge(),
                new Address(address.getAddressLine1(), address.getAddressLine2(), address.getCity(),
                        address.getStreet(), address.getStreetNumber()),
                generateHobbies(), rand.nextBoolean(), generateRandomMappings());
        return gson.toJson(object);
    }

    private static List<String> generateHobbies() {
        var count = rand.nextInt(10);
        var result = new ArrayList<String>(count);
        for (int i = 0; i < count; i++) {
            result.add(hobbies[rand.nextInt(hobbies.length)]);
        }
        return result;
    }

    private static Map<String, String> generateRandomMappings() {
        var count = rand.nextInt(10);
        var result = new HashMap<String, String>(count);
        for (int i = 0; i < count; i++) {
            result.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(generateRandom());
    }

}

class TestObject {
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
}

class Address {
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
}
