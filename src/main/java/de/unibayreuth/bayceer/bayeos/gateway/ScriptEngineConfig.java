package de.unibayreuth.bayceer.bayeos.gateway;

import javax.script.ScriptEngine;

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScriptEngineConfig {

	@Bean
	public ScriptEngine scriptEngine(){
		
	    NashornScriptEngineFactory factory = new NashornScriptEngineFactory();	    
        return factory.getScriptEngine();

	}
}
