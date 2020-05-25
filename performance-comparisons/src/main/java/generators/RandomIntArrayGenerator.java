package generators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

final class RandomIntArrayGenerator {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new RuntimeException("Expected 1 argument");
        }
        var rand = new Random();
        var file = new File(args[0]);
        var writer = new FileWriter(file, StandardCharsets.UTF_8);
        writer.write('[');
        for (int i = 0; ; i++) {
            writer.write(Integer.toString(-1_024_000 + rand.nextInt(2_048_000)));
            if (i == 99_999) {
                writer.write(']');
                break;
            } else {
                writer.write(',');
            }
        }
        writer.close();
    }
}
