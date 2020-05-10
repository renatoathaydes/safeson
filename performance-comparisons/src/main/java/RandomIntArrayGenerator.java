import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.stream.IntStream;

public class RandomIntArrayGenerator {
    public static void main(String[] args) throws IOException {
        var rand = new Random();
        var file = new File("src/main/resources/integers.json");
        var writer = new FileWriter(file, StandardCharsets.UTF_8);
        writer.write('[');
        for (int i = 0;; i++) {
            writer.write(Integer.toString(-1_024_000 + rand.nextInt(2_048_000)));
            if (i == 999_999) {
                writer.write(']');
                break;
            } else {
                writer.write(',');
            }
        }
        writer.close();
    }
}
