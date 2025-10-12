package io.github.liquidTruffle.parser;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.TruffleLanguage;

@TruffleLanguage.Registration(
    id = LiquidLanguage.ID,
    name = "Liquid",
    version = "0.1",
    defaultMimeType = LiquidLanguage.MIME,
    characterMimeTypes = {LiquidLanguage.MIME}
)
public class LiquidLanguage extends TruffleLanguage<LiquidLanguage.Context> {
    public static final String ID = "liquid";
    public static final String MIME = "application/x-liquid";

    public static class Context {
        // Empty context class
    }

    @Override
    protected Context createContext(Env env) {
        return new Context();
    }

    @Override
    protected CallTarget parse(ParsingRequest request) {
        String source = request.getSource().getCharacters().toString();
        LiquidParserFacade parser = new LiquidParserFacade();
        var root = parser.parse(this, source);
        return root.getCallTarget();
    }
}