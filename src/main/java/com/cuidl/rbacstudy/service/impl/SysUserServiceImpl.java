package com.cuidl.rbacstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cuidl.rbacstudy.entity.SysMenu;
import com.cuidl.rbacstudy.entity.SysRole;
import com.cuidl.rbacstudy.entity.SysUser;
import com.cuidl.rbacstudy.mapper.SysMenuMapper;
import com.cuidl.rbacstudy.mapper.SysRoleMapper;
import com.cuidl.rbacstudy.service.SysUserService;
import com.cuidl.rbacstudy.mapper.SysUserMapper;
import com.cuidl.rbacstudy.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author cuidl
* @description 针对表【sys_user】的数据库操作Service实现
* @createDate 2023-07-11 23:49:54
*/
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private SysRoleMapper roleMapper;

    @Resource
    private SysMenuMapper menuMapper;

    @Override
    public SysUser getUserByUsername(String username) {
        return getOne(new QueryWrapper<SysUser>().eq("username", username));
    }

    @Override
    public String getAuthorityById(Long id) {
        StringBuffer authority = new StringBuffer();
        List<SysRole> roleList = roleMapper.selectList(new QueryWrapper<SysRole>().inSql("id", String.format("select role_id from sys_user_role where user_id = '%s'", id)));
        if (roleList.size() > 0) {
            String userRoleStr = roleList.stream().map(role -> "ROLE_" + role.getCode()).collect(Collectors.joining(","));
            authority.append(userRoleStr);
        }
        HashSet<String> menuSet = new HashSet<>();
        for (SysRole role : roleList) {
            List<SysMenu> menuList = menuMapper.selectList(new QueryWrapper<SysMenu>().inSql("id", String.format("select menu_id from sys_role_menu where role_id = '%s'", role.getId())));
            menuList.forEach(menu -> {
                if (StringUtil.isNotEmpty(menu.getPerms())) {
                    menuSet.add(menu.getPerms());
                }
            });
        }
        if (menuSet.size() > 0) {
            authority.append(",");
            String userMenuStr = menuSet.stream().collect(Collectors.joining(","));
            authority.append(userMenuStr);
        }
        log.info("authority: " + authority);
        return authority.toString();
    }
}




