package omst;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Ovst {
    public static void main(String[] args) throws Throwable {

        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("java");//new JavaJavaScriptEngineFactory().getScriptEngine();
        Bindings bindings = scriptEngine.createBindings();
        String code = """
                import java.util.Collection;
                /*!end-imports!*/
                System.out.println("HelloWorld!!");
                new Exception().printStackTrace(System.out);
                System.out.println(getClass());
                System.out.println(Collection.class);
                return "Hello";
                """;
        bindings.put(ScriptEngine.FILENAME, "CustomClassName");
        bindings.put("hasReturn", "true");
        Object resp = scriptEngine.eval(code, bindings);
        System.out.println(resp);
    }
}
