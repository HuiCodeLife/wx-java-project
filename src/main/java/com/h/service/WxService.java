package com.h.service;

import org.dom4j.DocumentException;

import javax.servlet.ServletInputStream;
import java.io.IOException;

/**
 * 消息业务类
 * @author Lin
 */
public interface WxService {
    /**
     * 接收消息
     * @param inputStream 消息输入流
     * @return 结果
     * @throws IOException
     * @throws DocumentException
     */
    String handleMessage(ServletInputStream inputStream) throws IOException, DocumentException;

    /**
     * 根据code拉取用户信息，并将微信号、昵称、头像存入数据库的昵称微信号对应表中，因为微信号是用户的唯一标识
     * @param code
     */
    String getUserInfo(String code) throws IOException, DocumentException;

    /**
     * 拉取报名用户的信息
     * @param code
     * @return
     */
    String getSignUpUserInfo(String code);

    /**
     * 生成排行榜，返回排行榜的html
     * @return
     */
    String getRankingList();
}
