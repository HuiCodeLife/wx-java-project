package com.h.common.message;

import com.thoughtworks.xstream.XStream;

import java.util.Map;

/**
 * @author Lin
 */
public class MessageUtil {
    /**
     * 生成TextMessage的XML字符串
     * @param map
     * @param content
     * @return
     */
    public static String generateTextMessageForXML(Map<String,String> map,String content){
        TextMessage textMessage = new TextMessage();
        textMessage.setToUserName(map.get("FromUserName"));
        textMessage.setFromUserName(map.get("ToUserName"));
        textMessage.setMsgType("text");
        textMessage.setContent(content);
        textMessage.setCreateTime(System.currentTimeMillis()/1000);

        //XStream将Java对象转换成xml字符串
        XStream xStream = new XStream();
        xStream.processAnnotations(TextMessage.class);
        String xml = xStream.toXML(textMessage);
        return xml;
    }
}
