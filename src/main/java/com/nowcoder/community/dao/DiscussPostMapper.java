package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    // 如果需要动态sql<if>，且这个方法又只有只一个条件，那么就必须加上@Param注解取别名,<if>中写别名
    int selectDiscussPostRows(@Param("userId") int userId);
}
