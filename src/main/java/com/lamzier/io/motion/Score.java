package com.lamzier.io.motion;

import org.opencv.core.Point;

import java.util.List;

/**
 * 评分类
 */
public abstract class Score {

    WrongActions wrongActions;//错误集合

    abstract public void run(List<Point> pointList);

    abstract public String getName();



}
