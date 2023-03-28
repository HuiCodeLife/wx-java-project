package com.h.common.token;


/**
 * @author Lin
 */
public class AccessToken {
    private String token;
    private long expireTime;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireIn) {
        this.expireTime = System.currentTimeMillis() + expireIn * 1000;
    }

    /**
     * 判断是否超时
     * @return
     */
    public boolean isExpired(){
        return System.currentTimeMillis()>this.expireTime;
    }
}
