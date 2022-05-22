package com.lamzier.io.request.get;

import com.alibaba.fastjson2.JSONObject;
import com.lamzier.io.queue.AnalysisQueue;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 获取队列数量
 */
@WebServlet("/getAnalysisQueue")
public class getAnalysisQueue extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/json; charset=utf-8");//json
        resp.setHeader("Access-Control-Allow-Origin", "*");//允许跨域
        PrintWriter printWriter = resp.getWriter();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code" , 0);
        jsonObject.put("persion_num" , AnalysisQueue.getWaitListSize());
        jsonObject.put("msg" , "操作成功");
        printWriter.println(jsonObject.toJSONString());
    }
}
