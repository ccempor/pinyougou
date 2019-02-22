package com.pinyougou.manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Controller
public class VCodeController {

    private Random ran = new Random();  //创建随机类

    private Color getRanColor() {
        int r = ran.nextInt(256);
        int g = ran.nextInt(256);
        int b = ran.nextInt(256);
        return new Color(r, g, b);
    }
    @RequestMapping("/vcode")
    public void doPost(HttpServletRequest request, HttpServletResponse response)throws Exception {
        //2) 创建缓存图片：指定宽
        int width=90, height=30;
        BufferedImage img = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
        //3) 获取画笔对象
        Graphics graphics = img.getGraphics();
        //设置画笔颜色
        graphics.setColor(Color.WHITE);   //设置为白色
        //4) 并且填充矩形区域
        graphics.fillRect(0,0, width, height);
        //5) 从字符数组中随机得到字符
        char[] arr = { 'A', 'B', 'C', 'D', 'N', 'E', 'W', 'b', 'o', 'y', '1', '2', '3', '4' };
        //创建字符串拼接对象
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            //得到下标
            int index = ran.nextInt(arr.length);
            //取一个字符
            char c = arr[index];
            sb.append(c);  //加到字符串中
            //6) 设置字体，大小为19，设置字的颜色随机
            graphics.setFont(new Font(Font.DIALOG, Font.BOLD + Font.ITALIC, 19));
            graphics.setColor(getRanColor());
            //把字符转成字符串
            graphics.drawString(String.valueOf(c), 10+(i*20), 20);
        }

        //把字符串放到会话域中
        HttpSession session = request.getSession();
        session.setAttribute("vcode", sb.toString());
        System.out.println("服务器验证码：" + sb);
        //8) 画干扰线8条线，线的位置是随机的，x范围在width之中，y的范围在height之中。
        for (int i = 0; i < 8; i++) {
            //设置不同的颜色
            graphics.setColor(getRanColor());
            int x1 = ran.nextInt(width);
            int y1 = ran.nextInt(height);
            int x2 = ran.nextInt(width);
            int y2 = ran.nextInt(height);
            graphics.drawLine(x1,y1,x2,y2);
        }

        //9) 将缓存的图片输出到响应输出流中，三个参数：图片对象， 格式， 输出流
        ImageIO.write(img, "jpg", response.getOutputStream());
    }

}
