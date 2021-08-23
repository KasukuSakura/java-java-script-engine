package io.github.kasukusakura.jjse;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;
import java.util.*;

class JCI_JdkCompiler implements JCompiler {
    static final JavaCompiler JAVAC = ToolProvider.getSystemJavaCompiler();

    static final StandardJavaFileManager STANDARD_FILE_MANAGER;

    static {
        if (JAVAC == null) {
            STANDARD_FILE_MANAGER = null;
        } else {
            STANDARD_FILE_MANAGER = JAVAC.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8);
        }
    }

    JCI_JdkCompiler() {
        if (STANDARD_FILE_MANAGER == null) throw new UnsupportedOperationException();
    }

    @Override
    public JCompileResult compile(CompileContext context) {
        assert STANDARD_FILE_MANAGER != null;
        Objects.requireNonNull(context, "context");

        Collection<JavaFileObject> sourceCodes = new ArrayList<>();
        {
            Iterator<Object> sources = context.sources();
            while (sources.hasNext()) {
                Object src = sources.next();
                if (src instanceof JavaFileObject) {
                    sourceCodes.add((JavaFileObject) src);
                } else if (src instanceof String) {
                    String script = (String) src;
                    sourceCodes.add(new SimpleJavaFileObject(URI.create("dnd://s/" + Kit.findClassName(script) + ".java"), JavaFileObject.Kind.SOURCE) {
                        @Override
                        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                            return script;
                        }

                        @Override
                        public InputStream openInputStream() throws IOException {
                            return new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8));
                        }
                    });
                } else if (src instanceof File) {
                    Kit.addAll(sourceCodes, STANDARD_FILE_MANAGER.getJavaFileObjects((File) src));
                } else {
                    throw new UnsupportedOperationException("Unknown how to convert " + src + " to JavaFileObject");
                }
            }
        }

        Map<String, byte[]> outputs = new HashMap<>(sourceCodes.size());
        JCI_JC_ClassSolve solver = new JCI_JC_ClassSolve();
        for (String lib : Kit.nonNullC(context.libraries())) {
            solver.open(lib);
        }

        class B_TF_OS extends ByteArrayOutputStream {
            final String name;

            B_TF_OS(String name) {
                this.name = name;
            }

            @Override
            public void close() throws IOException {
                outputs.put(name, toByteArray());
                reset();
            }
        }

        JavaFileManager jfm = new ForwardingJavaFileManager<JavaFileManager>(STANDARD_FILE_MANAGER) {
            @Override
            public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
                if (location == StandardLocation.CLASS_PATH) {
                    return solver.findAll(packageName, recurse);
                }
                return super.list(location, packageName, kinds, recurse);
            }

            @Override
            public String inferBinaryName(Location location, JavaFileObject file) {
                if (file instanceof JCI_JC_ClassSolve.JFOinJar) {
                    return ((JCI_JC_ClassSolve.JFOinJar) file).binName;
                }
                return super.inferBinaryName(location, file);
            }

            @Override
            public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
                throw new UnsupportedEncodingException();
            }

            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
                return new SimpleJavaFileObject(URI.create("ot://tmsx/" + className + ".class"), kind) {
                    @Override
                    public OutputStream openOutputStream() throws IOException {
                        return new B_TF_OS(className);
                    }
                };
            }
        };
        StringWriter sw = new StringWriter();
        JavaCompiler.CompilationTask task = JAVAC.getTask(
                sw, jfm, null,
                context.options(),
                null,
                sourceCodes
        );

        boolean successful = task.call();
        solver.close();
        if (!successful) {
            return JCompileResult.failed(new IllegalStateException("Compile error:\n\n" + sw + "\n\n"));
        }

        class Response implements JCompileResult.CompiledCode {
            @Override
            public InputStream getClass(String path) {
                byte[] code = outputs.get(path);
                if (code == null) {
                    code = outputs.get(path.replace('/', '.'));
                }
                if (code != null) return new ByteArrayInputStream(code);
                return null;
            }

            @Override
            public Set<String> classes() {
                return outputs.keySet();
            }

            @Override
            public ClassLoader load(ClassLoader parent) {
                ProtectionDomain domain = new ProtectionDomain(null, null);
                return new JJSClassLoader(parent, outputs, domain);
            }
        }

        return JCompileResult.success(new Response());
    }
}
