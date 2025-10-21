# Liquid on Truffle

**ATTENTION**: this is still in development and far from being feature-complete. Pull requests are very welcome 
though!

A parser and interpreter for the [Liquid](https://shopify.github.io/liquid/) language implemented as a GraalVM Truffle language.

Its main goal is to be a fast, JVM-based Liquid implementation, based on a hand-rolled lexer and recursive descent 
parser, which then generates an AST based on the Truffle Framework (from GraalVM).

In order to be as simple as possible, we only aim to be compatible with Shopify Liquid, not
other alternatives such as Jekyll.

Functionality supported now:
- Text segments
- `{{ variable }}` 
- Filter pipelines like `{{ name | upcase }}` or `{{ a | append: b }}`

To be done:
- Ifs: `{% if variable %}...{% endif %}` with truthy evaluation per Liquid basics
- Case
- Loops
- Comments
- Variables: assign, capture, increment, decrement
- Ensuring multi-line works correctly
- Reporting parsing / runtime issues with correct line / column numbers
- Annotating AST nodes and related classes with Truffle annotations
- Generating Liquid pre-compiled binaries
- Support for all original Liquid filter functions
- Support for custom filter functions
- Test suite checking full compatibility with Shopify Liquid

## Build & Run

```bash
cd liquid-truffle
./gradlew run --args='--template "Hello, world!"'
```

Below does not work (yet!):
```bash
cd liquid-truffle
./gradlew run --args='--template "Hello, {{ name | upcase }}!" --vars "{\"name\":\"world\"}"'
```

Or render a file:

```bash
./gradlew run --args='--file sample.liquid --vars "{\"greet\":true,\"name\":\"world\"}"'
```

## Attribution
- Liquid language reference: [shopify.github.io/liquid](https://shopify.github.io/liquid/)
