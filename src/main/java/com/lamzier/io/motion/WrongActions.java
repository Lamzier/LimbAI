package com.lamzier.io.motion;

import java.util.ArrayList;
import java.util.List;

/**
 * 错误动作集合
 */
public final class WrongActions {//禁止被继承

    //错误动作集合
    private final List<WrongAction> content = new ArrayList<>();

    /**
     * 添加错误动作
     */
    public void add(WrongAction wrongAction){
        content.add(wrongAction);
    }

    /**
     * 删除错误动作
     * @param index 索引
     */
    public void remove(int index){
        content.remove(index);
    }

    /**
     * 获取所有错误动作
     */
    public List<WrongAction> getContent() {
        return content;
    }

    /**
     * 打印输出
     */
    @Override
    public String toString() {
        return "WrongActions{" +
                "content=" + content +
                '}';
    }
}
