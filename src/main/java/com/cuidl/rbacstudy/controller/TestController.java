package com.cuidl.rbacstudy.controller;

import com.cuidl.rbacstudy.entity.SysUser;
import com.cuidl.rbacstudy.service.SysUserService;
import com.cuidl.rbacstudy.utils.JwtUtils;
import com.cuidl.rbacstudy.utils.R;
import com.cuidl.rbacstudy.utils.StringUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cuidl
 */
@RestController
@RequestMapping("/self")
public class TestController {

    @Resource
    private SysUserService userService;

    @RequestMapping("test/get/user")
    @PreAuthorize("hasRole('ROLE_admin')")
    //@PreAuthorize("hasAuthority('system:user:resetPwid')")
    public R getUser(@RequestHeader(required = false) String authentication) {
        if (!StringUtil.isNotEmpty(authentication)){
            Map<String, Object> map = new HashMap<>();
            List<SysUser> userList = userService.list();
            map.put("userList", userList);
            return R.ok(map);
        }
        return R.error(401, "没有权限访问");
    }

    @RequestMapping("login")
    public R createToken(String username, String password) {
        String token = JwtUtils.genJwtToken(username);
        return R.ok().put("token", token);
    }
}
