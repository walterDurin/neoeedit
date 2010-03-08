package neoe.ne;

import java.util.ArrayList;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import sun.org.mozilla.javascript.internal.NativeArray;

public class JS {

    public static List<StringBuffer> run(List<StringBuffer> lines,
            String userScript) throws Exception {
        List<StringBuffer> res = new ArrayList<StringBuffer>();
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        engine.eval(userScript);
        Invocable jsInvoke = (Invocable) engine;

        int total = lines.size();
        for (int i = 0; i < total; i++) {
            Object o = jsInvoke.invokeFunction("run", new Object[] {
                    lines.get(i).toString(), i, total });
            if (o == null)
                continue;
            else if (o instanceof NativeArray) {
                NativeArray arr = (NativeArray) o;
                int len = (int) arr.getLength();
                for (int j = 0; j < len; j++) {
                    Object obj = arr.get(j, arr);
                    res.add(new StringBuffer(obj.toString()));
                }
            } else {
                res.add(new StringBuffer(o.toString()));
            }
        }
        if (res.size() == 0) {
            res.add(new StringBuffer("no result"));
        }

        return res;

    }
}
