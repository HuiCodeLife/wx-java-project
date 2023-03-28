package com.h.mapper;



import com.h.entity.RankingItem;
import com.h.entity.WxFollowInfo;

import java.util.List;

/**
 * @author Lin
 */
public interface WxFollowInfoMapper {
    int deleteByPrimaryKey(String openid);

    int insert(WxFollowInfo record);

    int insertSelective(WxFollowInfo record);

    WxFollowInfo selectByPrimaryKey(String openid);

    int updateByPrimaryKeySelective(WxFollowInfo record);

    int updateByPrimaryKey(WxFollowInfo record);

    List<RankingItem> selectRankingList();

    int selectCountByOpenid(String openid);
}
