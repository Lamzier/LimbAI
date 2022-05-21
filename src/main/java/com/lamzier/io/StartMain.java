package com.lamzier.io;

import com.lamzier.io.detection.ModelFinal;
import com.lamzier.io.detection.Person;
import com.sun.jna.Library;
import com.sun.jna.Native;
import org.opencv.core.Core;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tomcat启动类
 */
public class StartMain implements ServletContextListener {

    private final Logger LOGGER = Logger.getLogger(StartMain.class.getName());
    private boolean SYSTEM_IS_WINDOWS;
    public final static String PORJECT_NAME = "LimbAI";//项目名称
    public final static String PORJECT_PATH = PORJECT_NAME + "/";//项目地址


    /**
     * 启动时
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        SYSTEM_IS_WINDOWS = isWindows();//获取系统信息
        LOGGER.log(Level.INFO , "正在启动!!!");
        if(!init()){
            LOGGER.log(Level.SEVERE , "启动失败!!!");
            return;
        }
        LOGGER.log(Level.INFO , "启动成功!!!");
        //加载动态库
    }

    /**
     * 判断是否windows系统
     */
    private static boolean isWindows(){
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 初始化
     * @return 是否成功
     */
    private boolean init(){
        String path = System.getProperty("java.library.path");
        LOGGER.log(Level.INFO , "支持库路径有：" + path);
        if(SYSTEM_IS_WINDOWS){//是windows系统
            LOGGER.log(Level.INFO , "检测出WINDOWS系统！");
            URL opencvLib = this.getClass().getResource("../../../library/opencv_java455.dll");
            if(opencvLib == null){
                LOGGER.log(Level.SEVERE , "支持库文件路径找不到!!!");
                return false;
            }
            try {
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);//加载动态库 opencv_java455
//                System.load(opencvLib.getPath());//加载动态库
            }catch (Exception e){
                LOGGER.log(Level.SEVERE , "加载库加载失败！" + e);
            }
        }else{
            LOGGER.log(Level.INFO , "检测出LINUX系统！");
            URL opencvLib = this.getClass().getResource("");
            if(opencvLib == null){
                LOGGER.log(Level.SEVERE , "支持库文件路径找不到!!!");
                return false;
            }
            try{
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);//加载动态库 opencv_java455
            }catch (Exception e){
                LOGGER.log(Level.SEVERE , "加载库加载失败！" + e);
            }
        }
        LOGGER.log(Level.INFO , "动态库加载完成！");
        return true;
    }

    /**
     * 关闭时
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
