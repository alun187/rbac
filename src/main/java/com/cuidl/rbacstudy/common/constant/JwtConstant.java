package com.cuidl.rbacstudy.common.constant;

/**
 * 系统级静态变量
 * @author cuidl
 */
public class JwtConstant {

    /**
     * Token不存在
     */
    public static final int JWT_ERRCODE_NULL = 4000;
    /**
     * Token过期
     */
    public static final int JWT_ERRCODE_EXPIRE = 4001;
    /**
     * 验证不通过
     */
    public static final int JWT_ERRCODE_FAIL = 4002;

    /**
     * JWT
     */
    public static final String JWT_SECERT = "8677df7fc3a34e26a61c034d5ec8245d";			//密匙
    public static final long JWT_TTL = 24*60 * 60 * 1000;									//token有效时间
}
