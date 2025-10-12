package io.github.liquidTruffle.parser.ast.nodes;

import io.github.liquidTruffle.parser.ast.AstNode;
import io.github.liquidTruffle.runtime.LiquidRuntime;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import java.util.List;
import java.util.ArrayList;

@NodeInfo(description = "Represents variable references with optional filters in Liquid templates")
public class VariableNode extends AstNode {
    private final String name;
    private final List<FilterSpec> filters;

    public VariableNode(String name, List<FilterSpec> filters) {
        this.name = name;
        this.filters = filters;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object current = LiquidRuntime.getVariable(frame, name);
        for (FilterSpec filter : filters) {
            List<Object> evaluatedArgs = new ArrayList<>();
            for (AstNode arg : filter.getArgs()) {
                evaluatedArgs.add(arg.executeGeneric(frame));
            }
            current = LiquidRuntime.applyFilter(frame, filter.getName(), current, evaluatedArgs);
        }
        return current;
    }
    
    // Expose fields for testing purposes
    public String getVariableName() {
        return this.name;
    }
    
    public List<FilterSpec> getFilterSpecs() {
        return this.filters;
    }

    public static class FilterSpec {
        private final String name;
        private final List<AstNode> args;

        public FilterSpec(String name, List<AstNode> args) {
            this.name = name;
            this.args = args != null ? args : new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public List<AstNode> getArgs() {
            return args;
        }
    }
}