package io.github.liquidTruffle.parser.ast.nodes;

import java.util.function.Function;

public record FilterFunction(String name, Function<Object[], Object> function)  { }