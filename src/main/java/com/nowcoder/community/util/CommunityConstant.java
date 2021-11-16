package com.nowcoder.community.util;

public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态下登录凭证超时时间,12小时
     */
    int DEFAULT_EXPIRED_SECONDS = 3600*12;

    /**
     * 勾选记住我后，超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600*24*10;

    /**
     * 帖子实体类型
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 评论实体类型
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 用户实体类型
     */
    int ENTITY_TYPE_USER = 3;

}
