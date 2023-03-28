package com.h.common.token;

import com.h.common.util.HttpUtil;
import net.sf.json.JSONObject;


/**
 * @author Lin
 */
public class TokenUtil {


    private static AccessToken accessToken = new AccessToken();

    private static void getToken(String appId,String secret){
        String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appId,
                secret);
        String result = HttpUtil.doGet(url);
        JSONObject jsonObject = JSONObject.fromObject(result);
        String token = jsonObject.getString("access_token");
        long expiresIn = jsonObject.getLong("expires_in");
        accessToken.setToken(token);
        accessToken.setExpireTime(expiresIn);
    }

    /**
     * 获取AccessToken
     * @return
     */
    public static String getAccessToken(String appId,String secret){
        if(accessToken == null || accessToken.isExpired()){
            getToken(appId,secret);
        }
        return accessToken.getToken();
    }

}
