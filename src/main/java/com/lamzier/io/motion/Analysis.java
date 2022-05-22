package com.lamzier.io.motion;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Joints;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.translate.TranslateException;
import com.lamzier.io.detection.ModelFinal;
import com.lamzier.io.detection.Person;
import com.lamzier.io.detection.Person50;
import com.lamzier.io.utils.ImageUtils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import java.util.ArrayList;
import java.util.List;

/**
 * 模型分析类
 */
public class Analysis {

    private static final double CONFIDENCE_POINT = 0.45;//关键点置信度
    private static final double CONFIDENCE_PERSON = 0.1;//人置信度
    private static final int thickness = 50;//粗细比例 0 ~ 100

    private final List<Point> POINT_LIST = new ArrayList<>();//相对个体点，用于动作判断
    //注意动作识别的置信度低的点直接被填充为null
    private final List<Double> CONFIDENCE_LIST = new ArrayList<>();//置信度
    private final List<Point> POINT_FRAME_LIST = new ArrayList<>();//相对整体点，用于显示

    /**
     * 清理原来数据
     */
    private void clean(){
        POINT_LIST.clear();
        CONFIDENCE_LIST.clear();
        POINT_FRAME_LIST.clear();
    }

    /**
     * 画图函数
     * @param mat 视窗
     * @param videoCapture 视频数据信息
     */
    public void draw(Mat mat , VideoCapture videoCapture) throws TranslateException {
        clean();//清理掉以前数据
        Image image = ImageUtils.mat2Image(mat);//转图片
        Person person = ModelFinal.getPerson();//人模型
        Person50 person50 = ModelFinal.getPerson50();//关键点模型
        DetectedObjects detectedObjects = person.predict(image);//推理
        DetectedObjects.DetectedObject detectedObject  = detectedObjects.get("person");
        if(detectedObject == null ||
                detectedObject.getProbability() < CONFIDENCE_PERSON){//置信度不够
            return;//不修改
        }
        int frameWidth = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int frameHeight = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        //画框
        Rectangle rectangle = detectedObject.getBoundingBox().getBounds();//获取框框
        int person_x = (int) (rectangle.getX() * frameWidth);
        if (person_x < 0) person_x = 0;
        else if (person_x > frameWidth) person_x = frameWidth;//控制边界
        int person_y = (int) (rectangle.getY() * frameHeight);
        if (person_y < 0) person_y = 0;
        else if (person_y > frameHeight) person_y = frameHeight;//控制边界
        int person_width = (int) (rectangle.getWidth() * frameWidth);
        if (person_width < 0) person_width = 0;
        else if (person_width + person_x > frameWidth)
            person_width = frameWidth - person_x;//控制边界
        int person_height = (int) (rectangle.getHeight() * frameHeight);
        if (person_height < 0) person_height = 0;
        else if (person_height + person_y > frameHeight)
            person_height = frameHeight - person_y;//控制边界
        //完成边界控制
        int thickness = 8 * Analysis.thickness / 100;//粗细控制
        Rect rect = new Rect(person_x , person_y , person_width , person_height);//框边界
        Scalar person_rectangle = new Scalar(50,50,50);
        Imgproc.rectangle(mat , rect , person_rectangle , thickness);
        //完成画框 开始肢体识别
        Image person_image = image.getSubImage(
                person_x , person_y , person_width , person_height
        );//单独提取人物框
        //继续分析人体关键点
        Joints joints = person50.predict(person_image);
        List<Joints.Joint> jointList =  joints.getJoints();//人体关键点坐标
        int max_point = 17;//最多17个关键点
        //遍历
        for(Joints.Joint joint : jointList){//遍历左右坐标点
            POINT_FRAME_LIST.add(new Point(joint.getX() * person_width + person_x ,
                    joint.getY() * person_height + person_y));
            CONFIDENCE_LIST.add(joint.getConfidence());
            if(joint.getConfidence() < CONFIDENCE_POINT){//置信度不够
                POINT_LIST.add(null);
            }else {
                POINT_LIST.add(new Point(joint.getX(),
                        joint.getY()));
            }
        }
        int span = max_point - jointList.size();
        for (int i = 0 ; i < span ; i ++){//补全空点
            POINT_LIST.add(null);
            POINT_FRAME_LIST.add(new Point(0,0));
            CONFIDENCE_LIST.add(0.0);
        }//完成补全数据
        //左右眼线
        Scalar scalar_24_13 = new Scalar(100,100,100);
        //左眼线
        if (CONFIDENCE_LIST.get(2) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(4) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(2),
                    POINT_FRAME_LIST.get(4),scalar_24_13,thickness);
        //右眼线
        if (CONFIDENCE_LIST.get(1) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(3) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(1),
                    POINT_FRAME_LIST.get(3),scalar_24_13,thickness);
        //左右鼻眼
        Scalar scalar_01_02 = new Scalar(100,150,100);
        //左鼻眼
        if (CONFIDENCE_LIST.get(0) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(1) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(0),
                    POINT_FRAME_LIST.get(1),scalar_01_02,thickness);
        //右鼻眼
        if (CONFIDENCE_LIST.get(0) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(2) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(0),
                    POINT_FRAME_LIST.get(2),scalar_01_02,thickness);
        //左右鼻肩
        Scalar scalar_05_06 = new Scalar(100,200,100);
        //左鼻肩
        if (CONFIDENCE_LIST.get(0) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(5) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(0),
                    POINT_FRAME_LIST.get(5),scalar_05_06,thickness);
        //右鼻肩
        if (CONFIDENCE_LIST.get(0) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(6) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(0),
                    POINT_FRAME_LIST.get(6),scalar_05_06,thickness);
        //左右肩肘
        Scalar scalar_57_68 = new Scalar(100,200,150);
        //左肩肘
        if (CONFIDENCE_LIST.get(5) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(7) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(5),
                    POINT_FRAME_LIST.get(7),scalar_57_68,thickness);
        //右肩肘
        if (CONFIDENCE_LIST.get(6) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(8) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(6),
                    POINT_FRAME_LIST.get(8),scalar_57_68,thickness);
        //左右手肘
        Scalar scalar_79_810 = new Scalar(100,200,200);
        //左手肘
        if (CONFIDENCE_LIST.get(7) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(9) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(7),
                    POINT_FRAME_LIST.get(9),scalar_79_810,thickness);
        //右手肘
        if (CONFIDENCE_LIST.get(8) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(10) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(8),
                    POINT_FRAME_LIST.get(10),scalar_79_810,thickness);
        //左右肩跨
        Scalar scalar_511_612 = new Scalar(150,100,200);
        //左肩跨
        if (CONFIDENCE_LIST.get(5) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(11) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(5),
                    POINT_FRAME_LIST.get(11),scalar_511_612,thickness);
        //右肩跨
        if (CONFIDENCE_LIST.get(6) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(12) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(6),
                    POINT_FRAME_LIST.get(12),scalar_511_612,thickness);
        //双肩
        Scalar scalar_56 = new Scalar(50,50,50);
        if (CONFIDENCE_LIST.get(5) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(6) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(5),
                    POINT_FRAME_LIST.get(6),scalar_56,thickness);
        //左右跨腿
        Scalar scalar_1113_1214 = new Scalar(200,100,200);
        //左跨腿
        if (CONFIDENCE_LIST.get(11) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(13) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(11),
                    POINT_FRAME_LIST.get(13),scalar_1113_1214,thickness);
        //右跨腿
        if (CONFIDENCE_LIST.get(12) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(14) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(12),
                    POINT_FRAME_LIST.get(14),scalar_1113_1214,thickness);
        //左右腿脚
        Scalar scalar_1315_1416 = new Scalar(200,150,200);
        //左腿脚
        if (CONFIDENCE_LIST.get(13) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(15) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(13),
                    POINT_FRAME_LIST.get(15),scalar_1315_1416,thickness);
        //右腿脚
        if (CONFIDENCE_LIST.get(14) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(16) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(14),
                    POINT_FRAME_LIST.get(16),scalar_1315_1416,thickness);
        //双跨
        Scalar scalar_1112 = new Scalar(200,200,200);
        if (CONFIDENCE_LIST.get(11) > CONFIDENCE_POINT &&
                CONFIDENCE_LIST.get(12) > CONFIDENCE_POINT)
            Imgproc.line(mat,POINT_FRAME_LIST.get(11),
                    POINT_FRAME_LIST.get(12),scalar_1112,thickness);
        //画点
        Scalar scalar_point = new Scalar(205 , 0 , 0);
        Scalar scalar_point_text = new Scalar(0 , 0 , 205);
        for (int i = 0 ; i < max_point ; i++){
            if(CONFIDENCE_LIST.get(i) < CONFIDENCE_POINT){//执信度太低
                continue;
            }
            Point o = POINT_FRAME_LIST.get(i);
            Imgproc.circle(mat , o ,
                    6 * Analysis.thickness / 100 , scalar_point ,
                    12 * Analysis.thickness / 100
            );
            int spanJ = 4 * Analysis.thickness / 100;
            Point point = new Point(o.x - spanJ , o.y + spanJ);
            Imgproc.putText(mat , String.valueOf(i) , point , 1 ,
                    Imgproc.FONT_HERSHEY_PLAIN , scalar_point_text ,
                    2 * Analysis.thickness / 100
            );
        }
    }

    /**
     * 开始评分算法
     * @param score 评分算法
     */
    public void score(Score score){
        score.run(POINT_LIST);
    }











}
