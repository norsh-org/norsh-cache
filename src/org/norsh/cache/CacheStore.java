package org.norsh.cache;

/**
 * Interface for cache operations with support for backoff waiting retrieval.
 * <p>
 * This interface defines standard methods for storing, retrieving, and managing 
 * key-value pairs in a cache system. It includes support for backoff waiting
 * when retrieving keys that may not be immediately available.
 * </p>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Supports storing and retrieving string-based and object-based values.</li>
 *   <li>Allows setting expiration times for cached entries.</li>
 *   <li>Includes methods for checking key existence and deleting keys.</li>
 *   <li>Supports backoff waiting for key retrieval.</li>
 * </ul>
 *
 * @since 1.0.0
 * @version 1.0.0
 * @author Danthur Lice
 */
public interface CacheStore {
    /**
     * Saves a string value in the cache with a specific expiration time.
     *
     * @param key   The key to store the value.
     * @param value The value to be stored.
     * @param ttl   Expiration time in seconds (0 for no expiration).
     */
    void save(String key, String value, long ttl);

    /**
     * Saves an object in the cache, converting it to JSON before storage.
     *
     * @param key    The key to store the object.
     * @param object The object to be stored.
     * @param ttl    Expiration time in seconds (0 for no expiration).
     */
    void save(String key, Object object, long ttl);

    /**
     * Retrieves a string value from the cache.
     *
     * @param key The key of the stored data.
     * @return The value associated with the key, or null if not found.
     */
    String get(String key);

    /**
     * Retrieves a string value from the cache, waiting if the key is not immediately available.
     * Uses a backoff strategy for efficient retrying.
     *
     * @param key     The key of the stored data.
     * @param timeout Maximum time in seconds to wait for the key to be available.
     * @return The value associated with the key, or null if not found within the timeout.
     */
    String get(String key, long timeout);

    /**
     * Retrieves an object from the cache, converting it from JSON.
     *
     * @param key   The key to retrieve.
     * @param clazz The class type to convert the JSON into.
     * @param <T>   The generic type of the returned object.
     * @return The deserialized object, or null if not found or conversion fails.
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * Retrieves an object from the cache, waiting if the key is not immediately available.
     * Uses a backoff strategy for efficient retrying.
     *
     * @param key     The key to retrieve.
     * @param clazz   The class type to convert the JSON into.
     * @param timeout Maximum time in seconds to wait for the key to be available.
     * @param <T>     The generic type of the returned object.
     * @return The deserialized object, or null if not found within the timeout.
     */
    <T> T get(String key, Class<T> clazz, long timeout);

    /**
     * Removes a value from the cache.
     *
     * @param key The key to be deleted.
     */
    void delete(String key);

    /**
     * Checks if a key exists in the cache.
     *
     * @param key The key to check.
     * @return true if the key exists, false otherwise.
     */
    boolean exists(String key);
}
