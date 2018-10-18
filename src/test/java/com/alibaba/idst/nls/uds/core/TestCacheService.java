package com.alibaba.idst.nls.uds.core;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.alibaba.idst.nls.uds.core.context.DialogCache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class TestCacheService {

    @Autowired
    private DialogCache dialogCache;

    @Test
    public void testStringCache() {
        String key = "";
        dialogCache.saveString(key, "hello");

        String value = dialogCache.readString(key);
        System.out.println(value);
    }

    @Test
    public void testQueueCache() {
        String key = "";

        dialogCache.pushToQueue(key, "abc");

        ConcurrentLinkedQueue<String> queue = dialogCache.getQueue(key);
        System.out.println(queue.size());
    }
}
