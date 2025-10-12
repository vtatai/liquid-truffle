package io.github.liquidTruffle.parser.ast.nodes;

import io.github.liquidTruffle.parser.ast.AstNode;
import io.github.liquidTruffle.runtime.LiquidRuntime;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(description = "Represents conditional if statements that render content based on variable truthiness")
public class IfNode extends AstNode {
    private final String variableName;
    private final AstNode[] body;
    
    public IfNode(String variableName, AstNode[] body) {
        this.variableName = variableName;
        this.body = body;
    }
    
    @Override
    public String executeGeneric(VirtualFrame frame) {
        Object v = LiquidRuntime.getVariable(frame, variableName);
        if (LiquidRuntime.isTruthy(v)) {
            StringBuilder result = new StringBuilder();
            for (AstNode node : body) {
                Object value = node.executeGeneric(frame);
                result.append(value != null ? value.toString() : "");
            }
            return result.toString();
        } else {
            return "";
        }
    }
}