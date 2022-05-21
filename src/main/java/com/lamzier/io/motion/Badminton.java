package com.lamzier.io.motion;

import org.opencv.core.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * 羽毛球 继承评分接口
 */
public class Badminton extends Score{

    private int stage = 0;//当前进行阶段的意思
    private List<Point> pointList = new ArrayList<>();//人体点

    /**
     * 开始粉底
     * @param pointList 人体点坐标 , 最多17个
     */
    @Override
    public void run(List<Point> pointList) {
        this.pointList.clear();//清理原来的内容
        this.pointList.addAll(pointList);//添加全部不能影响原来的pointList
        switch (stage){
            case 0:
                stage_0();
                break;
            default:
                break;
        }




    }

    /**
     * 执行阶段 0
     * 判断是否是准备动作，如果是则进入下一个阶段，如果不是则不进入下一个阶段
     * 正确动作是：视线朝前上方（眼耳方向），躯干微微后仰（肩跨），低肩膀的收抬起（肘肩跨角度等于90度，
     * 手肘肩角度等于90度允许有15度误差），高肩膀的比较高（跨肩肘角度大于95度），手指向球（手肘肩角度
     * 大于100度）
     * 错误反馈动作：视线朝下方（眼耳方向），低肩手夹角小于90度（肘肩跨 ， 肩肘手），高肩手小于90度（
     * 手肘肩），躯干没有向低肩后仰
     * 阶段判断动作：判断高低肩，视线向高肩膀方向看，同手高肩手大于80度（肘肩跨 ， 手肘肩40度）。低肩手大
     * 于20度，肘大于30度
     */
    private void stage_0(){
        //判断高低肩
        if (!checkPoint(0,5,6,7,8,9,10,11,12)) return;//检查用到的点
        if (!checkPoint_NOT_ALL_NULL(1,2) || !checkPoint_NOT_ALL_NULL(3,4)) return;;
        //判断高低肩膀
        if (pointList.get(5).y > pointList.get(6).y) return;//高低肩不对
        if (pointList.get(4).x > pointList.get(1).x ||
                pointList.get(4).x > pointList.get(2).x
        ) return;//视线没有往右
        //计算高肩 肘肩跨角度
        double zhou_jian_kua_gao = getPointAngle(pointList.get(7) ,
                pointList.get(5) , pointList.get(11));
        if(zhou_jian_kua_gao < 80) return;//小于80度
        double shou_zhou_jian_gao = getPointAngle(pointList.get(9) ,
                pointList.get(7) , pointList.get(5));
        if(shou_zhou_jian_gao < 40) return;//小于40度
        double zhou_jian_kua_di = getPointAngle(pointList.get(8) ,
                pointList.get(6) , pointList.get(12));
        if(zhou_jian_kua_di < 20) return;//小于20度
        double shou_zhou_jian_di = getPointAngle(pointList.get(10) ,
                pointList.get(8) , pointList.get(6));
        System.out.println(shou_zhou_jian_di);
        if (shou_zhou_jian_di < 30) return;//小于30度
        //如果全部通过就是当前阶段了,设置进入下一个阶段
        stage = 2;






//        //计算视线方向 , 取耳朵中点和眼睛中点
//        Point ears_center = getPointCenter(pointList.get(3) , pointList.get(4));
//        Point eyes_center = getPointCenter(pointList.get(1) , pointList.get(2));
//        double jiao = getRightLevelAngle(ears_center , eyes_center);
//        System.out.println(jiao);



    }

    /**
     * 判断3点角度
     * @param point1 边
     * @param point2 角
     * @param point3 边
     */
    private double getPointAngle(Point point1 , Point point2 , Point point3){
        //求出三条边长
        double len_12 = getPointLength(point1 , point2); // b
        double len_13 = getPointLength(point1 , point3); // c
        double len_23 = getPointLength(point2 , point3); // a
        double hu = Math.acos(
                (Math.pow(len_23 , 2) + Math.pow(len_12 , 2) - Math.pow(len_13 , 2)) /
                        (2 * len_23 * len_13)
        );//余弦公式
        return Math.toDegrees(hu);
    }

//    public static void main(String[] args) {
//        Point point1 = new Point(0,10);
//        Point point2 = new Point(10,10);
//        Point point3 = new Point(10,0);
//        double a = getPointAngle(point1,point2,point3);
//        System.out.println(a);
//    }

    /**
     * 求两点距离
     */
    private double getPointLength(Point point1, Point point2){
        return Math.sqrt(
                Math.pow(point1.x - point2.x , 2) +
                        Math.pow(point1.y - point2.y , 2)
        );
    }

    /**
     * 判断向右水平线的角度 , 上为正方向
     * @param point1 起始点
     * @param point2 终止点
     */
    private double getRightLevelAngle(Point point1 , Point point2){
        double hu = Math.atan((point1.y - point2.y) / (point2.x - point1.x));
        return Math.toDegrees(hu);
    }

    /**
     * 取点中点
     */
    private Point getPointCenter(Point point1 , Point point2){
        return new Point((point1.x + point2.x) / 2 ,
                (point1.y + point2.y) / 2);
    }

    /**
     * 检查点是否正确
     */
    private boolean checkPoint(int... point){
        for(int i : point){//遍历点
            if (pointList.get(i) == null)
                return false;
        }
        return true;
    }

    /**
     * 不允许同时NULL的点 , 并把有的点赋值给没有的点
     */
    private boolean checkPoint_NOT_ALL_NULL(int p1,int p2){
        Point point1 = pointList.get(p1);
        Point point2 = pointList.get(p2);
        if(point1 == null && point2 == null) return false;
        else if(point1 == null) pointList.set(p1 , point2);
        else if(point2 == null) pointList.set(p2 , point1);
        System.out.println("true");
        return true;
    }



}
