package io.github.liquidTruffle.parser.ast.nodes;

import io.github.liquidTruffle.parser.ast.AstNode;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(description = "Represents nil literal values in Liquid templates")
public class NilLiteralNode extends AstNode {
    
    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return null;
    }
}