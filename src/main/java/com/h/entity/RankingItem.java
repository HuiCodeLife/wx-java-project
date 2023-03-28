package com.h.entity;

/**
 * 排行榜
 * @author Lin
 */
public class RankingItem {
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 关注数
     */
    private int count;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
