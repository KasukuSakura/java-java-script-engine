package io.github.kasukusakura.jjse.test;

import io.github.kasukusakura.jjse.JavaJavaScriptEngineFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static io.github.kasukusakura.jjse.test.Testing.assertFailed;

public class TestJavaJavaScriptEngine {
    @Test
    void testScriptEngine() throws Throwable {
        var engineManager = new ScriptEngineManager();
        var engine = engineManager.getEngineByName("java");
        Assertions.assertTrue(engine.getFactory() instanceof JavaJavaScriptEngineFactory, () -> engine.getFactory().getClass().getName());

        engine.eval("System.out.println(\"OK\");");

        ScriptException se = assertFailed(ScriptException.class, () -> {
            engine.eval("throw new RuntimeException(\"Test\");");
        });
        Assertions.assertTrue(se.getCause() instanceof RuntimeException);
        Assertions.assertEquals("Test", se.getCause().getMessage());

        {
            var bindings = engine.createBindings();
            bindings.put("hasReturn", "true");
            Assertions.assertEquals(
                    System.class,
                    engine.eval("return System.class;", bindings)
            );
        }

        assertFailed(ScriptException.class, () -> {
            engine.eval("$$$ COMPILE  ERROR $$$");
        }).printStackTrace(System.out);

        engine.eval("""
                import java.util.Collection;
                /*!end-imports!*/
                System.out.println(Collection.class);
                """);
    }
}
