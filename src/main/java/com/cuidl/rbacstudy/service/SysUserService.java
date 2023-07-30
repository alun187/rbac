package com.cuidl.rbacstudy.service;

import com.cuidl.rbacstudy.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author cuidl
* @description 针对表【sys_user】的数据库操作Service
* @createDate 2023-07-11 23:49:54
*/
public interface SysUserService extends IService<SysUser> {

    /**
     * 根据用户名查询
     * @param username 用户名
     * @return 用户对象
     */
    SysUser getUserByUsername(String username);

    String getAuthorityById(Long id);
}
