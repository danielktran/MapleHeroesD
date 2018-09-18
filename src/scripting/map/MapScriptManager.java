package scripting.map;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import client.MapleClient;
import scripting.AbstractScriptManager;

public class MapScriptManager extends AbstractScriptManager {

	private static final MapScriptManager instance = new MapScriptManager();
	private final Map<String, MapScript> scripts = new HashMap<>();
	private final ScriptEngineFactory sef;
	
	private MapScriptManager() {
		final ScriptEngineManager sem = new ScriptEngineManager();
		sef = sem.getEngineByName("javascript").getFactory();
	}
	
	public static final MapScriptManager getInstance() {
		return instance;
	}
	
	public void getMapScript(final MapleClient c, final String mapScriptName, boolean firstUser) {
		if (scripts.containsKey(mapScriptName)) {
			scripts.get(mapScriptName).start(new MapEventManager(c));
			return;
		}
		
		final String mapActionType = firstUser ? "onFirstUserEnter" : "onUserEnter";
		final File scriptFile = new File("scripts/map/" + mapActionType + "/" + mapScriptName + ".js");
		
		if (!scriptFile.exists()) {
			return;
		}
		
		FileReader fr = null;
		final ScriptEngine engine = sef.getScriptEngine();
		try {
			fr = new FileReader(scriptFile);
			final CompiledScript compiled = ((Compilable) engine).compile(fr);
			compiled.eval();
		} catch (ScriptException | IOException e) {
			e.printStackTrace();
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		final MapScript mapScript = ((Invocable) engine).getInterface(MapScript.class);
		scripts.put(mapScriptName, mapScript);
		mapScript.start(new MapEventManager(c));
	}
	
	public void clearScripts() {
		scripts.clear();
	}
	
	public final boolean hasScript(final MapleClient c, final String mapScriptName, final boolean firstUser) {
		final String mapActionType = firstUser ? "onFirstUserEnter" : "onUserEnter";
		Invocable iv = getInvocable("map/" + mapActionType + "/" + mapScriptName + ".js", c);
		
		return iv != null;
	}
	
	public final void start(final MapleClient c, final String mapScriptName, final boolean firstUser) {
		final String mapActionType = firstUser ? "onFirstUserEnter" : "onUserEnter";
		
		System.out.println("Executing map/" + mapActionType + "/" + mapScriptName + ".js");
		
		Invocable iv = getInvocable("map/" + mapActionType + "/" + mapScriptName + ".js", c);
		
		if (iv == null) {
			return;
		}
		
		final ScriptEngine scriptEngine = (ScriptEngine) iv;
		final MapEventManager em = new MapEventManager(c);
		
		scriptEngine.put("em", em);
		try {
			iv.invokeFunction("start");
		} catch (NoSuchMethodException | ScriptException e) {
			System.err.println("[Error] Error executing Map Script: " + mapActionType + "/" + mapScriptName);
			e.printStackTrace();
		}
	}
	
}
