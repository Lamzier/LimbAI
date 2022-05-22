package com.lamzier.io.queue;

import ai.djl.translate.TranslateException;
import com.lamzier.io.motion.Analysis;
import com.lamzier.io.motion.Score;
import com.lamzier.io.utils.JakartaEmail;
import com.lamzier.io.utils.JakartaEmailThread;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 队列单个任务
 */
public class AnalysisTask {

    private final File analysisOutFile;//输出路径
    private final Score score;//评分算法
    private final File read;//读取路径
    private final String toEmail;//收件人
    private final String name;//用户名
    private final static Logger LOGGER = Logger.getLogger(AnalysisTask.class.getName());

    /**
     * 初始化
     * @param analysisOutFile 输出路径
     * @param read 读取路径
     * @param score 评分算法
     * @param toEmail 通知邮件收件人
     * @param name 用户名
     */
    private AnalysisTask(File analysisOutFile , File read , Score score , String toEmail , String name){
        this.analysisOutFile = analysisOutFile;
        this.read = read;
        this.score = score;
        this.toEmail = toEmail;
        this.name = name;
    }

    /**
     * 跑方法
     * @throws Exception 异常
     */
    public void run() throws Exception{
        Date date = new Date();//获取当前时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy 年 MM 月 dd 日 E HH 点 mm 分 ss 秒");
        long start = System.currentTimeMillis();//记录开始时间
        boolean success = analysis(analysisOutFile , score , read);
        long span = System.currentTimeMillis() - start;
        clean();//清理临时文件
        if(!success){//分析没成功
            //线程发送邮件
            JakartaEmail.send_email("肢识科技（LambAI）" ,
                    "尊敬的" + name + "：\n视频解析结果：视频分析失败！\n" +
                            "采用算法：" + score.getName() + "\n" +
                            "提交时间：" + simpleDateFormat.format(date) + "\n" +
                            "分析耗时：" + span + " 毫秒，即" +
                            (span / 1000) + " 秒" + "即" + (span / 1000 / 60) + " 分钟"
                    , toEmail);//发送邮件
            return;
        }
        //        分析成功
        JakartaEmailThread.send_email("肢识科技（LambAI）" ,
                "尊敬的" + name + "：\n视频解析结果：视频分析成功！\n" +
                        "采用算法：" + score.getName() + "\n" +
                        "提交时间：" + simpleDateFormat.format(date) + "\n" +
                        "分析耗时：" + span + "毫秒，即" +
                        (span / 1000) + "秒" + "，即" + (span / 1000 / 60) + "分钟"
                , toEmail,
                analysisOutFile.getPath(), "视频.mp4");//发送邮件






    }

    /**
     * 清理产生的临时文件
     */
    private void clean(){
        if(!read.delete()){
            LOGGER.log(Level.WARNING , "临时文件清理异常！");
        }
    }

    /**
     * 分析视频
     * @param analysisOutFile 分析结果保存路径
     * @param score 评分算法
     * @param read 视频读取路径
     * @return 成功与否
     */
    private static boolean analysis(File analysisOutFile , Score score , File read){
        VideoCapture videoCapture = new VideoCapture();
        videoCapture.open(read.getPath());
        if(!videoCapture.isOpened()){
            LOGGER.log(Level.WARNING , "视频打开失败！");
            return false;
        }
        int frameWidth = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int frameHeight = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        int frameCount = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);//总帧数
        if(!analysisOutFile.getParentFile().exists()){
            if(!analysisOutFile.getParentFile().mkdirs()){
                LOGGER.log(Level.WARNING , "文件创建失败！");
                return false;
            }
        }
        //与桌面版不同，这里不需要展示GUI界面
        Size size = new Size(frameWidth,frameHeight);
        Mat mat = new Mat();
        VideoWriter videoWriter = new VideoWriter(
                analysisOutFile.toString(),//生成路径
                VideoWriter.fourcc('M', 'P', '4', '2'),//生成品质
                30.0,//帧率
                size,//大小
                true//是否显示演示
        );
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        int speed_progress = 0;
        Analysis analysis = new Analysis();//动作分析类
        while(videoCapture.read(mat)){//读取每一帧 注意这里可以使用多线程完成
            Mat dst = new Mat();//快照
            Imgproc.resize(mat,dst,size);
            if(dst.empty()){
                LOGGER.log(Level.SEVERE , "视频分析异常！");
                return false;
            }
            try {
                analysis.draw(dst , videoCapture);
                //初始化，画线 ,此过程会对mat进行修改
                analysis.score(score);//开始评分算法
//                HighGui.imshow("测试" , dst);

            } catch (TranslateException e) {
                e.printStackTrace();
                LOGGER.log(Level.SEVERE , "推理过程异常！");
                return false;
            }
            //推理完毕
            videoWriter.write(dst);
            System.out.println(speed_progress + " / " + frameCount);//进度条
            speed_progress++;
//            HighGui.waitKey(5);
        }
        videoCapture.release();//释放资源
        videoWriter.release();//释放资源
        return true;
    }

    /**
     * 添加任务
     * @param analysisOutFile 输出路径
     * @param read 读取路径
     * @param score 评分算法
     * @param toEmail 通知邮件收件人
     * @param name 用户名
     * @return false 则等待列表满了
     */
    public static boolean addTask(File analysisOutFile , File read , Score score , String toEmail , String name){
        AnalysisTask analysisTask = new AnalysisTask(analysisOutFile , read , score , toEmail , name);
        if (!AnalysisQueue.addWaitTast(analysisTask)){//添加等待列表
            return false;
        }
        //下面多线程执行
        //dida一次
        Thread thread = new Thread(AnalysisQueue::dida);
        thread.start();//启动线程
        return true;
    }




}

