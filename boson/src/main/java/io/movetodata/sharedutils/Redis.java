package io.movetodata.sharedutils;

import io.movetodata.platform.library.models.CacheConfigModel;
import io.movetodata.platform.library.models.GitConfigModel;
import io.movetodata.platform.library.repository.CacheRepository;
import io.movetodata.platform.library.repository.PlatformConfigRepository;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Redis {

    private Redis() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static void setCache(String key, String value, CacheRepository cacheRepository) {
        CacheConfigModel cacheConfigModel = cacheRepository.findByConfig("platform");
        if (!cacheConfigModel.isCache())
            return;

        if (!cacheConfigModel.isUseRedis() || cacheConfigModel.getRedisUrl() == null) {
            return;
        }
        Long expiration = cacheConfigModel.getCacheExpiration();

//        System.out.println("USE_REDIS : " + USE_REDIS);
//        System.out.println("REDIS_URL : " + REDIS_URL);

        try (Jedis jedis = new Jedis(cacheConfigModel.getRedisUrl())) {
            if (expiration == null) {
                jedis.set(key, value);
//                System.out.println("Redis key :" + key);
//                System.out.println("Redis value :" + value);
            } else {
                jedis.setex(key, expiration, value);
//                System.out.println("Redis key :" + key);
//                System.out.println("Redis value :" + value);
//                System.out.println("Redis expiration :" + expiration);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Redis cache setting problem.");
        }
    }

    public static void deleteCache(String key, CacheRepository cacheRepository) {
        CacheConfigModel cacheConfigModel = cacheRepository.findByConfig("platform");
        if (!cacheConfigModel.isCache())
            return;

        if (!cacheConfigModel.isUseRedis() || cacheConfigModel.getRedisUrl() == null) {
            return;
        }

        try (Jedis jedis = new Jedis(cacheConfigModel.getRedisUrl())) {
            if (jedis.get(key) != null) {
                jedis.del(key);
            }
        } catch (Exception e) {

        }
    }

    public static void deleteCacheWithWildCard(String key, CacheRepository cacheRepository) {
        CacheConfigModel cacheConfigModel = cacheRepository.findByConfig("platform");
        if (!cacheConfigModel.isCache())
            return;

        if (!cacheConfigModel.isUseRedis() || cacheConfigModel.getRedisUrl() == null) {
            return;
        }

        try (Jedis jedis = new Jedis(cacheConfigModel.getRedisUrl())) {

            // Get list of keys matching the pattern
            Set<String> keysToDelete = jedis.keys(key);

            // Delete each key in the list
            for (String key1 : keysToDelete) {
                jedis.del(key1);
            }
        } catch (Exception e) {

        }
    }

    public static String getCache(String key, CacheRepository cacheRepository) {
        CacheConfigModel cacheConfigModel = cacheRepository.findByConfig("platform");
        if (!cacheConfigModel.isCache())
            return null;

        if (!cacheConfigModel.isUseRedis() || cacheConfigModel.getRedisUrl() == null) {
            return null;
        }

        try (Jedis jedis = new Jedis(cacheConfigModel.getRedisUrl())) {
            String jsonResults = jedis.get(key);
            if (jsonResults != null) {
                return jsonResults;
            }
        } catch (Exception e) {

        }

        return null;
    }

    public static void clearRelatedCaches(UUID datasetId, String branch, CacheRepository cacheRepository) {
        List<String> cacheKeys = List.of("dataset", "columns", "sparkResults", "chartData");
        cacheKeys.forEach(key -> deleteCacheWithWildCard(key + datasetId + branch + "*", cacheRepository));
    }
}
