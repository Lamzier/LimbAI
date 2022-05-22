package com.lamzier.io.queue;

import java.util.ArrayList;
import java.util.List;

/**
 * 分析队列
 */
public class AnalysisQueue {

    private static Integer taskingNum = 0;//执行中的任务数量,加锁
    private final static int TASK_MAX_NUM = 2;//允许的最大同时执行数
    public final static int WAIT_LIST_MAX = 10;//允许最大的等待数量
    private  final static List<AnalysisTask> WAIT_LIST = new ArrayList<>();//等待列表,加锁

    /**
     * 添加等待任务 对象锁
     * @return 是否获取成功 ， 失败则等待列表已满
     */
    public static boolean addWaitTast(AnalysisTask analysisTask){
        if (WAIT_LIST.size() >= WAIT_LIST_MAX) return false;
        synchronized (WAIT_LIST){
            WAIT_LIST.add(analysisTask);
        }
        return true;
    }

    /**
     * 执行一次任务（判断条件）
     */
    public static void dida(){
        AnalysisTask analysisTask = didaCheck();//检查一次
        if (analysisTask == null){//为空
            return;
        }
        try {
            analysisTask.run();//执行内容
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            finishTask();
            dida();//不要锁dida
        }
    }

    /**
     * 完成任务时执行 加锁
     */
    private synchronized static void finishTask(){
        taskingNum -= 1;
        System.out.println(taskingNum);
        if (taskingNum < 0) taskingNum = 0;//越界处理
        //dida一次
    }

    /**
     * dida检查
     * @return 执行任务，null则没有
     */
    private synchronized static AnalysisTask didaCheck(){
        if (taskingNum >= TASK_MAX_NUM){//达到上限
            return null;//直接返回
        }
        //没有达到上限
        if (WAIT_LIST.size() <= 0){//等待列表没有东西
            return null;//直接返回
        }
        AnalysisTask analysisTask = WAIT_LIST.get(0);//取一个元素
        //并且删掉
        WAIT_LIST.remove(0);//删除掉这个元素
        taskingNum += 1;//执行程序加一
        return analysisTask;//返回执行任务
    }

    /**
     * 获取等待列表，不加锁
     */
    public static int getWaitListSize(){
        return WAIT_LIST.size() + taskingNum;
    }


}
