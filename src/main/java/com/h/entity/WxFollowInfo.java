package com.h.entity;

import java.util.Date;

/**
 * @author Lin
 */
public class WxFollowInfo {
    private String openid;

    private String followOpenid;

    private Date followTime;

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid == null ? null : openid.trim();
    }

    public String getFollowOpenid() {
        return followOpenid;
    }

    public void setFollowOpenid(String followOpenid) {
        this.followOpenid = followOpenid == null ? null : followOpenid.trim();
    }

    public Date getFollowTime() {
        return followTime;
    }

    public void setFollowTime(Date followTime) {
        this.followTime = followTime;
    }
}
