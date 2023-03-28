package com.h.mapper;



import com.h.entity.WxAssistLevel;

import java.util.List;

/**
 * @author Lin
 */
public interface WxAssistLevelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(WxAssistLevel record);

    int insertSelective(WxAssistLevel record);

    WxAssistLevel selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WxAssistLevel record);

    int updateByPrimaryKey(WxAssistLevel record);

    List<WxAssistLevel> selectAll();
}
