package com.red.verb.cache.config;

import com.alibaba.fastjson.parser.ParserConfig;
import com.red.verb.cache.serializer.FastJsonRedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;


/**
 * RedisCacheConfig
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/24     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-24 16:48
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {
	private RedisConnectionFactory redisConnectionFactory;

	@Autowired
	public void setRedisConnectionFactory(RedisConnectionFactory redisConnectionFactory) {
		this.redisConnectionFactory = redisConnectionFactory;
	}

	/**
	 * 覆盖默认的配置
	 *
	 * @return RedisTemplate
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
		FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);


		// 设置value的序列化规则和key的序列化规则
		template.setKeySerializer(stringRedisSerializer);
		template.setValueSerializer(fastJsonRedisSerializer);
		template.setHashKeySerializer(stringRedisSerializer);
		template.setHashValueSerializer(fastJsonRedisSerializer);
		template.setDefaultSerializer(fastJsonRedisSerializer);
		template.afterPropertiesSet();
		return template;
	}

	/**
	 * 解决注解方式存放到redis中的值是乱码的情况
	 *
	 * @param factory 连接工厂
	 * @return CacheManager
	 */
	@Bean
	public CacheManager cacheManager(RedisConnectionFactory factory) {
		RedisSerializer<String> redisSerializer = new StringRedisSerializer();
		FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);

		// 配置注解方式的序列化
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
		RedisCacheConfiguration redisCacheConfiguration =
				config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
						.serializeValuesWith(
								RedisSerializationContext.SerializationPair.fromSerializer(fastJsonRedisSerializer))
						//配置注解默认的过期时间
						.entryTtl(Duration.ofDays(1));
		// 加入白名单   https://github.com/alibaba/fastjson/wiki/enable_autotype
		ParserConfig.getGlobalInstance().addAccept("com.red");
		ParserConfig.getGlobalInstance().addAccept("com.baomidou");
		return RedisCacheManager.builder(factory).cacheDefaults(redisCacheConfiguration).build();
	}
}
