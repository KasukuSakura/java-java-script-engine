package io.github.kasukusakura.jjse;

import javax.script.*;
import java.io.IOException;
import java.io.Reader;
import java.util.UUID;
import java.util.concurrent.Callable;

@SuppressWarnings("unchecked")
class JavaJavaScriptEngineImpl extends AbstractScriptEngine {
    ScriptEngineFactory factory;
    JCompiler compiler;

    JavaJavaScriptEngineImpl(
            ScriptEngineFactory factory,
            JCompiler compiler
    ) {
        this.factory = factory;
        this.compiler = compiler;
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        try {
            return eval(Kit.readAll(reader));
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        try {
            return eval(Kit.readAll(reader), bindings);
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }


    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        try {
            return eval(Kit.readAll(reader), context);
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public Object eval(String script) throws ScriptException {
        return super.eval(script);
    }

    @Override
    public Object eval(String script, Bindings bindings) throws ScriptException {
        JCompiler.CompileContext.Builder builder = JCompiler.CompileContext.newBuilder();

        String pkgName = "jjse.dyncode.c" + (UUID.randomUUID().toString().replace('-', '_'));
        String cname = "DynCode";
        {
            Object fname = bindings.get(ScriptEngine.FILENAME);
            if (fname != null) cname = fname.toString();
        }
        {

            StringBuilder finallyCode = new StringBuilder(script.length());
            // /*!end-imports!*/
            int ind = script.indexOf("/*!end-imports!*/");
            finallyCode.append("package ").append(pkgName).append(';');
            int svs;
            if (ind > 0) {
                finallyCode.append(script, 0, ind);
                svs = ind;
            } else {
                svs = 0;
            }

            boolean hasReturnValue = bindings.containsKey("hasReturn") && Kit.parseBoolean(bindings.get("hasReturn"));

            finallyCode.append("public class ").append(cname).append(" implements ");
            finallyCode.append("java.util.concurrent.Callable<Object>");
            finallyCode.append("{");


            finallyCode.append("protected ")
                    .append(hasReturnValue ? "java.lang.Object" : "void")
                    .append(" execute() throws java.lang.Throwable {");
            finallyCode.append(script, svs, script.length());
            finallyCode.append("}");

            finallyCode.append("public java.lang.Object call() throws java.lang.Exception {");

            finallyCode
                    .append("try {")
                    .append(hasReturnValue ? "return execute();" : "execute(); return null;")
                    .append("} ");
            finallyCode.append("catch (java.lang.Exception e) { throw e; } ");
            finallyCode.append("catch (java.lang.Error e) { throw e; } ");
            finallyCode.append("catch (java.lang.Throwable e) { throw new java.lang.RuntimeException(e); } ");

            finallyCode.append("}");


            finallyCode.append("}");
            builder.sources(finallyCode.toString());
        }

        {
            Object lib = bindings.get("lib");
            if (lib != null) {
                if (lib instanceof String) {
                    builder.lib(lib.toString());
                } else if (lib instanceof String[]) {
                    builder.lib((String[]) lib);
                } else if (lib instanceof Iterable) {
                    builder.lib((Iterable<String>) lib);
                } else {
                    builder.lib(lib.toString());
                }
            }
        }
        {
            Object opt = bindings.get("opt");
            if (opt != null) {
                if (opt instanceof String) {
                    builder.options(opt.toString());
                } else if (opt instanceof String[]) {
                    builder.options((String[]) opt);
                } else if (opt instanceof Iterable) {
                    builder.options((Iterable<String>) opt);
                } else {
                    builder.options(opt.toString());
                }
            }
        }
        ClassLoader loader = (ClassLoader) bindings.get("classLoader");
        if (loader == null && !bindings.containsKey("classLoader")) {
            loader = ClassLoader.getSystemClassLoader();
        }

        JCompileResult compile = compiler.compile(builder.build());
        if (compile.isSuccessful()) {
            ClassLoader classLoader = compile.compiledCode().load(loader);
            try {
                return classLoader
                        .loadClass(pkgName + "." + cname)
                        .asSubclass(Callable.class)
                        .getConstructor()
                        .newInstance()
                        .call();
            } catch (Throwable throwable) {
                throw (ScriptException) new ScriptException(throwable.getMessage()).initCause(throwable);
            }
        } else {
            Throwable throwable = compile.exceptionOrNull();
            throw (ScriptException) (new ScriptException(throwable.getMessage()).initCause(throwable));
        }
    }


    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        return eval(script, context.getBindings(ScriptContext.GLOBAL_SCOPE));
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }
}
