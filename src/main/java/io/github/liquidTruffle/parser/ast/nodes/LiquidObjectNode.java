package io.github.liquidTruffle.parser.ast.nodes;

import io.github.liquidTruffle.parser.ast.AstNode;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(description = "Represents Liquid objects that hold a child object, which can be either a literal or a variable")
public class LiquidObjectNode extends AstNode {
    @Child
    private AstNode initialNode;
    @Children
    private FilterNode[] filters;

    public LiquidObjectNode(AstNode initialNode, FilterNode[] filters) {
        this.initialNode = initialNode;
        this.filters = filters;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return initialNode.executeGeneric(frame);
    }

    public FilterNode[] getFilters() {
        return filters;
    }

    // Expose child for testing purposes
    public AstNode getChildNode() {
        return this.initialNode;
    }
}