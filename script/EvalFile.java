import javax.script.*;

public class EvalFile {
	
	public static void main(String[] args) throws Exception {
		
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		
		String script = "function hello(name) {print('hello, ' +  name);}";
		engine.eval(script);
		//engine.eval(new java.io.FileReader(args[0]));
		Invocable inv = (Invocable)engine;
		
		inv.invokeFunction("hello", "Scripting");
	}
}