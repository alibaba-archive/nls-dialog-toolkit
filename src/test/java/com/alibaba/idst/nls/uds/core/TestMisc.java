package com.alibaba.idst.nls.uds.core;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.idst.nlu.response.common.NluResultElement;

import org.junit.Test;

public class TestMisc {

    @Test
    public void testNluSelect() {
        NluResultElement e1 = new NluResultElement();
        e1.setScore(1.1);
        e1.setSource("jsgf");

        NluResultElement e2 = new NluResultElement();
        e2.setScore(1.0);
        e2.setSource("jsgf");

        NluResultElement e3 = new NluResultElement();
        e3.setScore(1.0);
        e3.setSource("model");

        List<NluResultElement> elementList = Arrays.asList(e1, e2, e3);

        List<NluResultElement> elements = elementList
            .stream()
            .filter(e -> e.getSource().equals("jsgf"))
            .sorted(Comparator.comparing(NluResultElement::getScore).reversed())
            .limit(1)
            .collect(Collectors.toList());

        System.out.println(JSON.toJSONString(elements));
    }

}
