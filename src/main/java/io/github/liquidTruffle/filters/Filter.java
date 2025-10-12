package io.github.liquidTruffle.filters;

import java.util.List;
import java.util.Map;

public interface Filter {
    Object apply(Object input, List<Object> args, Map<String, Object> kwargs);
}