package io.github.liquidTruffle.filters;

import java.util.List;
import java.util.Map;

public class BuiltinFilters {
    public static void installInto(Map<String, Filter> out) {
        out.put("upcase", new Filter() {
            @Override
            public Object apply(Object input, List<Object> args, Map<String, Object> kwargs) {
                return input != null ? input.toString().toUpperCase() : "";
            }
        });
        
        out.put("downcase", new Filter() {
            @Override
            public Object apply(Object input, List<Object> args, Map<String, Object> kwargs) {
                return input != null ? input.toString().toLowerCase() : "";
            }
        });
        
        out.put("append", new Filter() {
            @Override
            public Object apply(Object input, List<Object> args, Map<String, Object> kwargs) {
                Object rhs = args.isEmpty() ? "" : args.get(0);
                return (input != null ? input.toString() : "") + (rhs != null ? rhs.toString() : "");
            }
        });
    }
}