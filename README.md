# Liquid on Truffle

A parser and interpreter for the [Liquid](https://shopify.github.io/liquid/) language implemented as a GraalVM Truffle language. 

Its main goal is to be a fast, JVM-based Liquid implementation, based on a hand-rolled lexer and recursive descent 
parser which generates an AST based on the Truffle Framework (from GraalVM).

In order to be as simple as possible, we only aim to be compatible with Shopify Liquid, not
other alternatives such as Jekyll.

Functionality supported now:
- Text segments

Not supported yet:
- `{{ variable }}` and simple filter pipelines like `{{ name | upcase }}` or `{{ a | append: b }}`
- `{% if variable %}...{% endif %}` with truthy evaluation per Liquid basics
- Loops, advanced tags, complex expressions, whitespace control nuances, full set of filters

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
