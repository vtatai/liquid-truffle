package io.github.liquidTruffle;

import io.github.liquidTruffle.filters.BuiltinFilters;
import io.github.liquidTruffle.filters.Filter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.liquidTruffle.parser.LiquidLanguage;
import io.github.liquidTruffle.parser.LiquidParserFacade;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, String> cli = parseArgs(args);
        String template = cli.get("template");
        if (template == null && cli.get("file") != null) {
            try {
                template = Files.readString(Path.of(cli.get("file")));
            } catch (Exception e) {
                System.err.println("Error reading file: " + e.getMessage());
                System.exit(1);
            }
        }
        
        if (template == null) {
            System.err.println("Provide --template or --file");
            System.exit(2);
        }
        
        Map<String, Object> vars = parseJson(cli.get("vars"));

        LiquidParserFacade parser = new LiquidParserFacade();
        var root = parser.parse(new LiquidLanguage(), template);
        var ct = root.getCallTarget();
        Map<String, Filter> filters = new HashMap<>();
        BuiltinFilters.installInto(filters);
        Object result = ct.call(vars, filters);
        System.out.print(result);
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> m = new HashMap<>();
        int i = 0;
        while (i < args.length) {
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2);
                String value = (i + 1 < args.length) ? args[++i] : "";
                m.put(key, value);
            }
            i++;
        }
        return m;
    }

    private static Map<String, Object> parseJson(String s) {
        if (s == null || s.isEmpty()) return new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(s, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}