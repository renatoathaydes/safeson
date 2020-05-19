# SafeSON

A modern JSON parser for the JVM focused on safety and speed.

SafeSON adheres strictly to [RFC-8259](https://tools.ietf.org/html/rfc8259).

> SafeSON passes all tests mentioned in [Parsing JSON is a minefield](http://seriot.ch/parsing_json.php).
> You can [run the tests yourself](https://github.com/renatoathaydes/JSONTestSuite/tree/master/parsers/test_java_safeson_1_0).
 
In many areas where the specification gives implementation freedom, SafeSON allows users to configure options for the 
desired behaviour.

These options are:

* `int maxStringLength` - `16..` (default `1_024_000`)
* `int maxRecursionDepth` - `4..` (default `512`)
* `int maxWhitespace` - `2..` (default `128`)
* `boolean consumeTrailingContent` (default `true`)
* `DuplicateKeyStrategy duplicateKeyStrategy` - `FAIL` | `KEEP_LAST` | `KEEP_FIRST` (default `FAIL`)

Establishing limits to how many whitespaces or String characters may be consumed by the parser, as well as limiting
recursion depth, helps prevent DDoS attacks that attempt to exhaust an application of resources.

The `consumeTrailingContent` option, when set to `false`, allows parsing consecutive JSON documents from the same stream
(there must be one character, any character, delimiting entries, e.g. `{"a":1} ; {"b": 2}`).

SafeJSON, by default, fails to parse JSON objects containing duplicate keys as they are known to cause
[security issues](https://justi.cz/security/2017/11/14/couchdb-rce-npm.html), and rarely make sense.

But if necessary, you can change the `duplicateKeyStrategy` to keep only the first, or only the last, value.

## Unsupported JSON optional features and extensions

The following optional features of JSON parsers are not supported by SafeSON:

* non-UTF-8 input. Only UTF-8 input is supported (but `\u`-escaped UTF-16 surrogate pairs are allowed).
* non-quoted object keys. The JSON grammar [does not allow](https://tools.ietf.org/html/rfc8259#section-4) object
  keys that are not quoted.
* unconstrained numbers. Only numbers that can fit into Java numeric types (`int`, `long`, `double`) are supported. 

## Design principles

SafeSON strives to stay simple and small, while running as fast as possible.

The types used to represent JSON types are the equivalent Java types:

|      JSON                |       Java          |
|--------------------------|---------------------|
| string                   | String              |
| number                   | Number              |
| true, false              | Boolean             |
| null                     | null                |
| object                   | Map<String, Object> |
| array                    | List<Object>        |

No surprises. No `JsonObject` and the likes.

Numbers are represented by:

* `Integer` where possible (i.e. fraction part is 0, magnitude fits).
* `Long` where the fraction part is 0 but the magnitude is too large for `Integer`.
* `Double` in all other cases.

> To force a number into a specific type, use `Number`'s conversion methods like `number.floatValue()` or `number.intValue()`.

## POJO Mapping

SafeSON also supports parsing JSON objects into POJOs (Plain-Old-Java-Object).

> As soon as records are released into Java, SafeSON will be able to support them with very little change.

POJOs are expected to be similar to the upcoming [Java records](https://blogs.oracle.com/javamagazine/records-come-to-java):

* One or more constructors define the fields required to build it.
* Must be compiled with the `-parameters` option (to keep parameter names in bytecode).

To avoid any possibility of unexpected classes being loaded by SafeSON, all POJO types must be whitelisted
explicitly when creating a new instance of `JSON` by using the `Pojos` object. 

For example, a JSON object like this:

```json
{
  "name": "Joe",
  "age": 15
}
```

Can be de-serialized directly into a POJO like this:

```java
import com.athaydes.json.JSON;
import com.athaydes.json.pojo.Pojos;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Main {
    public static void main(String[] args){
              var parser = new JSON(Pojos.of(Person.class));
              var person = parser.parse("{\n" +
                      "  \"name\": \"Joe\",\n" +
                      "  \"age\": 15\n" +
                      "}", Person.class);
              assertEquals("Joe", person.name);
              assertEquals(15, person.age);
    }
}

final class Person {
    final String name;
    final int age;
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    } 
}
```

Notice that only the constructor parameter names matter. Fields and getters are not used.

The JSON object may contain extra fields that are not present in the POJO's constructors.

`Optional` can be used in constructor parameters to allow JSON objects missing certain fields to de-serialize into
a POJO. The field will be set if present, but will be an instance of `Optional.empty` otherwise.

POJOs can have nested POJOs as long as their types can also be de-serialized by SafeSON. 
