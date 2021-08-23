package io.github.kasukusakura.jjse;

import java.io.InputStream;
import java.util.Set;

public interface JCompileResult {
    Throwable exceptionOrNull();

    boolean isSuccessful();


    interface CompiledCode {
        InputStream getClass(String path);

        Set<String> classes();

        ClassLoader load(ClassLoader parent);

        default void cleanup() {
        }
    }

    CompiledCode compiledCode();

    static JCompileResult failed(Throwable exception) {
        return new JCompileResult_ERROR(exception);
    }

    static JCompileResult success(CompiledCode code) {
        return new JCompileResult_SUCCESS(code);
    }
}

class JCompileResult_SUCCESS implements JCompileResult {
    private final CompiledCode code;

    JCompileResult_SUCCESS(CompiledCode code) {
        this.code = code;
    }

    @Override
    public Throwable exceptionOrNull() {
        return null;
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public CompiledCode compiledCode() {
        return code;
    }
}

class JCompileResult_ERROR implements JCompileResult {
    private final Throwable error;

    JCompileResult_ERROR(Throwable error) {
        this.error = error;
    }

    @Override
    public Throwable exceptionOrNull() {
        return error;
    }

    @Override
    public boolean isSuccessful() {
        return false;
    }

    @Override
    public CompiledCode compiledCode() {
        return Kit.throwExceptionRT(error);
    }
}
