package io.github.liquidTruffle.runtime;

import io.github.liquidTruffle.filters.Filter;
import com.oracle.truffle.api.frame.VirtualFrame;
import java.util.Map;
import java.util.List;
import java.util.Collections;

public class LiquidRuntime {
    
    public static boolean isTruthy(Object v) {
        if (v == null) {
            return false;
        } else if (v instanceof Boolean) {
            return (Boolean) v;
        } else if (v instanceof Number) {
            return ((Number) v).doubleValue() != 0.0;
        } else {
            return !v.toString().isEmpty();
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getVariables(VirtualFrame frame) {
        Object[] args = frame.getArguments();
        Object o = args.length > 0 ? args[0] : null;
        if (o instanceof Map) {
            return (Map<String, Object>) o;
        } else {
            return Collections.emptyMap();
        }
    }

    public static Object getVariable(VirtualFrame frame, String name) {
        return getVariables(frame).get(name);
    }

    @SuppressWarnings("unchecked")
    public static Object applyFilter(VirtualFrame frame, String filterName, Object input, List<Object> args) {
        Object[] frameArgs = frame.getArguments();
        Object fObj = frameArgs.length > 1 ? frameArgs[1] : null;
        Map<String, Filter> filters = fObj instanceof Map ? (Map<String, Filter>) fObj : Collections.emptyMap();
        Filter filter = filters.get(filterName);
        if (filter == null) {
            return input;
        }
        return filter.apply(input, args, Collections.emptyMap());
    }
}