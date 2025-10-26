package io.github.liquidTruffle.parser.ast.nodes;

import io.github.liquidTruffle.parser.ast.AstNode;
import io.github.liquidTruffle.runtime.LiquidRuntimeUtils;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(description = "Represents conditional if statements that render content based on condition truthiness")
public class IfNode extends AstNode {
    @Child
    private AstNode condition;
    @Children
    private AstNode[] body;
    
    public IfNode(AstNode condition, AstNode[] body) {
        this.condition = condition;
        this.body = body;
    }
    
    @Override
    public String executeGeneric(VirtualFrame frame) {
        Object conditionValue = condition.executeGeneric(frame);
        if (LiquidRuntimeUtils.isTruthy(conditionValue)) {
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

    public AstNode getCondition() {
        return condition;
    }

    public AstNode[] getBody() {
        return body;
    }
}