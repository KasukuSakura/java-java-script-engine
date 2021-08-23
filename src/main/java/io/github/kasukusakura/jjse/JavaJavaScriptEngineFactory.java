package io.github.kasukusakura.jjse;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JavaJavaScriptEngineFactory implements ScriptEngineFactory {
    static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();

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
        return new JavaJavaScriptEngineImpl(JCompiler.getCompiler(Collections.emptyMap()));
    }

    public ScriptEngine getScriptEngine(JCompiler compiler) {
        return new JavaJavaScriptEngineImpl(compiler);
    }
}
