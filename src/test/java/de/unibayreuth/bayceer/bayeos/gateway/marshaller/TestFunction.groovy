package de.unibayreuth.bayceer.bayeos.gateway.marshaller;

import static org.junit.Assert.*

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

import org.junit.Before
import org.junit.Test

class TestFunction {

	ScriptEngine engine;
	
	@Before
	public void setUp(){
		engine = new ScriptEngineManager().getEngineByName("js")
	}
		
	
	@Test
	public void testSingleLine(){		
		engine.put("x",10)
		engine.put("y",20)	
		println(engine.eval("x * y"))

	}
	
	@Test
	public void testMultiLine(){
		engine.put("rf",100)
		engine.put("T",20)
		def script = """
			var H = (Math.log(rf)/Math.LN10-2.0)/0.4343+(17.62*T)/(243.12+T);
			243.12*H/(17.62-H);							
		"""
			
		println(engine.eval(script))
	}
	

}
