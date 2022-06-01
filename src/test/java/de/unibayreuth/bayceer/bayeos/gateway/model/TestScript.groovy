package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import org.junit.Before
import org.junit.Test

class TestScript {

	ScriptEngine engine;
	
	@Before
	public void setUp(){
		engine = new ScriptEngineManager().getEngineByName("js")		
		// engine = new ScriptEngineManager().getEngineByName("nashorn")
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
	
	@Test
	public void testTaupunkt(){
		
		engine.put("T",20)
		engine.put("rf",80)
		
		
		def script = """
		  var K2 = 17.625; 
		  var K3 = 243.04;
		  var f = Math.log(rf/100);
		  K3 * (K2*T/(K3+T) + f)/(K2*K3/(K3+T) - f);		  	
		"""
		
	println("Tau:" + engine.eval(script))
		
	}
	
	@Test
	public void testLibrary(){
				
		// input
		// temperature [V], humidity [0-1]
		def inFrame = ["V":0.2,"H":0.9]
		
		// wanted output 
		// T[°C], rf[%], Tau[°C] 
		
		// function library 
		def lib =  [
			// volt to degree celsius
			"""function vtot(v) {
				var a = 12.0; 
				var b = 134.0; 
				return a + v*b;
			}""",
			// dewpoint temperature 
			"""function dewpoint(rf,t){
				var K2 = 17.625;
				var K3 = 243.04;
				var f = Math.log(rf/100);
				return K3 * (K2*t/(K3+t) + f)/(K2*K3/(K3+t) - f); 
			}"""						
		]
		
		// register functions 
		lib.each { 
			engine.eval(it)			
		}
				
		// execute frame function
		engine.put("V",0.2)
		engine.put("H",0.6)		
		def ret = engine.eval("""
				var ret = {};
				var t = vtot(V);
				ret = {t:t,rf:100*H,tau:dewpoint(100*H,t)};
		""")

		println("output frame:" + ret);

		
	}
	

}
