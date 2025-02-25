package org.norsh.cache;

import java.util.concurrent.TimeUnit;

import org.norsh.util.Converter;
import org.norsh.util.Logger;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Generic Redis cache service with support for backoff waiting retrieval.
 * <p>
 * This class provides methods for interacting with Redis, including saving, retrieving, and deleting cached values with
 * optional expiration settings. It also supports backoff waiting when retrieving keys that may not be immediately
 * available.
 * </p>
 *
 * <h2>Features:</h2>
 * <ul>
 * <li>Allows setting cached values with and without expiration.</li>
 * <li>Provides safe key existence checks and deletion methods.</li>
 * <li>Supports backoff waiting for key retrieval.</li>
 * </ul>
 *
 * @since 1.0.0
 * @version 1.0.0
 * @author Danthur Lice
 */
public class RedisCache implements CacheStore {
	private final RedisTemplate<String, String> redisTemplate;
	private final Logger log;

	public RedisCache(RedisTemplate<String, String> redisTemplate, Logger log) {
		this.redisTemplate = redisTemplate;
		this.log = log;
	}
	
	public RedisTemplate<String, String> getTemplate() {
		return redisTemplate;
	}

	@Override
	public void save(String key, String value, long ttlMs) {
		if (ttlMs < 1000)
			ttlMs = 1000;

		try {
			redisTemplate.opsForValue().set(key, value);
			
			if (ttlMs > 0) {
				redisTemplate.expire(key, ttlMs, TimeUnit.MILLISECONDS);
			}
		} catch (Exception e) {
			log.error("Error saving key in Redis", e);
		}
	}

	@Override
	public void save(String key, Object object, long ttlMs) {
		if (object == null) {
			log.error("Cannot save null object in Redis", key);
			return;
		}

		String jsonValue = Converter.toJson(object);
		save(key, jsonValue, ttlMs);
	}

	@Override
	public String get(String key, long timeoutMs) {
		long startTime = System.currentTimeMillis();
		long waitTime = 100; // Initial wait time in milliseconds
		String value = null;

		do {
			try {
				value = redisTemplate.opsForValue().get(key);
			} catch (Exception e) {
				log.error("Error retrieving key from Redis", e);
				return null;
			}
			
			if (value == null) {
				try {
					Thread.sleep(waitTime);
					waitTime = Math.min(Double.valueOf(waitTime * 2).longValue(), timeoutMs/10); // Exponential backoff
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					log.error("Backoff wait interrupted", e);
					return null;
				}
			}

		} while (value == null && Math.abs(System.currentTimeMillis() - startTime) < timeoutMs);
		return value;
	}
	
	@Override
	public <T> T get(String key, Class<T> type, long timeoutMs) {
		String jsonValue = get(key, timeoutMs);

		if (jsonValue == null) {
			return null;
		}

		try {
			return Converter.fromJson(jsonValue, type);
		} catch (Exception e) {
			log.error("Error converting JSON to object from Redis", e);
			return null;
		}
	}
	
	@Override
	public String get(String key) {
		return get(key, 0);
	}

	@Override
	public <T> T get(String key, Class<T> type) {
		return get(key, type, 0);
	}

	@Override
	public void delete(String key) {
		try {
			redisTemplate.delete(key);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error deleting key from Redis", e);
		}
	}

	@Override
	public boolean exists(String key) {
		try {
			return Boolean.TRUE.equals(redisTemplate.hasKey(key));
		} catch (Exception e) {
			log.error("Error checking existence of key in Redis", e);
			return false;
		}
	}
}
