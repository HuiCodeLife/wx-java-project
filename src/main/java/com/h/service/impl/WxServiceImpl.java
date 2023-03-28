package com.h.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.h.common.constant.QRCodeConstant;
import com.h.common.message.Image;
import com.h.common.message.ImageMessage;
import com.h.common.message.MessageUtil;
import com.h.common.token.TokenUtil;
import com.h.common.user.SubscribedUser;
import com.h.common.util.HttpUtil;
import com.h.common.util.ImageUtil;
import com.h.entity.*;
import com.h.mapper.WxAssistLevelMapper;
import com.h.mapper.WxFollowInfoMapper;
import com.h.mapper.WxUserMapper;
import com.h.service.WxService;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.h.common.constant.Constant.BAOMING;

@Service
public class WxServiceImpl implements WxService {

    @Value("${redirect.domain}")
    private String redirectDomain;

    @Value("${wx.appid}")
    private String appId;

    @Value("${wx.secret}")
    private String secret;

    @Autowired
    private WxUserMapper userMapper;

    @Autowired
    private WxFollowInfoMapper followInfoMapper;

    @Autowired
    private WxAssistLevelMapper assistLevelMapper;



    @Override
    public String handleMessage(ServletInputStream inputStream) throws IOException, DocumentException {
        Map<String, String> map = getRequestMap(inputStream);
        String msgType = map.get("MsgType");
        String message = "";
        switch (msgType) {
            case "text":
                //海报生成
                if (BAOMING.equals(map.get("Content"))) {
                    //获得用户授权，拉取用户信息，存入微信号昵称对应表
                    message = createSignUpRedirectUri(map);
                }else if("海报".equals(map.get("Content"))){
                    message = createPoster(map);
                }else if("排行榜".equals(map.get("Content"))){
                    // 点击 排行榜 查看
                    message = getRankingLink(map);
                }
                break;
            case "event":
                if ("subscribe".equals(map.get("Event"))&& StringUtils.isNotEmpty(map.get("EventKey"))) {//未关注用户扫二维码后关注公众号，推送
                    message = handleSubscribe(map);
                }
                break;
            default:
                break;

        }
        return message;
    }

    /**
     * 返回查看排行榜链接
     * @return
     */
    private String getRankingLink(Map<String, String> map) {
        String url = "https://"+redirectDomain+"/getRankingList";
        String xml = MessageUtil.generateTextMessageForXML(map, "点击查看：<a style='color:#007ecc;' href=\"" + url + "\">助力排行榜</a>");
        return xml;
    }

