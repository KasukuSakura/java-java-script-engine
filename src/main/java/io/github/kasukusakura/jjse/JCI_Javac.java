package io.github.kasukusakura.jjse;

import javax.tools.JavaFileObject;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class JCI_Javac implements JCompiler {
    final File temp;
    private final String javac;

    JCI_Javac(File tmp, String javac) {
        this.temp = Objects.requireNonNull(tmp, "tmp");
        this.javac = javac;
    }

    @Override
    public JCompileResult compile(CompileContext context) {
        File build = findTmpDir();
        File tmpSrc = new File(build, "src");
        File out = new File(build, "classes");
        out.mkdirs();
        List<String> cmd = new ArrayList<>();
        cmd.add(javac);

        cmd.add("-d");
        cmd.add(out.toString());

        {
            Collection<String> libraries = context.libraries();
            if (libraries != null && !libraries.isEmpty()) {
                cmd.add("-cp");
                cmd.add(String.join(File.pathSeparator, libraries));
            }
        }

        {
            Iterator<Object> sources = context.sources();
            while (sources.hasNext()) {
                Object src = sources.next();
                if (src instanceof File) {
                    cmd.add(src.toString());
                } else if (src instanceof String) {
                    tmpSrc.mkdirs();
                    String script = (String) src;
                    try {
                        Path p = tmpSrc.toPath().resolve(Kit.findClassName(script) + ".java");
                        Files.write(
                                p,
                                script.getBytes(StandardCharsets.UTF_8)
                        );
                        cmd.add(p.toString());
                    } catch (IOException e) {
                        return JCompileResult.failed(e);
                    }
                } else if (src instanceof JavaFileObject) {
                    JavaFileObject fileObject = (JavaFileObject) src;
                    URI uri = fileObject.toUri();
                    if ("file".equals(uri.getScheme())) {
                        cmd.add(Paths.get(uri).toFile().toString());
                    } else {
                        String n = uri.getPath();
                        tmpSrc.mkdirs();
                        {
                            int i = n.lastIndexOf('/');
                            if (i != -1) n = n.substring(i + 1);
                        }
                        File oot = new File(tmpSrc, n);
                        cmd.add(oot.toString());
                        try (InputStream stream = new BufferedInputStream(fileObject.openInputStream());
                             OutputStream os = new BufferedOutputStream(new FileOutputStream(oot))) {
                            Kit.transfer(stream, os);
                        } catch (Throwable throwable) {
                            return JCompileResult.failed(throwable);
                        }
                    }
                } else {
                    throw new UnsupportedOperationException("Unknown how to process " + src);
                }
            }
        }

        // System.out.println(cmd);

        try {
            Process process = new ProcessBuilder(cmd)
                    //.inheritIO()
                    .start();
            class BBOS extends ByteArrayOutputStream {
                public String complete() {
                    return new String(buf, 0, count, StandardCharsets.UTF_8);
                }
            }
            BBOS bos = new BBOS();
            Kit.transfer(process.getErrorStream(), bos);
            if (process.waitFor() != 0) {
                Kit.deleteRecurse(build);
                return JCompileResult.failed(new IllegalStateException(bos.complete()));
            }

            Path ootPath = out.toPath();
            class Response implements JCompileResult.CompiledCode {
                private final List<URLClassLoader> cls = new ArrayList<>();

                @Override
                public InputStream getClass(String path) {
                    File file = new File(out, path.replace('.', '/') + ".class");
                    if (file.isFile()) {
                        try {
                            return new FileInputStream(file);
                        } catch (FileNotFoundException ignore) {
                        }
                    }
                    return null;
                }

                @Override
                public Set<String> classes() {
                    try (Stream<String> classes = Files.walk(ootPath)
                            .filter(it -> !Files.isDirectory(it))
                            .filter(it -> it.getFileName().toString().endsWith(".class"))
                            .map(it -> it.relativize(ootPath))
                            .map(it -> it.toString().replace(File.separatorChar, '.'))
                            .map(it -> it.substring(0, it.length() - 6))) {
                        return classes.collect(Collectors.toSet());
                    } catch (Throwable ignore) {
                    }
                    return Collections.emptySet();
                }

                @Override
                public ClassLoader load(ClassLoader parent) {
                    try {
                        URLClassLoader urlc = new URLClassLoader(new URL[]{
                                out.toURI().toURL()
                        }, parent);
                        cls.add(urlc);
                        return urlc;
                    } catch (Throwable throwable) {
                        return Kit.throwExceptionRT(throwable);
                    }
                }

                @Override
                public void cleanup() {
                    for (URLClassLoader uc : cls) {
                        try {
                            uc.close();
                        } catch (IOException ignore) {
                        }
                    }
                    cls.clear();
                    Kit.deleteRecurse(build);
                }
            }

            return JCompileResult.success(new Response());
        } catch (Throwable e) {
            return JCompileResult.failed(e);
        }
    }

    private File findTmpDir() {
        while (true) {
            File tmp = new File(this.temp, UUID.randomUUID().toString());
            if (!tmp.exists())
                return tmp;
        }
    }
}
