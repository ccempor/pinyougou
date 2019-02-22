package com.pinyougou.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-15<p>
 */
@Controller
public class LoginController {

    /** 注入身份认证管理器 */
    @Autowired
    private AuthenticationManager authenticationManager;

    /** 登录认证 */
    @RequestMapping("/user/login")
    public String login(String username, String password, String code,
                        HttpServletRequest request){
        // 判断请求方式
        if (request.getMethod().equalsIgnoreCase("post")){
            System.out.println(username + "==" + password);
            // 从Session中获取验证码
            String vcode = (String) request.getSession().getAttribute("vcode");
            // 跟用户输入的验证码比较
            if (code.equalsIgnoreCase(vcode)){
                // 创建用户名与密码认证对象
                UsernamePasswordAuthenticationToken token
                        = new UsernamePasswordAuthenticationToken(username,password);
                try {
                    // 调用认证方法，返回认证对象
                    Authentication authenticate = authenticationManager.authenticate(token);
                    // 判断是否认证成功
                    if (authenticate.isAuthenticated()) {
                        // 设置用户认证成功，往Session中添加认证通过信息
                        SecurityContextHolder.getContext()
                                .setAuthentication(authenticate);
                        // 重定向到登录成功页面
                        return "redirect:/admin/index.html";
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
        // 重定向到登录页面
        return "redirect:/login.html";

    }

    /** 获取登录用户名 */
    @GetMapping("/showLoginName")
    @ResponseBody
    public Map<String, String> showLoginName(){
        // 获取安全上下文对象
        SecurityContext securityContext = SecurityContextHolder.getContext();
        // 获取登录用户名
        String loginName = securityContext.getAuthentication().getName();

        Map<String, String> data = new HashMap<>();
        data.put("loginName", loginName);
        return data;
    }

}
