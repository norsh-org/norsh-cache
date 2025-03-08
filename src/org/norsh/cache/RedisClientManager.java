//package org.norsh.cache;
//
//import org.norsh.config.RedisConfig;
//import org.norsh.exceptions.InternalException;
//import org.norsh.util.Log;
//
//import io.lettuce.core.RedisClient;
//import io.lettuce.core.RedisURI;
//import io.lettuce.core.api.StatefulRedisConnection;
//import io.lettuce.core.api.sync.RedisCommands;
//
///**
// * High-performance Redis client for Norsh, using Lettuce without Spring.
// *
// * Features:
// * - Supports both Standalone and Cluster Redis configurations.
// * - Uses connection pooling for efficient resource management.
// * - Provides atomic and high-speed Redis operations.
// *
// * @since 1.0.0
// * @version 1.0.1
// * @author Danthur Lice
// */
//public class RedisClientManager {
//    private static RedisClient redisClient;
//    private static StatefulRedisConnection<String, String> connection;
//    private static RedisCommands<String, String> syncCommands;
//    private static final Object LOCK = new Object();
//    private static Log log;
//
//    /**
//     * Initializes Redis connection.
//     * Uses Lettuce for high-performance asynchronous Redis access.
//     *
//     * @param redisConfig Redis configuration parameters.
//     * @param log Logger instance.
//     */
//    public static void init(RedisConfig redisConfig, Log log) {
//        RedisClientManager.log = log;
//
//        if (redisClient != null) {
//            log.warning("RedisClient already initialized.");
//            return;
//        }
//
//        if (redisConfig == null) {
//            throw new InternalException("Redis configuration not found.");
//        }
//
//        synchronized (LOCK) {
//            RedisURI redisURI = RedisURI.builder()
//                .withHost(redisConfig.getHost())
//                .withPort(redisConfig.getPort())
//                .withPassword(redisConfig.getPassword().toCharArray())
//                .build();
//
//            redisClient = RedisClient.create(redisURI);
//            connection = redisClient.connect();
//            syncCommands = connection.sync();
//
//            log.info("Connected to Redis: " + redisConfig.getHost() + ":" + redisConfig.getPort());
//        }
//    }
//
//    /**
//     * Returns the synchronous Redis commands interface.
//     *
//     * @return RedisCommands for direct Redis operations.
//     */
//    public static RedisCommands<String, String> getCommands() {
//        if (syncCommands == null) {
//            throw new InternalException("RedisClient is not initialized.");
//        }
//        return syncCommands;
//    }
//
//    /**
//     * Closes the Redis connection safely.
//     */
//    public static void close() {
//        synchronized (LOCK) {
//            if (connection != null) {
//                connection.close();
//                connection = null;
//            }
//            if (redisClient != null) {
//                redisClient.shutdown();
//                redisClient = null;
//            }
//            syncCommands = null;
//            log.info("Redis connection closed.");
//        }
//    }
//
//    /**
//     * Sets a key-value pair in Redis.
//     *
//     * @param key   Redis key.
//     * @param value Redis value.
//     */
//    public static void set(String key, String value) {
//        getCommands().set(key, value);
//    }
//
//    /**
//     * Gets a value from Redis by key.
//     *
//     * @param key Redis key.
//     * @return The value or null if not found.
//     */
//    public static String get(String key) {
//        return getCommands().get(key);
//    }
//
//    /**
//     * Deletes a key from Redis.
//     *
//     * @param key Redis key.
//     */
//    public static void delete(String key) {
//        getCommands().del(key);
//    }
//}
