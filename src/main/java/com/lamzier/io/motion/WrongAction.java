package com.lamzier.io.motion;

/**
 * 动作动作类
 */
public class WrongAction {

    private WrongActionLevel level;//动作错误等级
    private int rate;//错误动作帧
    private double progress;//错误动作进度
    private long time;//错误动作时间，毫秒
    private String msg;//错误动作提示

    public WrongAction(WrongActionLevel level ,String msg, int rate , double progress ,long time){
        this.level = level;
        this.rate = rate;
        this.progress = progress;
        this.time = time;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public WrongActionLevel getLevel() {
        return level;
    }

    public double getProgress() {
        return progress;
    }

    public int getRate() {
        return rate;
    }

    public long getTime() {
        return time;
    }
}
