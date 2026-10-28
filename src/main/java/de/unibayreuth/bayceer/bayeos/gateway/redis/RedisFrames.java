package de.unibayreuth.bayceer.bayeos.gateway.redis;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisException;

@Component
public class RedisFrames {
        
    @Value("${REDIS_FRAMES_KEY:frames}")
    String key; 
    
    @Value("${REDIS_FRAMES:false}")
    private Boolean redis_frames;
        
    @Value("${REDIS_FRAMES_MAXLEN:10000}")
    private long maxLen;
   
    @Autowired
    JedisPool jedisPool;

    private Logger log = LoggerFactory.getLogger(RedisFrames.class);
    
    
    public void write(HttpServletRequest req, String sender, List<String> frames) {
        if (!redis_frames)
            return;
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ts", System.currentTimeMillis());
            m.put("authorization", req.getHeader("Authorization"));
            m.put("sender", sender);
            m.put("frames", frames);
            
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("payload", new ObjectMapper().writeValueAsString(m));
            
            if (maxLen > 0) {                
                jedis.xadd(key, StreamEntryID.NEW_ENTRY, fields, maxLen, true);
            } else {
                jedis.xadd(key, StreamEntryID.NEW_ENTRY, fields);
            }
        } catch (JedisException | JsonProcessingException e) {
            log.error("Failed to publish frames on redis stream", e);
        }
    }

    
}