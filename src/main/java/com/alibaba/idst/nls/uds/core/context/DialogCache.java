package com.alibaba.idst.nls.uds.core.context;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class DialogCache {

    private static final String STRING_CACHE_NAME = "stringcache";
    private static final String QUEUE_CACHE_NAME = "queuecache";

    private PersistentCacheManager getPersistentCacheManager() {
        ResourcePoolsBuilder resourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .heap(10, EntryUnit.ENTRIES)
            .disk(20, MemoryUnit.MB, true);

        PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .with(CacheManagerBuilder.persistence(new File(getStoragePath(), "cacheData")))
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

        return persistentCacheManager;
    }

    public String readString(String key) {
        log.info("read string, key:{}", key);
        PersistentCacheManager persistentCacheManager = getPersistentCacheManager();
        Cache<String, String> stringCache = persistentCacheManager.getCache(STRING_CACHE_NAME, String.class, String.class);
        String val = stringCache.get(key);
        persistentCacheManager.close();
        return val;
    }

    public void saveString(String key, String value) {
        log.info("save string, key:{} value:{}", key, value);
        PersistentCacheManager persistentCacheManager = getPersistentCacheManager();
        Cache<String, String> stringCache = persistentCacheManager.getCache(STRING_CACHE_NAME, String.class, String.class);
        stringCache.put(key, value);
        persistentCacheManager.close();
    }

    public void removeCache(String key) {
        log.info("remove cache, key:{}", key);
        PersistentCacheManager persistentCacheManager = getPersistentCacheManager();
        Cache<String, String> stringCache = persistentCacheManager.getCache(STRING_CACHE_NAME, String.class, String.class);
        Cache<String, ConcurrentLinkedQueue> queueCache = persistentCacheManager.getCache(QUEUE_CACHE_NAME, String.class, ConcurrentLinkedQueue.class);

        if(stringCache.containsKey(key)) {
            stringCache.remove(key);
        }
        if(queueCache.containsKey(key)) {
            queueCache.remove(key);
        }

        persistentCacheManager.close();
    }

    public int getQueueLen(String key) {
        log.info("get queue length, key:{}", key);
        PersistentCacheManager persistentCacheManager = getPersistentCacheManager();
        Cache<String, ConcurrentLinkedQueue> queueCache = persistentCacheManager.getCache(QUEUE_CACHE_NAME, String.class, ConcurrentLinkedQueue.class);
        ConcurrentLinkedQueue<String> queue = queueCache.get(key);
        int len = queue == null ? 0 : queue.size();
        persistentCacheManager.close();
        return len;
    }

    public ConcurrentLinkedQueue<String> getQueue(String key) {
        log.info("get queue, key:{}", key);
        PersistentCacheManager persistentCacheManager = getPersistentCacheManager();
        Cache<String, ConcurrentLinkedQueue> queueCache = persistentCacheManager.getCache(QUEUE_CACHE_NAME, String.class, ConcurrentLinkedQueue.class);
        ConcurrentLinkedQueue<String> queue = queueCache.get(key);
        persistentCacheManager.close();
        return queue;
    }

    public void popFromQueue(String key) {
        log.info("pop from queue, key:{}", key);
        PersistentCacheManager persistentCacheManager = getPersistentCacheManager();
        Cache<String, ConcurrentLinkedQueue> queueCache = persistentCacheManager.getCache(QUEUE_CACHE_NAME, String.class, ConcurrentLinkedQueue.class);
        ConcurrentLinkedQueue<String> queue = queueCache.get(key);
        if(queue != null && queue.size() > 0) {
            queue.poll();
            queueCache.put(key, queue);
        }
        persistentCacheManager.close();
    }

    public void pushToQueue(String key, String value) {
        log.info("push to queue, key:{} value:{}", key, value);
        PersistentCacheManager persistentCacheManager = getPersistentCacheManager();
        Cache<String, ConcurrentLinkedQueue> queueCache = persistentCacheManager.getCache(QUEUE_CACHE_NAME, String.class, ConcurrentLinkedQueue.class);
        ConcurrentLinkedQueue<String> queue = queueCache.get(key);
        if(queue == null) {
            queue = new ConcurrentLinkedQueue<>();
        }
        queue.offer(value);
        queueCache.put(key, queue);
        persistentCacheManager.close();
    }

    public String getQueueByIndex(String key, int index) {
        log.info("get queue by index, key:{} index:{}", key, index);
        PersistentCacheManager persistentCacheManager = getPersistentCacheManager();
        Cache<String, ConcurrentLinkedQueue> queueCache = persistentCacheManager.getCache(QUEUE_CACHE_NAME, String.class, ConcurrentLinkedQueue.class);
        ConcurrentLinkedQueue<String> queue = queueCache.get(key);
        if(queue == null) {
            return null;
        }

        int count = 0;
        Iterator<String> iter = queue.iterator();
        while(iter.hasNext()) {
            count++;
            if(count == index) {
                return iter.next();
            }
        }
        return null;
    }

    private String getStoragePath() {
        return System.getProperties().getProperty("user.home");
    }
}
