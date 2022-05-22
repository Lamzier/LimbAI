package com.lamzier.io.utils;

import javax.mail.MessagingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 邮件多线程类
 */
public class JakartaEmailThread {

    private static final Logger LOGGER = Logger.getLogger(JakartaEmailThread.class.getName());

    /**
     * 发送邮件 一对一 不含附件
     * @param title 标题
     * @param content 内容
     * @param to 收件人
     */
    public static void send_email(String title , String content , String to){
        new Thread(() -> {
            try {
                JakartaEmail.send_email(title , content , to);
            } catch (MessagingException e) {
                e.printStackTrace();
                LOGGER.log(Level.INFO , "多线程发送给 " +
                        to + " 不含附件的邮件（" + title + "）发送失败！");
            }
        }).start();
    }

    /**
     * 发送邮件 一对一 含附件
     * @param title 标题
     * @param content 内容
     * @param to 收件人
     * @param path 附件地址
     * @param name 附件名称（包含后缀）
     */
    public static void send_email(String title , String content , String to , String path , String name){
        new Thread(() -> {
            try {
                JakartaEmail.send_email(title , content , to , path , name);
            } catch (MessagingException e) {
                e.printStackTrace();
                LOGGER.log(Level.INFO , "多线程发送给 " +
                        to + " 含附件的邮件（" + title + "）发送失败！");
            }
        }).start();
    }
}
