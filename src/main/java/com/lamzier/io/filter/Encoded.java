package com.lamzier.io.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

/**
 * 编码过滤器
 */
public class Encoded implements Filter {
    String encoding = null;//编码类型

    @Override
    public void init(FilterConfig filterConfig){
        encoding = filterConfig.getInitParameter("encoding");//获取编码参数
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (encoding == null){
            filterChain.doFilter(servletRequest , servletResponse);//传递
            return;
        }
//        HttpServletRequest req = (HttpServletRequest) servletRequest;
//        String content_type = req.getHeader("content-type");
//        if(content_type != null &&
//                content_type.toLowerCase().contains("multipart/form-data")){//非表单
//            filterChain.doFilter(servletRequest,servletResponse);//传递
//            return;
//        }
        servletRequest.setCharacterEncoding(encoding);//设置接受编码
        servletResponse.setContentType("text/html; charset=" + encoding);//设置返回协议编码
        filterChain.doFilter(servletRequest,servletResponse);//传递
    }

    @Override
    public void destroy() {
        encoding = null;//清理
    }
}
