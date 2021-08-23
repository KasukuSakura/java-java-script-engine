package io.github.kasukusakura.jjse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public interface JCompiler {
    public interface CompileContext {
        Iterator<Object> sources();

        List<String> options();

        Collection<String> libraries();

        interface Builder {
            Builder sources(Object... src);

            Builder sources(Iterable<?> src);

            Builder options(String... opts);

            Builder options(Iterable<String> opts);

            Builder lib(String... lib);

            Builder lib(Iterable<String> lib);

            CompileContext build();
        }

        static Builder newBuilder() {
            return new CompileContextBuilder();
        }
    }

    public JCompileResult compile(CompileContext context);

    static JCompiler getCompiler(Map<String, ?> options) {
        return JCAllocate.get(options);
    }
}

class JCAllocate {
    static JCompiler get(Map<String, ?> options) {
        String type = (String) options.get("type");
        if (type == null) {
            if (JCI_JdkCompiler.JAVAC != null) {
                return getSys(options);
            }
            return getJavac(options);
        }
        switch (type) {
            case "javac":
                return getJavac(options);
            case "jdk":
            case "built":
            case "system":
                return getSys(options);
            default:
                throw new NoSuchElementException("No such compiler with type " + type);
        }
    }

    static JCompiler getSys(Map<String, ?> opt) {
        if (JCI_JdkCompiler.JAVAC == null)
            throw new UnsupportedOperationException("System JavaCompiler not supported for " + Kit.jrt() + ". use `$JDK/bin/java` and rerun");
        return new JCI_JdkCompiler();
    }

    static JCompiler getJavac(Map<String, ?> opt) {
        String bin = Kit.toStringOrNull(opt.get("binary"));
        if (bin == null) {
            bin = Kit.toStringOrNull(opt.get("javac"));
        }
        if (bin == null) {
            bin = Kit.toStringOrNull(opt.get("executable"));
        }
        if (bin == null) {
            try {
                if (new ProcessBuilder("javac", "-version").start().waitFor() == 0) {
                    bin = "javac";
                }
            } catch (Throwable ignore) {
            }
        }
        if (bin == null)
            throw new IllegalArgumentException("No javac executable found: missing option `binary`");

        String tmp = Kit.toStringOrNull(opt.get("tmp"));
        File tmp0;
        if (tmp == null) {
            try {
                tmp0 = Files.createTempDirectory("java-java-script-engine-").toFile();
            } catch (IOException e) {
                return Kit.throwExceptionRT(e);
            }
        } else {
            File target = new File(tmp, "jjse");
            Kit.deleteRecurse(target);
            target.mkdirs();
            tmp0 = target;
        }
        return new JCI_Javac(tmp0, bin);
    }
}
