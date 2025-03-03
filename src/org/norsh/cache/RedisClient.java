package org.norsh.cache;

import org.norsh.config.RedisConfig;
import org.norsh.exceptions.InternalException;
import org.norsh.util.Log;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Manages Redis connections and configurations.
 * <p>
 * This class provides methods to configure and obtain Redis connections using {@link LettuceConnectionFactory},
 * ensuring proper integration with Norsh services.
 * </p>
 *
 * <h2>Features:</h2>
 * <ul>
 * <li>Loads Redis connection properties dynamically from a configuration object.</li>
 * <li>Provides a pre-configured {@link RedisTemplate} with String serializers.</li>
 * <li>Ensures secure authentication for Redis connections (if required).</li>
 * </ul>
 *
 * @since 1.0.0
 * @version 1.0.0
 * @author Danthur Lice
 */
public class RedisClient {
	private static RedisConnectionFactory connectionFactory;
	private Log log;

	/**
	 * Initializes the Redis connection factory using configuration values.
	 *
	 * @param config The application configuration containing Redis settings.
	 * @return A singleton {@link RedisConnectionFactory} instance.
	 */
	public RedisConnectionFactory redisConnectionFactory(RedisConfig redisConfig, Log log) {
		this.log = log;
		
		log.info("Redis Cluster");
		log.info("Redis Cluster");
		log.info("Redis Cluster");		
		
		if (connectionFactory == null) {
			if (redisConfig == null) {
				throw new InternalException("Redis configuration not found.");
			}

			connectionFactory = createConnectionFactory(redisConfig.getHost(), redisConfig.getPort(), redisConfig.getUsername(), redisConfig.getPassword(), redisConfig.getCluster());
		}
		return connectionFactory;
	}

	/**
	 * Creates a Redis connection factory with the given parameters.
	 *
	 * @param host     The Redis host.
	 * @param port     The Redis port.
	 * @param username The Redis username.
	 * @param password The Redis password.
	 * @return A configured {@link RedisConnectionFactory}.
	 */
	private RedisConnectionFactory createConnectionFactory(String host, Integer port, String username, String password, Boolean cluster) {
		log.info("Redis Cluster: " + cluster);
		log.info("Redis Cluster: " + cluster);
		log.info("Redis Cluster: " + cluster);
		
		if (cluster != null && cluster) {
			RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration();
			clusterConfig.clusterNode(host, port);
			clusterConfig.setUsername(username);
			clusterConfig.setPassword(password);

			return new LettuceConnectionFactory(clusterConfig);
		} else {
			RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(host, port);
			standaloneConfig.setUsername(username);
			standaloneConfig.setPassword(password);
			
			return new LettuceConnectionFactory(standaloneConfig);
		}
	}

	/**
	 * Creates a Redis template configured with String serializers.
	 *
	 * @param connectionFactory The Redis connection factory to use.
	 * @return A configured {@link RedisTemplate}.
	 */
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// Use String as serializer to avoid compatibility issues
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());

		return template;
	}
}
