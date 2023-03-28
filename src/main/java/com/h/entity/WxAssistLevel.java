package com.h.entity;

/**
 * 助力相关
 * @author Lin
 */
public class WxAssistLevel {
    private Integer id;

    private String assistLevel;

    private Integer assistCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAssistLevel() {
        return assistLevel;
    }

    public void setAssistLevel(String assistLevel) {
        this.assistLevel = assistLevel == null ? null : assistLevel.trim();
    }

    public Integer getAssistCount() {
        return assistCount;
    }

    public void setAssistCount(Integer assistCount) {
        this.assistCount = assistCount;
    }
}
