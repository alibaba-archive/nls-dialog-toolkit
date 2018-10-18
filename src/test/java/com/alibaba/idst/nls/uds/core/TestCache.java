package com.alibaba.idst.nls.uds.core;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.junit.Test;

public class TestCache {

    private static final String STRING_CACHE_NAME = "stringCache";
    private static final String QUEUE_CACHE_NAME = "queueCache";

    @Test
    public void testSave() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        Cache<Long, String> myCache = cacheManager.createCache("myCache", CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Long.class, String.class, ResourcePoolsBuilder.heap(10)
        ));
        myCache.put(1L, "Hello World");
        myCache.put(1L, "haha");
        String value = myCache.get(1L);
        System.out.println(value);
        cacheManager.close();
    }

    @Test
    public void testPersistence() {
        ResourcePoolsBuilder resourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .heap(10, EntryUnit.ENTRIES)
            .offheap(1, MemoryUnit.MB)
            .disk(20, MemoryUnit.MB, true);

        PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .with(CacheManagerBuilder.persistence(new File(getStoragePath(), "myData")))
            .withCache("myCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                    resourcePoolsBuilder
                ).withExpiry(Expirations.timeToLiveExpiration(Duration.of(10, TimeUnit.MINUTES)))
            ).build(true);

        Cache<Long, String> myCache = persistentCacheManager.getCache("myCache", Long.class, String.class);
        //myCache.put(1L, "stillAvailableAfterRestart");

        String val = myCache.get(1L);
        System.out.println(val);

        persistentCacheManager.close();
    }

    @Test
    public void testPersistence2() {
        ResourcePoolsBuilder resourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .heap(10, EntryUnit.ENTRIES)
            .offheap(1, MemoryUnit.MB)
            .disk(20, MemoryUnit.MB, true);

        PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .with(CacheManagerBuilder.persistence(new File(getStoragePath(), "myData2")))
            .withCache(STRING_CACHE_NAME,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                    resourcePoolsBuilder
                ).withExpiry(Expirations.timeToLiveExpiration(Duration.of(10, TimeUnit.MINUTES)))
            )
            .withCache(QUEUE_CACHE_NAME,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, ConcurrentLinkedQueue.class,
                    resourcePoolsBuilder
                ).withExpiry(Expirations.timeToLiveExpiration(Duration.of(10, TimeUnit.MINUTES)))
            )
            .build(true);

        Cache<String, String> stringCache = persistentCacheManager.getCache(STRING_CACHE_NAME, String.class, String.class);
        Cache<String, ConcurrentLinkedQueue> queueCache = persistentCacheManager.getCache(QUEUE_CACHE_NAME, String.class, ConcurrentLinkedQueue.class);
        //stringCache.put("key1", "hahaha");

        String val = stringCache.get("key1");
        System.out.println(val);

        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        queue.offer("a");
        queue.offer("b");
        //queueCache.put("key2", queue);

        ConcurrentLinkedQueue<String> q = queueCache.get("key2");
        System.out.println(q.size());

        persistentCacheManager.close();
    }


    private String getStoragePath() {
        return "/Users/matrix/ehcache";
    }
}
