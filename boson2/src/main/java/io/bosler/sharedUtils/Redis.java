package io.bosler.sharedUtils;

import io.bosler.sharedUtils.Response.ResourceNotFoundException;
import redis.clients.jedis.Jedis;

import java.util.Set;

public class Redis {

    private static final String REDIS_URL = System.getenv("REDIS_URL");
    private static final boolean USE_REDIS = Boolean.parseBoolean(System.getenv("USE_REDIS"));

    private Redis() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static void setCache(String key, String value, Long expiration) {
        if (!USE_REDIS || REDIS_URL == null) {
            return;
        }

        try (Jedis jedis = new Jedis(REDIS_URL)) {
            if (expiration == null) {
                jedis.set(key, value);
            } else {
                jedis.setex(key, expiration, value);
            }
        } catch (Exception e) {
            System.out.println("Error deleting cache for key: " + key + " " + e);
        }
    }

    public static void deleteCache(String key) {
        if (!USE_REDIS || REDIS_URL == null) {
            return;
        }

        try (Jedis jedis = new Jedis(REDIS_URL)) {
            if (jedis.get(key) != null) {
                jedis.del(key);
            }
        } catch (Exception e) {
            System.out.println("Error deleting cache for key: " + key + " " + e);
        }
    }

    public static void deleteCacheWithWildCard(String key) {
        if (!USE_REDIS || REDIS_URL == null) {
            return;
        }

        try (Jedis jedis = new Jedis(REDIS_URL)) {

            // Get list of keys matching the pattern
            Set<String> keysToDelete = jedis.keys(key);

            // Delete each key in the list
            for (String key1 : keysToDelete) {
                jedis.del(key1);
            }
        } catch (Exception e) {
            System.out.println("Error deleting cache for key: " + key + " " + e);
        }
    }

    public static String getCache(String key) {
        if (!USE_REDIS || REDIS_URL == null) {
            return null;
        }

        try (Jedis jedis = new Jedis(REDIS_URL)) {
            String jsonResults = jedis.get(key);
            if (jsonResults != null) {
                return jsonResults;
            }
        } catch (Exception e) {
            System.out.println("Error deleting cache for key: " + key + " " + e);
        }

        return null;
    }
}
