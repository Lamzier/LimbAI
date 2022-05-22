package com.lamzier.io.request.post;

import com.alibaba.fastjson.JSONObject;
import com.lamzier.io.StartMain;
import com.lamzier.io.motion.Badminton;
import com.lamzier.io.motion.Default;
import com.lamzier.io.motion.Score;
import com.lamzier.io.queue.AnalysisTask;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
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

    static {
        SUFFIX.add("mp4");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter writer = resp.getWriter();//获取写出器
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        //解析器
        ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
        //解决上传为中文乱码问题
        servletFileUpload.setHeaderEncoding("utf-8");
        List<FileItem> fileItems;
        try {
            fileItems = servletFileUpload.parseRequest(req);
        } catch (Exception e) {//没有数据
            e.printStackTrace();
            resp.setContentType("application/json");//设置json返回数据
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code" , -1);
            jsonObject.put("msg" , "表单异常！");
            writer.println(jsonObject.toJSONString());
            return;
        }
        FileItem video = null;
        String href = null;//跳转网址
        String name = null;//用户昵称
        String email = null;//邮箱
        String key = null;//体验密匙
        String type_str = null;//动作分析类型
        for (FileItem item : fileItems){
            if (item.isFormField()){//表单数据
                String key_temp = item.getFieldName();
                String value = new String(item.getString()
                        .getBytes(StandardCharsets.ISO_8859_1),
                        StandardCharsets.UTF_8);//编码转换
                if(key_temp.equalsIgnoreCase("href")){
                    href = value;
                }else if (key_temp.equalsIgnoreCase("name")){
                    name = value;
                }else if (key_temp.equalsIgnoreCase("email")){
                    email = value;
                }else if (key_temp.equalsIgnoreCase("key")){
                    key = value;
                }else if (key_temp.equalsIgnoreCase("type")){
                    type_str = value;
                }
            }else{//非表单数据
                video = item;//表单数据
            }
        }
        //校验数据
        if (href == null || href.length() <= 0){//跳转地址异常
            href = "../index.html";
        }//校验表单数据
        if(video == null || name == null || email == null ||
                key == null || type_str == null){
            resp.sendRedirect(href + "?message=" +
                    URLEncoder.encode("表单不完整！" , "UTF-8"));//重定向
            return;
        }
        String email_check = "[a-zA-Z0-9_]+@[a-zA-Z0-9_]+(\\.[a-zA-Z0-9]+)+";
        if(name.length() <= 0 || key.length() <= 0 || !email.matches(email_check)){
            resp.sendRedirect(href + "?message=" +
                    URLEncoder.encode("表单数据异常！" , "UTF-8"));//重定向
            return;
        }
        int type;
        try {
            type = Integer.parseInt(type_str);
        }catch (Exception e){
            resp.sendRedirect(href + "?message=" +
                    URLEncoder.encode("type类型错误！" , "UTF-8"));//重定向
            return;
        }
        Score score = getScore(type);
        if (score == null){//没有该算法
            resp.sendRedirect(href + "?message=" +
                    URLEncoder.encode("没有此类算法！" , "UTF-8"));//重定向
            return;
        }
        //检查key
        if(!key.equals("123asd456qwe") && !key.equals("123456789asd..")){
            resp.sendRedirect(href + "?message=" +
                    URLEncoder.encode("密匙不正确！" , "UTF-8"));//重定向
            return;
        }
        String[] fileName = video.getName().split("\\.");//上传文件名
        if (fileName.length < 2 ||
                !SUFFIX.contains(fileName[fileName.length - 1].toLowerCase())){
            //视频名称异常
            LOGGER.log(Level.WARNING , "仅支持mp4格式视频！");
            resp.sendRedirect("../index.html");//重定向
            return;
        }
        if(video.getSize() > 10485760){//文件大小大于10mb
            resp.sendRedirect(href + "?message=" +
                    URLEncoder.encode("文件大小不能大于10mb！！" , "UTF-8"));//重定向
            return;
        }
        //数据校验完成，开始分析，保存到临时位置
        String file_name = getOnlyName() + ".mp4";
        File outFile = new File(StartMain.PORJECT_PATH + "temp", file_name);
        if(!outFile.getParentFile().exists()){//父目录不存在
            if(!outFile.getParentFile().mkdirs()){
                resp.sendRedirect(href + "?message=" +
                        URLEncoder.encode("目录创建失败！" , "UTF-8"));//重定向
                return;
            }
        }
        try {
            video.write(outFile);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(href + "?message=" +
                    URLEncoder.encode("文件写出异常！" , "UTF-8"));//重定向
            return;
        }//保存到了本地临时存储位置
        File analysisFile = new File(//生成路径
                StartMain.PORJECT_PATH + "Ayalysis" , file_name);
        if(!AnalysisTask.addTask(analysisFile , outFile, score , email , name)){//任务添加失败
            resp.sendRedirect(href + "?message=" +
                    URLEncoder.encode("当前队列已满！请稍后重试！" , "UTF-8"));//重定向
            return;
        }
        //任务队列添加成功
        resp.sendRedirect(href + "?message=" +
                URLEncoder.encode("操作成功！请注意邮件查看！可能需要等待3分钟！" , "UTF-8"));//重定向
    }



    /**
     * 获取评分算法
     * @param type 评分类型
     * @return 评分对象
     */
    private Score getScore(int type){
        Score score;
        switch (type){
            case 0:
                score = new Default();
                break;
            case 1:
                score = new Badminton();
                break;
            default:
                score = null;
                break;
        }
        return score;
    }



    //临时名字数量
    private static int tempNameCount = 0;

    /**
     * 获取唯一名字（用于临时取名），加锁
     */
    private synchronized static String getOnlyName(){
        return System.currentTimeMillis() + "+++" + tempNameCount++;
    }

    @Override
    public void destroy() {
//        super.destroy();
    }


}
