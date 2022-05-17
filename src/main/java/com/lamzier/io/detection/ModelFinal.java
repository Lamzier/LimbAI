package com.lamzier.io.detection;

/**
 * 模型常量类
 */
public class ModelFinal {

    private static final Person PERSON = new Person();//人物检测模型
    private static final Person50 PERSON50 = new Person50();//关键点检测模型

    /**
     * 获取人物检测模型
     */
    public static Person getPerson() {
        return PERSON;
    }

    /**
     * 获取关键点检测模型
     */
    public static Person50 getPerson50() {
        return PERSON50;
    }
}
