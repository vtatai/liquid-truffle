package io.github.liquidTruffle.parser.ast.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import io.github.liquidTruffle.parser.ast.AstNode;

@NodeInfo(description = "Represents a single filter call in Liquid templates as a binary operator")
public class FilterNode extends AstNode {
    private final FilterFunction filterFunction;
    @Child
    private AstNode inputValue;  // The input value (previous filter or literal/variable)
    @Children
    private AstNode[] parameters;  // The filter parameters

    public FilterNode(FilterFunction function, AstNode inputValue, AstNode[] parameters) {
        this.filterFunction = function;
        this.inputValue = inputValue;
        this.parameters = parameters;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        // Execute the left child (input value)
        Object inputValue = this.inputValue.executeGeneric(frame);
        
        // Execute the right children (filter parameters)
        Object[] allParams = new Object[parameters.length + 1];
        allParams[0] = inputValue;
        
        for (int i = 0; i < parameters.length; i++) {
            allParams[i + 1] = parameters[i].executeGeneric(frame);
        }
        
        return filterFunction.function().apply(allParams);
    }

    public FilterFunction getFilterFunction() {
        return filterFunction;
    }

    public AstNode getInputValue() {
        return inputValue;
    }

    public AstNode[] getParameters() {
        return parameters;
    }
}
