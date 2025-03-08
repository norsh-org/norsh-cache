package org.norsh.cache;

import java.util.concurrent.TimeUnit;

import org.norsh.config.RedisConfig;
import org.norsh.util.Converter;
import org.norsh.util.Log;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * High-performance Redis cache service using Lettuce (without Spring).
 *
 * Features:
 * - Supports TTL for cached keys.
 * - Uses connection pooling for optimal performance.
 * - Implements backoff waiting for missing keys without blocking.
 *
 * @since 1.0.1
 * @version 1.0.1
 * @author Danthur Lice
 */
public class RedisCache {
    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;
    private final Object LOCK = new Object();
    private Log log;

    /**
     * Initializes Redis connection.
     *
     * @param redisHost Redis server hostname.
     * @param redisPort Redis server port.
     * @param log Logger instance.
     */
    public RedisCache(RedisConfig redisConfig, Log log) {
        this.log = log;

        if (redisClient != null) {
            log.warning("RedisCache already initialized.");
            return;
        }

        synchronized (LOCK) {
            RedisURI redisURI = RedisURI.builder()
                    .withHost(redisConfig.getHost())
                    .withPort(redisConfig.getPort())
                    .build();

            redisClient = RedisClient.create(redisURI);
            connection = redisClient.connect();
            syncCommands = connection.sync();
        }
    }

    private RedisCommands<String, String> getCommands() {
        if (syncCommands == null) {
            throw new IllegalStateException("RedisCache is not initialized.");
        }
        return syncCommands;
    }

    /**
     * Saves a key-value pair in Redis with an optional TTL.
     *
     * @param key   Redis key.
     * @param value Redis value.
     * @param ttlMs Time-to-live in milliseconds (0 for no expiration).
     */
    
    public void save(String key, String value, long ttlMs) {
        if (ttlMs < 1000) {
            ttlMs = 1000;
        }

        try {
            getCommands().set(key, value);
            if (ttlMs > 0) {
                getCommands().expire(key, ttlMs / 1000);
            }
        } catch (Exception e) {
            log.error("Error saving key in Redis", e);
        }
    }

    public boolean saveIfAbsent(String key, String value, long ttlMs) {
        try {
            Boolean isSet;
            if (ttlMs > 0) {
                isSet = syncCommands.set(key, value, SetArgs.Builder.nx().ex(ttlMs / 1000)) != null;
            } else {
                isSet = syncCommands.setnx(key, value);
            }
            return isSet;
        } catch (Exception e) {
            log.error("Error executing setIfAbsent in Redis", e);
            return false;
        }
    }
    
    /**
     * Saves an object in Redis by serializing it to JSON.
     *
     * @param key    Redis key.
     * @param object The object to store.
     * @param ttlMs  Time-to-live in milliseconds.
     */
    
    public void save(String key, Object object, long ttlMs) {
        if (object == null) {
            log.error("Cannot save null object in Redis", key);
            return;
        }

        String jsonValue = Converter.toJson(object);
        save(key, jsonValue, ttlMs);
    }

    /**
     * Retrieves a value from Redis, waiting up to `timeoutMs` using controlled backoff.
     *
     * @param key       Redis key.
     * @param timeoutMs Maximum wait time in milliseconds.
     * @return The value, or null if not found.
     */
    
    public String get(String key, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        long waitTime = 50; // Initial wait time in milliseconds
        String value = null;

        while ((value = getCommands().get(key)) == null &&
                (System.currentTimeMillis() - startTime) < timeoutMs) {
            try {
                TimeUnit.MILLISECONDS.sleep(waitTime);
                waitTime = Math.min(waitTime * 2, 500); // Controlled exponential backoff (max 500ms)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Backoff wait interrupted", e);
                return null;
            }
        }

        return value;
    }

    /**
     * Retrieves an object from Redis by deserializing it from JSON.
     *
     * @param key       Redis key.
     * @param type      The object type.
     * @param timeoutMs Maximum wait time in milliseconds.
     * @return The object, or null if not found.
     */
    
    public <T> T get(String key, Class<T> type, long timeoutMs) {
        String jsonValue = get(key, timeoutMs);
        if (jsonValue == null) return null;

        try {
            return Converter.fromJson(jsonValue, type);
        } catch (Exception e) {
            log.error("Error converting JSON to object from Redis", e);
            return null;
        }
    }

    
    public String get(String key) {
        return get(key, 0);
    }

    
    public <T> T get(String key, Class<T> type) {
        return get(key, type, 0);
    }

    
    public void delete(String key) {
        try {
            getCommands().del(key);
        } catch (Exception e) {
            log.error("Error deleting key from Redis", e);
        }
    }

    
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(getCommands().exists(key) > 0);
        } catch (Exception e) {
            log.error("Error checking existence of key in Redis", e);
            return false;
        }
    }

    /**
     * Closes the Redis connection safely.
     */
    public void close() {
        synchronized (LOCK) {
            if (connection != null) {
                connection.close();
                connection = null;
            }
            if (redisClient != null) {
                redisClient.shutdown();
                redisClient = null;
            }
            syncCommands = null;
            log.info("Redis connection closed.");
        }
    }
}
