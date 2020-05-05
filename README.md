# SafeSON

A modern JSON parser for the JVM focused on safety and speed.

SafeSON adheres somewhat strictly to [RFC=8259](https://tools.ietf.org/html/rfc8259), with the only exceptions to that
being configurable via options, mostly in the name of safety.

These options are:

* `int maxStringLength`
* `int maxRecursionDepth`
* `int maxWhitespace`
* `boolean consumeTrailingContent`

Establishing limits to how many whitespaces or String characters may be consumed by the parser, as well as limiting
recursion depth, helps prevent attacks that attempt to exhaust an application of resources.

The `consumeTrailingContent` option, when set to `false`, allows parsing consecutive JSON documents from the same stream.

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
* `Long` where the magnitude is too large for `Integer`.
* `Double` in all other cases.

> To force a number into a specific type, use `Number`'s conversion methods like `number.floatValue()` or `number.intValue()`.

