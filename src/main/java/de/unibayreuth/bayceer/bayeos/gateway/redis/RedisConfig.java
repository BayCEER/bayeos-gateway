package de.unibayreuth.bayceer.bayeos.gateway.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


@Configuration
public class RedisConfig {
	
	@Value("${REDIS_HOST:localhost}")
	private String redis_host;

	@Value("${REDIS_PORT:6379}")
	private Integer redis_port;
	
	@Bean	
	public JedisPool jedisPool() {		
		return new JedisPool(new JedisPoolConfig(),redis_host, redis_port);
	}
	
	
}
