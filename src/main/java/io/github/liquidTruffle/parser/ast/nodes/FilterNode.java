package io.github.liquidTruffle.parser.ast.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import io.github.liquidTruffle.parser.ast.AstNode;

import java.util.Arrays;

@NodeInfo(description = "Represents a a single filter call in Liquid templates")
public class FilterNode extends AstNode {
    private final FilterFunction filterFunction;
    @Children
    private AstNode[] params;

    public FilterNode(FilterFunction function, AstNode[] params) {
        this.filterFunction = function;
        this.params = params;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return filterFunction.function().apply(Arrays.stream(params).map(astNode -> astNode.executeGeneric(frame)).toArray());
    }

    public FilterFunction getFilterFunction() {
        return filterFunction;
    }

    public AstNode[] getParams() {
        return params;
    }
}
