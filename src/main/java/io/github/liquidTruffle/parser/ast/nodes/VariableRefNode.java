package io.github.liquidTruffle.parser.ast.nodes;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import io.github.liquidTruffle.LiquidRuntimeException;
import io.github.liquidTruffle.parser.GlobalScopeObject;
import io.github.liquidTruffle.parser.LiquidLanguage;
import io.github.liquidTruffle.parser.ast.AstNode;

import static java.lang.String.format;

@NodeInfo(description = "Represents variable reference")
@NodeField(name = "name", type = String.class)
public class VariableRefNode extends AstNode {
    private final String name;

    public VariableRefNode(String name) {
        this.name = name;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        GlobalScopeObject globalScopeObject = LiquidLanguage.getContext(this).getGlobalScopeObject();
        if (!globalScopeObject.containsVariable(name)) {
            throw new LiquidRuntimeException(format("Variable %s is undefined", name), this);
        }
        return globalScopeObject.get(name);
    }
    
    // Expose fields for testing purposes
    public String getName() {
        return this.name;
    }
}