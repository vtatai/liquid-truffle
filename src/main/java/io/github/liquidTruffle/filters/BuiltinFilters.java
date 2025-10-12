package io.github.liquidTruffle.filters;

import java.util.Map;

public class BuiltinFilters {
    public static void installInto(Map<String, Filter> out) {
        out.put("upcase", (input, args, kwargs) -> input != null ? input.toString().toUpperCase() : "");
        
        out.put("downcase", (input, args, kwargs) -> input != null ? input.toString().toLowerCase() : "");
        
        out.put("append", (input, args, kwargs) -> {
            Object rhs = args.isEmpty() ? "" : args.get(0);
            return (input != null ? input.toString() : "") + (rhs != null ? rhs.toString() : "");
        });
    }
}