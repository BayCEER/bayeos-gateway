package de.unibayreuth.bayceer.bayeos.gateway.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.Jedis;


@Configuration
public class JedisConfig {
	
	@Value("${REDIS_HOST:localhost}")
	private String redis_host;

	@Value("${REDIS_PORT:6379}")
	private Integer redis_port;
	
	
	
	
	
	@Bean
	public Jedis jedis() {		
		return new Jedis(redis_host, redis_port);
	}
	
	
}
