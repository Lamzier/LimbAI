package com.lamzier.io;

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
        URL opencvLib = this.getClass().getResource("../../../library/opencv_java455.dll");
        if(opencvLib == null){
            LOGGER.log(Level.SEVERE , "支持库文件路径找不到!!!");
            return false;
        }
        if(SYSTEM_IS_WINDOWS){//是windows系统
            LOGGER.log(Level.INFO , "检测出WINDOWS系统！");
            try {
                System.load(opencvLib.getPath());//加载动态库
            }catch (Exception ignored){
                LOGGER.log(Level.SEVERE , "加载库加载失败！");
            }
        }else{
            LOGGER.log(Level.INFO , "检测出LINUX系统！");
        }
        return true;
    }

    /**
     * 关闭时
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
