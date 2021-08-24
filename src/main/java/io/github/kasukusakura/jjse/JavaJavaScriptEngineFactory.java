package io.github.kasukusakura.jjse;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.*;

public class JavaJavaScriptEngineFactory implements ScriptEngineFactory {
    public static final String OPTIONS = "jjse.options";

    private final Map<String, ?> options = new HashMap<>();

    @Override
    public String getEngineName() {
        return "JavaJavaScriptEngineFactory";
    }

    @Override
    public String getEngineVersion() {
        return "0.0.0";
    }

    @Override
    public List<String> getExtensions() {
        return Collections.singletonList("java");
    }

    @Override
    public List<String> getMimeTypes() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList("Java", "java");
    }

    @Override
    public String getLanguageName() {
        return "java";
    }

    @Override
    public String getLanguageVersion() {
        return "0.0.0";
    }

    @Override
    public Object getParameter(String key) {
        switch (key) {
            case ScriptEngine.ENGINE:
                return getEngineName();
            case ScriptEngine.ENGINE_VERSION:
                return getEngineVersion();
            case ScriptEngine.LANGUAGE:
                return getLanguageName();
            case ScriptEngine.LANGUAGE_VERSION:
                return getLanguageVersion();
            case ScriptEngine.NAME:
                return getNames();
            case OPTIONS:
                return options;
        }
        return null;
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return "System.out.println(" + toDisplay + ");";
    }

    @Override
    public String getProgram(String... statements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new JavaJavaScriptEngineImpl(this, JCompiler.getCompiler(options));
    }

    public ScriptEngine getScriptEngine(JCompiler compiler) {
        return new JavaJavaScriptEngineImpl(this, compiler);
    }
}
