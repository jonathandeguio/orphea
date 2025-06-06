export interface CacheConfig {
  config: string;
  cache: boolean;
  cacheExpiration: number;
  useRedis: boolean;
  redisUrl: string;
}
