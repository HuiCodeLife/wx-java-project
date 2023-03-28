package com.h.web;

import com.h.service.WxService;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Lin
 */
@RestController
public class WxController {

    @Autowired
    private WxService wxService;

    /**
     * 消息验证
     *
     * @param signature
     * @param timestamp
     * @param nonce
     * @param echostr
     * @return
     */
    @GetMapping("/")
    public String check(String signature,
                        String timestamp,
                        String nonce,
                        String echostr) {

        // 1）将token、timestamp、nonce三个参数进行字典序排序
        String token = "huicodelife";
        List<String> list = Arrays.asList(token, timestamp, nonce);
        //排序
        Collections.sort(list);
        // 2）将三个参数字符串拼接成一个字符串进行sha1加密
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : list) {
            stringBuilder.append(s);
        }
        //加密
        try {
            MessageDigest instance = MessageDigest.getInstance("sha1");
            //使用sha1进行加密，获得byte数组
            byte[] digest = instance.digest(stringBuilder.toString().getBytes());
            StringBuilder sum = new StringBuilder();
            for (byte b : digest) {
                sum.append(Integer.toHexString((b >> 4) & 15));
                sum.append(Integer.toHexString(b & 15));
            }
            // 3）开发者获得加密后的字符串可与 signature 对比，标识该请求来源于微信
            if (!StringUtils.isEmpty(signature) && signature.equals(sum.toString())) {
                return echostr;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 接收消息
     * @param request
     * @return
     */
    @PostMapping("/")
    public String receiveMessage(HttpServletRequest request) {

        try {
            ServletInputStream inputStream = request.getInputStream();
            return wxService.handleMessage(inputStream);
        } catch (IOException | DocumentException e) {
            return e.getMessage();
        }
    }

    /**
     * 获得排行榜
     * @return
     */
    @RequestMapping("/getRankingList")
    public String getRankingList(){
        return wxService.getRankingList();
    }


    /**
     * 用户点击参与活动 -> 获取用户信息
     * @param request
     * @return
     */
    @RequestMapping("/getSignUpUserInfo")
    public String getSignUpUserInfo(HttpServletRequest request) {
        //获取code
        String code = request.getParameter("code");
        return wxService.getSignUpUserInfo(code);
    }

    @RequestMapping("/getUserInfo")
    public String getUserInfo(HttpServletRequest request) {
        try {
            //获取code
            String code = request.getParameter("code");
            return wxService.getUserInfo(code);
        } catch (IOException | DocumentException e) {
            return e.getMessage();
        }
    }


}
