# Liquid on Truffle (Minimal)

A minimal parser and interpreter for a subset of [Liquid](https://shopify.github.io/liquid/) implemented as a GraalVM Truffle language.

Supported now:
- Text segments
- `{{ variable }}` and simple filter pipelines like `{{ name | upcase }}` or `{{ a | append: b }}`
- `{% if variable %}...{% endif %}` with truthy evaluation per Liquid basics

Not supported yet:
- Loops, advanced tags, complex expressions, whitespace control nuances, full set of filters

## Build & Run

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
