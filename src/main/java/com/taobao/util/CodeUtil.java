package com.taobao.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CodeUtil {
    private static int width = 90;//定义图片的width
    private static int height = 20;//定义图片的height
    private static int codeCount = 4;//定义图片显示验证码的个数
    private static int xx = 15;
    private static int fontHeight = 18;
    private static int codeY = 16;
    private static char[] codeSequence = {'A','B','C','D','E','F','G','H','I','J',
    'K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c',
    'd','e','f','g','h','i','j','k','l','m','n','o','p','w','r','d','t','u','v',
     'w','x','y','z','0','1','2','3','4','5','6','7','8','9'};

    public static Map<String,Object> generateCodeAndPic(){
        //定义图像buffer
        BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics gd = buffImg.getGraphics();
        Random random=new Random();

        gd.setColor(Color.WHITE);
        gd.fillRect(0,0,width, height);

        Font font = new Font("Fixedsys",Font.BOLD,fontHeight);
        gd.setFont(font);
        //边框
        gd.setColor(Color.BLACK);
        gd.drawRect(0, 0, width-1, height-1);

        //随机产生40条干扰线
        gd.setColor(Color.BLACK);
        for (int i = 0; i < 40; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            gd.drawLine(x,y,x+xl,y+yl);
        }

        StringBuffer randomCode = new StringBuffer();
        int red=0,green=0,blue=0;

        //随机产生codeCount数字的验证码
        for (int i = 0; i < codeCount; i++) {
            //得到随机生成的验证码数字
            String code = String.valueOf(codeSequence[random.nextInt(36)]);
            red = random.nextInt(255);
            green = random.nextInt(255);
            blue = random.nextInt(255);

            //用随机参数的颜色讲验证码绘制
            gd.setColor(new Color(red,green,blue));
            gd.drawString(code,(i+1)*xx, codeY);

            randomCode.append(code);

        }
        Map<String,Object> map = new HashMap<>();
        map.put("code", randomCode);
        map.put("codePic", buffImg);
        return map;

    }
    public static void main(String[] args) throws IOException {
        OutputStream out = new FileOutputStream("/home/weichong/Desktop/seckill2.0/a.jpg");
        Map<String,Object> map = CodeUtil.generateCodeAndPic();
        ImageIO.write((RenderedImage)map.get("codePic"),"jpeg", out);
        System.out.println("code is: "+map.get("code"));
    }
}