    //裂变进来的用户
    @Override
    public String getUserInfo(String code) throws IOException, DocumentException {
        //根据code获取Access_token的地址
        String url = String.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                appId,
                secret,
                code);
        //发送get请求获得access_token
        String result = HttpUtil.doGet(url);
        String accessToken = JSONObject.parseObject(result).getString("access_token");
        String openId = JSONObject.parseObject(result).getString("access_token");
        //拿到access-token后拉取用户的基本信息 access_token openid
        String userInfoUrl = String.format("https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN",
                accessToken,
                openId);
        //拉取用户信息
        String userInfo = HttpUtil.doGet(userInfoUrl);
        //解析获得微信号、昵称、头像
        SubscribedUser user = JSONObject.parseObject(userInfo, SubscribedUser.class);
        //存入微信号昵称对应表
        WxUser tUser = new WxUser();
        BeanUtils.copyProperties(user,tUser);
        try {
            userMapper.insertSelective(tUser);
        } catch (Exception e) {
            //log.log(e.getMessage());
        }
        return "<h2>关注成功，请返回。参加活动请在公众号输入：</h2><h1>报名</h1>";
    }

    /**
     * 拉取主动获取海报的用户信息
     * @param code
     * @return
     */
    @Override
    public String getSignUpUserInfo(String code) {
        //根据code获取Access_token的地址
        String url = String.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                appId,
                secret,
                code);
        //发送get请求获得access_token
        String result = HttpUtil.doGet(url);
        String accessToken = JSONObject.parseObject(result).getString("access_token");
        String openId = JSONObject.parseObject(result).getString("openId");
        //拿到access-token后拉取用户的基本信息 access_token openid
        String userInfoUrl = String.format("https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN",
                accessToken,
                openId);
        //拉取用户信息
        String userInfo = HttpUtil.doGet(userInfoUrl);
        //解析获得微信号、昵称、头像
        SubscribedUser user = JSONObject.parseObject(userInfo, SubscribedUser.class);
        // 存入微信号昵称对应表
        WxUser tUser = new WxUser();
        BeanUtils.copyProperties(user,tUser);
        userMapper.insertSelective(tUser);
        String html = getSignUpHtml();
        return html;
    }

    /**
     * 报名成功的html
     * @return
     */
    private String getSignUpHtml() {
                //return "<h2>报名成功，返回后请在公众号输入关键字：</h2><h1>海报</h1><h2>分享活动海报，邀请好友扫码助力，输入\"排行榜\"查看助力排行榜</h2>";
        String html = "<!doctype html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <title>活动详情</title>\n" +
                "    <link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css\" integrity=\"sha384-HSMxcRTRxnN+Bdg0JdbxYKrThecOKuH5zCYotlSAcp1+c8xmyTe9GYg1l9a69psu\" crossorigin=\"anonymous\">\n" +
                "  </head>\n" +
                "  <body>\n" +
                "\t  <div class=\"container\">\n" +
                "\t\t  <div class=\"row\">\n" +
                "\t\t\t  <blockquote>\n" +
                "\t\t\t    <p>恭喜你，报名成功！</p>\n" +
                "\t\t\t  \t\t<p>请关闭此页面后在公众号输入关键字：</p>\n" +
                "\t\t\t  \t\t<p><strong>海报</strong></p>\n" +
                "\t\t\t    <p>分享活动海报，邀请好友扫码助力，输入&nbsp;\"<strong>排行榜</strong>\"&nbsp;查看助力排行榜</p>\n" +
                "\t\t\t  </blockquote>\n" +
                "\t\t  </div>\n" +
                "\t  </div>\n" +
                "    <script src=\"https://cdn.jsdelivr.cn/npm/jquery@1.12.4/dist/jquery.min.js\" integrity=\"sha384-nvAa0+6Qg9clwYCGGPpDQLVpLNn0fRaROjHqs13t4Ggj3Ez50XnGQqc/r8MhnRDZ\" crossorigin=\"anonymous\"></script>\n" +
                "<script src=\"https://stackpath.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js\" integrity=\"sha384-aJ21OjlMXNL5UyIl/XNwTMqvzeRMZH2w8c5cRVpzpU8Y5bApTppSuUkhZXN0VxHd\" crossorigin=\"anonymous\"></script>\n" +
                "  </body>\n" +
                "</html>";
        return html;
    }

    /**
     * 生成排行榜
     * @return 带table的html
     */
    @Override
    public String getRankingList() {
        List<RankingItem> rankingList = followInfoMapper.selectRankingList();
        String html = createHtml(rankingList);
        return html;
    }

    /**
     * 生成带table的html
     * @param rankingList
     * @return
     */
    private String createHtml(List<RankingItem> rankingList) {
        String preHtml = "<!doctype html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <title>助力排行榜</title>\n" +
                "    <link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css\" integrity=\"sha384-HSMxcRTRxnN+Bdg0JdbxYKrThecOKuH5zCYotlSAcp1+c8xmyTe9GYg1l9a69psu\" crossorigin=\"anonymous\">\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <table  width=\"90%\" align=\"center\" style=\"text-align: center;\"  class=\"table table-striped\">\n" +
                "\t\t<thead>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td>微信名</td>\n" +
                "\t\t\t\t<td>助力值</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t</thead>\n" +
                "      <tbody>";
        StringBuilder stringBuilder = new StringBuilder();
        for (RankingItem rankingItem : rankingList) {
            stringBuilder.append("<tr><td>");
            stringBuilder.append(rankingItem.getNickname());
            stringBuilder.append("</td><td>");
            stringBuilder.append(rankingItem.getCount());
            stringBuilder.append("</td></tr>");
        }
        String content = stringBuilder.toString();
        String afterHtml = "</tbody></table><script src=\"https://cdn.jsdelivr.cn/npm/jquery@1.12.4/dist/jquery.min.js\" integrity=\"sha384-nvAa0+6Qg9clwYCGGPpDQLVpLNn0fRaROjHqs13t4Ggj3Ez50XnGQqc/r8MhnRDZ\" crossorigin=\"anonymous\"></script>\n" +
                "<script src=\"https://stackpath.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js\" integrity=\"sha384-aJ21OjlMXNL5UyIl/XNwTMqvzeRMZH2w8c5cRVpzpU8Y5bApTppSuUkhZXN0VxHd\" crossorigin=\"anonymous\"></script>\n" +
                "</body></html>";
        return preHtml+content+afterHtml;
    }

    /**
     * 未关注用户扫二维码后关注公众号，推送
     * @param map
     * @return
     */
    private String handleSubscribe(Map<String, String> map) {
        //解析map
        //EventKey:qrscene_oRQ325lwAb_WyRRs0WsY3g3i6RME
        //将分享者信息和被关注者存入数据库的关注量表和关注记录表- t_follow_info
        //获得分享者的微信号
        String sharedUserWxId = getSharedUserWxId(map.get("EventKey"));
        String fromUserName = map.get("FromUserName");
        WxFollowInfo followInfo = new WxFollowInfo();
        followInfo.setOpenid(sharedUserWxId);
        followInfo.setFollowOpenid(fromUserName);
        followInfo.setFollowTime(new Date());
        try {
            followInfoMapper.insertSelective(followInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 给分享者推送消息，发送模版消息
        sendModelMessage(sharedUserWxId);

        //获取用户信息，这一步可以不用获取，待用户报名活动时再获取
        //用户关注后的回复内容，点击链接->回复"海报"，生成海报
        String url = String.format("https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s#wechat_redirect",
                appId,
                //redirect_uri用户同意后跳转的地址
                "https://"+redirectDomain+"/getSignUpUserInfo",
                //scope
                "snsapi_userinfo");
        String message =  MessageUtil.generateTextMessageForXML(map, "欢迎关注本公众号，我们举办了\"开源时代，共码未来\"活动，分享活动海报邀请好友扫码进行助力，" +
                "助力人数满足条件可领取精美大礼包一份：键盘+鼠标+Java全套学习资料！" +
                "点击链接：<a style='color:#007ecc;' href=\"" + url + "\">参与活动</a>");
        return message;

    }

    /**
     * 助力成功后，给分享者发送模版消息
     * @param sharedUserWxId
     */
    private void sendModelMessage(String sharedUserWxId) {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s",
                TokenUtil.getAccessToken(appId,secret));
        //查询当前助力值
        int count = followInfoMapper.selectCountByOpenid(sharedUserWxId);
        //根据目前助力值获得返回内容
        String remark = getRemarkContent(count);
        String data = "{\n" +
                "           \"touser\":\""+sharedUserWxId+"\",\n" +
                "           \"template_id\":\"7q3IPFWaVtebdZDCfzB13QoAwm-Um6QFp61a1AGhSZY\",\n" +
                "           \"data\":{\n" +
                "                   \"first\": {\n" +
                "                       \"value\":\"恭喜你！您得到了一位好友的支持！\",\n" +
                "                       \"color\":\"#173177\"\n" +
                "                   },\n" +
                "                   \"keyword1\":{\n" +
                "                       \"value\":\""+count+"\",\n" +
                "                       \"color\":\"#173177\"\n" +
                "                   },\n" +
                "                   \"remark\":{\n" +
                "                       \"value\":\""+remark+"\",\n" +
                "                       \"color\":\"#173177\"\n" +
                "                   }\n" +
                "           }\n" +
                "       }";
        HttpUtil.doPost(url, data);
    }

    /**
     * 根据目前助力值获得返回内容
     * 比如1-3个档，1档几个，2档几个，3档几个，都可以在后台设置
     * @param count
     * @return
     */
    private String getRemarkContent(int count) {

        List<WxAssistLevel> assistLevels = assistLevelMapper.selectAll();
        int level1 = 0;
        int level2 = 0;
        int level3 = 0;
        int level4 = 0;
        for (WxAssistLevel assistLevel : assistLevels) {
            switch (assistLevel.getAssistLevel()){
                case "第一档":
                    level1 = assistLevel.getAssistCount();
                    break;
                case "第二档":
                    level2 = assistLevel.getAssistCount();
                    break;
                case "第三档":
                    level3 = assistLevel.getAssistCount();
                    break;
                case "第四档":
                    level4 = assistLevel.getAssistCount();
                    break;
                default: break;
            }
        }
        String remark = "";
        if(count<level4){
            remark = "助力值目前处于第五档，距离第四档还差："+(level4-count)+"个";
        }else if(count>=level4 &&count<level3){
            remark = "助力值目前处于第四档，距离第三档还差："+(level3-count)+"个";
        }else if(count>=level3 &&count<level2){
            remark = "助力值目前处于第三档，距离第二档还差："+(level2-count)+"个";
        }else if(count>=level2 &&count<level1){
            remark = "助力值目前处于第二档，距离第一档还差："+(level1-count)+"个";
        }else {
            remark = "恭喜你，助力值处于第一档，继续分享，让自己摇摇领先！";
        }
        return remark;
    }

    /**
     * 引导报名活动用户访问地址进行授权，之后改用模版消息来封装
     * @param map
     * @return
     */
    private String createSignUpRedirectUri(Map<String, String> map) {
        //获得关注者的信息（微信号、昵称、头像等）并存入微信号昵称对应表
        //先引导用户访问地址进行授权，之后改用模版消息来封装
        String url = String.format("https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s#wechat_redirect",
                appId,
                //redirect_uri用户同意后跳转的地址
                "https://"+redirectDomain+"/getSignUpUserInfo",
                //scope
                "snsapi_userinfo");
        String xml = MessageUtil.generateTextMessageForXML(map, "点击链接：<a href=\"" + url + "\">参与活动</a>");
        return xml;
    }

    /*
    引导裂变用户访问地址进行授权，之后改用模版消息来封装
     */
    private String createRedirectUri(Map<String, String> map) {
        //获得关注者的信息（微信号、昵称、头像等）并存入微信号昵称对应表
        //先引导用户访问地址进行授权，之后改用模版消息来封装
        String url = String.format("https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s#wechat_redirect",
                appId,
                //redirect_uri用户同意后跳转的地址
                "https://"+redirectDomain+"/getUserInfo",
                //scope
                "snsapi_userinfo");
        String xml = MessageUtil.generateTextMessageForXML(map, "点击<a href=\"" + url + "\">这里</a>完成关注");
        return xml;
    }


    /**
     * 获得分享者的微信号
     * @param eventKey
     * @return
     */
    private String getSharedUserWxId(String eventKey) {
        return eventKey.substring(eventKey.indexOf('_') + 1);
    }

    private String createPoster(Map<String, String> map) {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s",
                TokenUtil.getAccessToken(appId,secret));
        //String data = "{\"action_name\": \"QR_LIMIT_STR_SCENE\", \"action_info\": {\"scene\": {\"scene_str\": \"oRQ325lwAb_WyRRs0WsY3g3i6RME\"}}}";
        //封装二维码参数
        QRCode qrCode = new QRCode();
        qrCode.setAction_name(QRCodeConstant.ACTION_NAME);
        String fromUserName = map.get("FromUserName");
        qrCode.setAction_info(new ActionInfo(new Scene(fromUserName)));

        String json = JSONObject.toJSONString(qrCode);
        //发送post请求
        String result = HttpUtil.doPost(url, json);
        //保存二维码图片至本地,以用户名作为文件名
        String qrPath = getQRCodeImage(result,fromUserName);
        //合成图片
        //获得用户头像：FromUserName
        WxUser user = userMapper.selectByPrimaryKey(fromUserName);
        String mergeImgPath = "";
        try {
            mergeImgPath = ImageUtil.MergeImage(qrPath,user.getHeadimgurl(),fromUserName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //上传合成图片资源到微信服务器
        String mediaId = uploadQRCodeImage(mergeImgPath);
        return createImageMessage(map,mediaId);
    }

    /**
     * 上传图片到微信服务器,返回mediaId
     * @param path meidaId
     */
    private String uploadQRCodeImage(String path) {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/media/upload?access_token=%s&type=%s",
                TokenUtil.getAccessToken(appId,secret),
                "image");
        String result = HttpUtil.doPostByFile(url, null, path, "test");
        return JSONObject.parseObject(result).getString("media_id");
    }

    /**
     * 保存二维码至本地
     * @param result
     * @param fromUserName
     * @return 图片本地保存的路径
     */
    private String getQRCodeImage(String result, String fromUserName) {
        JSONObject ticket = JSONObject.parseObject(result);
        String ticketUrl = String.format("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s",ticket.getString("ticket"));
        String path = HttpUtil.download(ticketUrl,fromUserName);
        return path;
    }

    /**
     * 创建带二维码的图片消息并返回
     *
     * @param map
     * @param mediaId
     * @return
     */
    private String createImageMessage(Map<String, String> map, String mediaId) {
        ImageMessage imageMessage = new ImageMessage();
        imageMessage.setToUserName(map.get("FromUserName"));
        imageMessage.setFromUserName(map.get("ToUserName"));
        imageMessage.setCreateTime(System.currentTimeMillis()/1000);
        imageMessage.setImage(new Image(mediaId));
        imageMessage.setMsgType("image");
        //XStream将Java对象转换成xml字符串
        XStream xStream = new XStream();
        xStream.processAnnotations(ImageMessage.class);
        String xml = xStream.toXML(imageMessage);
        return xml;
    }

    /**
     * 获取请求消息中的xml
     * @param inputStream
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    private Map<String, String> getRequestMap(ServletInputStream inputStream) throws IOException, DocumentException {
        Map<String, String> map = new HashMap<>();
        SAXReader reader = new SAXReader();
        //读取request输入流，获得Document对象
        Document document = reader.read(inputStream);
        //获得root节点
        Element root = document.getRootElement();
        //获取所有的子节点
        List<Element> elements = root.elements();
        for (Element element : elements) {
            map.put(element.getName(), element.getStringValue());
        }
        return map;
    }

}
