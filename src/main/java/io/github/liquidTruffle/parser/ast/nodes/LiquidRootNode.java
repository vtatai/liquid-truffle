package io.github.liquidTruffle.parser.ast.nodes;

import io.github.liquidTruffle.parser.ast.AstNode;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(description = "Root node of the Liquid template AST that orchestrates execution of child nodes")
public class LiquidRootNode extends RootNode {
    private final AstNode[] children;
    
    public LiquidRootNode(TruffleLanguage<?> language, AstNode[] children) {
        super(language);
        this.children = children;
    }
    
    @Override
    public String execute(VirtualFrame frame) {
        StringBuilder result = new StringBuilder();
        for (AstNode node : children) {
            Object value = node.executeGeneric(frame);
            result.append(value != null ? value.toString() : "");
        }
        return result.toString();
    }
    
    // Expose children for testing purposes
    public AstNode[] getChildrenNodes() {
        return this.children;
    }
}