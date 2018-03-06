package de.unibayreuth.bayceer.bayeos.gateway;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScriptEngineConfig {

	@Bean
	public ScriptEngine scriptEngine(){
		//TODO Use nashorn for java > 8
		return new ScriptEngineManager().getEngineByName("js");
	}
}
