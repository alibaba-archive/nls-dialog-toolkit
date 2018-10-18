package com.alibaba.idst.nls.uds.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.alibaba.idst.nls.uds.core.nlu.NluService;
import com.alibaba.idst.nlu.request.v6.NluRequest;
import com.alibaba.idst.nlu.response.v6.NluResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class TestNluService {

    @Autowired
    private NluService nluService;

    @Test
    public void testInvoke() {
        String akId = "";
        String akSecret = "";

        Map<String, String> params = new HashMap<>();
        params.put("accessKeyId", akId);
        params.put("accessKeySecret", akSecret);

        NluRequest request = new NluRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setAppKey("");
        request.setQuery("我想听刘德华的歌");
        request.setService("");
        request.setVersion("6.0");
        request.setVLog(0);

        NluResponse response = nluService.invokeNlu(params, request);
        System.out.println(JSON.toJSONString(response));
    }

}
