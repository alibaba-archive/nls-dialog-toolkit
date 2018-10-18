package com.alibaba.idst.nls.uds.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = {"com.alibaba.idst.nls"})
@EnableCaching
public class TestApplication {
}
