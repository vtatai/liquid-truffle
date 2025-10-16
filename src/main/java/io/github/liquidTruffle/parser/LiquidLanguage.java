package io.github.liquidTruffle.parser;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import io.github.liquidTruffle.parser.ast.AstNode;

@TruffleLanguage.Registration(
    id = LiquidLanguage.ID,
    name = "Liquid",
    version = "0.1",
    defaultMimeType = LiquidLanguage.MIME,
    characterMimeTypes = {LiquidLanguage.MIME}
)
public class LiquidLanguage extends TruffleLanguage<LiquidContext> {
    public static final String ID = "liquid";
    public static final String MIME = "application/x-liquid";
    private static final ContextReference<LiquidContext> CONTEXT_REFERENCE =
            ContextReference.create(LiquidLanguage.class);

    @Override
    protected LiquidContext createContext(Env env) {
        return new LiquidContext();
    }

    public static LiquidContext getContext(AstNode node) {
        return CONTEXT_REFERENCE.get(node);
    }

    @Override
    protected CallTarget parse(ParsingRequest request) {
        String source = request.getSource().getCharacters().toString();
        LiquidParserFacade parser = new LiquidParserFacade();
        var root = parser.parse(this, source);
        return root.getCallTarget();
    }

    @Override
    protected Object getScope(LiquidContext context) {
        return context.getGlobalScopeObject();
    }
}