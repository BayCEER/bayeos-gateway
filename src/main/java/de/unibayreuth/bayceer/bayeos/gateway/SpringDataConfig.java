package de.unibayreuth.bayceer.bayeos.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.dialect.springdata.SpringDataDialect;

@Configuration
public class SpringDataConfig {
	
	@Bean
    public SpringDataDialect springDataDialect() {
        return new SpringDataDialect();
    }

}
