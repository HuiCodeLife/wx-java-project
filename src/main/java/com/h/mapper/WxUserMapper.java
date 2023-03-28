package com.h.mapper;


import com.h.entity.WxUser;

/**
 * @author Lin
 */
public interface WxUserMapper {
    int deleteByPrimaryKey(String openid);

    int insert(WxUser record);

    int insertSelective(WxUser record);

    WxUser selectByPrimaryKey(String openid);

    int updateByPrimaryKeySelective(WxUser record);

    int updateByPrimaryKey(WxUser record);
}
