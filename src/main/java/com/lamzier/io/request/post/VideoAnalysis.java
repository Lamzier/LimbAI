package com.lamzier.io.request.post;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lamzier.io.StartMain;
import com.lamzier.io.detection.ModelFinal;
import com.lamzier.io.detection.Person;
import com.lamzier.io.detection.Person50;
import com.sun.jmx.snmp.agent.SnmpUserDataFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 视频分析请求包
 */
@WebServlet("/user/videoanalysis")
public class VideoAnalysis extends HttpServlet {

    //允许接受的后缀
    private static final HashSet<String> SUFFIX = new HashSet<>();
    private static final Logger LOGGER = Logger.getLogger(VideoAnalysis.class.getName());
    private File outFile;

    static {
        SUFFIX.add("mp4");
    }

    @Override
    public void init(){
//        super.init();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        //解析器
        ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
        //解决上传为中文乱码问题
        servletFileUpload.setHeaderEncoding("utf-8");
        List<FileItem> fileItems = null;
        try {
            fileItems = servletFileUpload.parseRequest(req);
        } catch (FileUploadException e) {//没有数据
            e.printStackTrace();
            LOGGER.log(Level.WARNING , "表单异常！");
            resp.sendRedirect("../index.html");//重定向
            return;
        }
        FileItem video = null;
        for (FileItem item : fileItems){
            if (item.isFormField()){//表单数据
//                String key = item.getFieldName();
//                String value = new String(item.getString()
//                        .getBytes(StandardCharsets.ISO_8859_1),
//                        StandardCharsets.UTF_8);//编码转换
                LOGGER.log(Level.INFO , "有表单数据！");
            }else{//非表单数据
                video = item;//表单数据
            }
        }
        if(video == null){
            LOGGER.log(Level.WARNING , "没有上传视频！");
            resp.sendRedirect("../index.html");//重定向
            return;
        }
        //校验数据
        String[] fileName = video.getName().split("\\.");//上传文件名
        if (fileName.length < 2 ||
                !SUFFIX.contains(fileName[fileName.length - 1].toLowerCase())){
            //视频名称异常
            LOGGER.log(Level.WARNING , "仅支持mp4格式视频！");
            resp.sendRedirect("../index.html");//重定向
            return;
        }
        if(video.getSize() > 10485760){//文件大小大于10mb
            LOGGER.log(Level.WARNING , "文件大小不能大于10mb！！");
            resp.sendRedirect("../index.html");//重定向
            return;
        }
        //数据校验完成，开始分析，保存到临时位置
        String name = getOnlyName() + ".mp4";
        outFile = new File(StartMain.PORJECT_PATH + "temp" , name);
        if(!outFile.getParentFile().exists()){//父目录不存在
            if(!outFile.getParentFile().mkdirs()){
                LOGGER.log(Level.WARNING , "目录创建失败！");
                resp.sendRedirect("../index.html");//重定向
                return;
            }
        }
        try {
            video.write(outFile);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.WARNING , "文件写出异常！");
            resp.sendRedirect("../index.html");//重定向
            return;
        }//保存到了本地临时存储位置
        //这里需要建立一个新的线程
        //开始读取分析
        if(!analysis()){
            LOGGER.log(Level.WARNING , "视频分析失败！");
            clean();
            resp.sendRedirect("../index.html");//重定向
            return;
        }



        clean();
        resp.sendRedirect("../index.html");//重定向
    }

    /**
     * 分析视频
     */
    private boolean analysis(){
        VideoCapture videoCapture = new VideoCapture(outFile.toString());
        if(!videoCapture.isOpened()){
            LOGGER.log(Level.WARNING , "视频打开失败！");
            return false;
        }
        int frameWidth = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int frameHeight = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        int frameCount = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);//总帧数
        Person person = ModelFinal.getPerson();//获取人模型
        Person50 person50 = ModelFinal.getPerson50();//获取关键点模型
        String name = getOnlyName() + ".mp4";
        File outAnalysisFile = new File(StartMain.PORJECT_PATH + "Ayalysis" , name);
        if(!outAnalysisFile.getParentFile().exists()){
            if(!outAnalysisFile.getParentFile().mkdirs()){
                LOGGER.log(Level.WARNING , "文件创建失败！");
                return false;
            }
        }
        //与桌面版不同，这里不需要展示GUI界面
        Mat mat = new Mat();






        return true;
    }

    /**
     * 清理产生的临时文件
     */
    private void clean(){
        if(!outFile.delete()){
            LOGGER.log(Level.WARNING , "临时文件清理异常！");
        }
    }

    //临时名字数量
    private static int tempNameCount = 0;

    /**
     * 获取唯一名字（用于临时取名），加锁
     */
    private synchronized static String getOnlyName(){
        return String.valueOf(System.currentTimeMillis()) + "+++" + tempNameCount++;
    }

    @Override
    public void destroy() {
//        super.destroy();
    }


}
