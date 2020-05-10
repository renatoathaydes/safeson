import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class CsvReporter {
    static void printReport(List<TestResult> results, int measuredRuns) {
        printHeader(results);
        printData(results, measuredRuns);
    }

    private static void printHeader(List<TestResult> results) {
        // will print the following:
        //
        // Test1  ,       ,       ,Test2  ,       ,       ,Test3
        // parser1,parser2,parser3,parser1,parser2,parser3,parser1,parser2,parser3
        var parserNames = results.stream()
                .flatMap(r -> r.timesPerParser.keySet().stream().sorted())
                .collect(Collectors.toList());
        var testNameSeparator = parserNames.stream().skip(1).map((ignore) -> ",").collect(joining());
        var testNames = results.stream().map((r) -> r.name).collect(toList());
        System.out.println(String.join(testNameSeparator, testNames));
        System.out.println(String.join(",", parserNames));
    }

    private static void printData(List<TestResult> results, int measuredRuns) {
        var times = results.stream()
                .flatMap(r -> r.timesPerParser.entrySet().stream().sorted(comparing(e -> e.getKey())))
                .map(e -> e.getValue())
                .collect(Collectors.toList());
        for (int i = 0; i < measuredRuns; i++) {
            final var idx = i;
            System.out.println(times.stream()
                    .map(t -> t.get(idx))
                    .collect(joining(",")));
        }
    }

}
