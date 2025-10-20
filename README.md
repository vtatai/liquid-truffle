# Liquid on Truffle

A parser and interpreter for the [Liquid](https://shopify.github.io/liquid/) language implemented as a GraalVM Truffle language. 

Its main goal is to be a fast, JVM-based Liquid implementation, based on a hand-rolled lexer and recursive descent 
parser which generates an AST based on the Truffle Framework (from GraalVM).

In order to be as simple as possible, we only aim to be compatible with Shopify Liquid, not
other alternatives such as Jekyll.

Functionality supported now:
- Text segments
- `{{ variable }}` 
- Filter pipelines like `{{ name | upcase }}` or `{{ a | append: b }}`

To be done:
- Annotating AST nodes and related classes with Truffle annotations
- `{% if variable %}...{% endif %}` with truthy evaluation per Liquid basics
- Support for all original Liquid filter functions
- Loops, advanced tags, complex expressions, whitespace control nuances
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
