package com.cuidl.rbacstudy.common.security;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cuidl.rbacstudy.entity.SysMenu;
import com.cuidl.rbacstudy.entity.SysRole;
import com.cuidl.rbacstudy.entity.SysUser;
import com.cuidl.rbacstudy.service.SysMenuService;
import com.cuidl.rbacstudy.service.SysUserService;
import com.cuidl.rbacstudy.utils.JwtUtils;
import com.cuidl.rbacstudy.utils.R;
import com.cuidl.rbacstudy.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 登录成功处理器
 * @author cuidl
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Resource
    private SysUserService userService;

    @Autowired
    private SysRoleService roleService;

    @Resource
    private SysMenuService menuService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();

        String token = JwtUtils.genJwtToken(authentication.getName());

        // 更新登录时间
        userService.update(new UpdateWrapper<SysUser>().set("login_date", new Date()).eq("username", authentication.getName()));

        SysUser user = userService.getUserByUsername(authentication.getName());
        // 获取当前用户角色
        List<SysRole> roleList = roleService.list(new QueryWrapper<SysRole>().inSql("id", String.format("select role_id from sys_user_role where user_id = '%s'", user.getId())));
        user.setRoles(roleList.stream().map(SysRole::getName).collect(Collectors.joining(",")));
        HashSet<SysMenu> menuSet = new HashSet<>();
        // 获取所有菜单权限
        for (SysRole role : roleList) {
            List<SysMenu> menuList = menuService.list(new QueryWrapper<SysMenu>().inSql("id", String.format("select menu_id from sys_role_menu where role_id = '%s'", role.getId())));
            menuSet.addAll(menuList);
        }
        List<SysMenu> menuList = new ArrayList<>(menuSet);
        // 排序
        menuList.sort(Comparator.comparing(SysMenu::getOrderNum));
        // 返回树形结构的菜单
        List<SysMenu> sysMenus = menuService.buildTreeMenu(menuList);

        outputStream.write(JSONUtil.toJsonStr(Objects.requireNonNull(Objects.requireNonNull(R.ok("登录成功").put("authorization", token)).put("currentUser", user)).put("menuList", sysMenus)).getBytes());
        outputStream.flush();
        outputStream.close();
    }
}
