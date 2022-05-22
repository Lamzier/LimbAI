package com.lamzier.io.utils;

import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * 邮件类
 */
public class JakartaEmail {

    //配置文件路径
    private static final URL EMAIL_CONFIG_PATH = JakartaEmail.class.getResource("../../../../config/email.yml");
    private static Map<String , Object> email_config;//配置信息
    private static List<Map<String , String>> email_user;//发送的邮件列表
    private static final Logger LOGGER = Logger.getLogger(JakartaEmail.class.getName());

    static {
        try {
            init();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE , "邮件信息初始化失败！");
        }
        LOGGER.log(Level.INFO , "邮件信息初始化成功！");
    }

    /**
     * 初始化
     */
    private static void init() throws IOException {
        if (EMAIL_CONFIG_PATH == null) {
            LOGGER.log(Level.SEVERE , "EMAIL_CONFIG_PATH文件找不到！");
            return;
        }
        Yaml yaml = new Yaml();
        InputStream inputStream = new FileInputStream(EMAIL_CONFIG_PATH.getPath());
        email_config= yaml.load(inputStream);//读取配置信息
        email_user = (List<Map<String, String>>) email_config.get("email_user");
        inputStream.close();//关闭
    }

    /**
     * 获取随机发送账号
     */
    private static Map<String ,String> getRandEmailUser(){
        int index = (int) (Math.random() * email_user.size());
        return email_user.get(index);
    }

    /**
     * 发送邮件 一对一 不含附件
     * @param title 标题
     * @param content 内容
     * @param to 收件人
     * @throws MessagingException 异常
     */
    public static void send_email(String title , String content , String to) throws MessagingException {
        Map<String , String> from = getRandEmailUser();
        Message message = getMessage(from);
        message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
        message.setSubject(title);//设置标题
        message.setText(content);//设置内容
        Transport.send(message);
        LOGGER.log(Level.INFO , "由 " + from.get("username") + " 发送给 " +
                to + " 不含附件的邮件（" + title + "）发送成功！");
    }

    /**
     * 发送邮件 一对一 含附件
     * @param title 标题
     * @param content 内容
     * @param to 收件人
     * @param path 附件地址
     * @param name 附件名称（包含后缀）
     * @throws MessagingException 异常
     */
    public static void send_email(String title , String content , String to , String path , String name) throws MessagingException {
        Map<String , String> from = getRandEmailUser();
        Message message = getMessage(from);
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(to));
        message.setSubject(title);//设置标题
        //创建消息主体部分
        BodyPart bodyPart = new MimeBodyPart();
        //设置实际消息
        bodyPart.setText(content);
        //创建一个multipart对象的实例
        Multipart multipart = new MimeMultipart();
        //设置第一个短信部分
        multipart.addBodyPart(bodyPart);
        //设置第二部分，即附件
        bodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(path);
        bodyPart.setDataHandler(new DataHandler(source));
        bodyPart.setFileName(name);
        multipart.addBodyPart(bodyPart);
        message.setContent(multipart);
        Transport.send(message);
        LOGGER.log(Level.INFO , "由 " + from.get("username") + " 发送给 " +
                to + " 含附件的邮件（" + title + "）发送成功！");
    }

    /**
     * 获取Message对象
     * @return Message对象
     */
    private static Message getMessage(Map<String , String> from) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", email_config.get("auth"));
        properties.put("mail.smtp.starttls.enable", email_config.get("starttls_enable"));
        properties.put("mail.smtp.host", email_config.get("host"));
        properties.put("mail.smtp.port",  email_config.get("port"));
        properties.put("mail.smtp.socketFactory.class", email_config.get("socketFactory_class"));
        properties.put("mail.smtp.socketFactory.fallback", email_config.get("socketFactory_fallback"));
        properties.put("mail.smtp.socketFactory.port", email_config.get("port"));
        properties.put("mail.smtp.ssl.enable", email_config.get("ssl_enable"));
        Session session = Session.getInstance(properties,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                from.get("username"),
                                from.get("password"));
                    }
                });
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from.get("username")));
        return message;
    }

}