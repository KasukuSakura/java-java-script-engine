package io.github.kasukusakura.jjse;

import java.util.*;

class CompileContextBuilder implements JCompiler.CompileContext.Builder, JCompiler.CompileContext {
    private final Collection<Object> src = new ArrayList<>();
    private final List<String> opts = new ArrayList<>();
    private final List<String> libs = new ArrayList<>();

    @Override
    public Iterator<Object> sources() {
        return src.iterator();
    }

    @Override
    public List<String> options() {
        return Collections.unmodifiableList(opts);
    }

    @Override
    public Builder sources(Object... src) {
        this.src.addAll(Arrays.asList(src));
        return this;
    }

    @Override
    public Builder sources(Iterable<?> src) {
        Kit.addAll(this.src, src);
        return this;
    }

    @Override
    public Builder options(String... opts) {
        return options(Arrays.asList(opts));
    }

    @Override
    public Builder options(Iterable<String> opts) {
        Kit.addAll(this.opts, opts);
        return this;
    }

    @Override
    public Builder lib(String... lib) {
        return lib(Arrays.asList(lib));
    }

    @Override
    public Builder lib(Iterable<String> lib) {
        Kit.addAll(this.libs, lib);
        return this;
    }

    @Override
    public Collection<String> libraries() {
        return Collections.unmodifiableCollection(libs);
    }

    @Override
    public JCompiler.CompileContext build() {
        return this;
    }
}
