package com.cuidl.rbacstudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cuidl.rbacstudy.entity.SysRole;
import com.cuidl.rbacstudy.entity.SysUserRole;
import com.cuidl.rbacstudy.service.SysRoleService;
import com.cuidl.rbacstudy.service.SysUserRoleService;
import com.cuidl.rbacstudy.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.management.relation.RoleStatus;
import java.util.*;

/**
 * 系统角色Controller控制器
 *
 * @author cuidl
 */
@RestController
@RequestMapping("/sys/role")
public class SysRoleController {

    @Resource
    private SysRoleService sysRoleService;

    @Resource
    private SysUserRoleService userRoleService;

    @GetMapping("/listAll")
    @PreAuthorize("hasAuthority('system:role:query')")
    public R listAll() {
        Map<String, Object> resultMap = new HashMap<>();
        List<SysRole> roleList = sysRoleService.list();
        resultMap.put("roleList", roleList);
        return R.ok(resultMap);
    }

    /**
     * 用户角色授权
     *
     * @param userId
     * @param roleIds
     * @return
     */
    @Transactional
    @PostMapping("/grantRole/{userId}")
    @PreAuthorize("hasAuthority('system:user:role')")
    public R grantRole(@PathVariable("userId") Long userId, @RequestBody Long[] roleIds) {
        List<SysUserRole> userRoleList = new ArrayList<>();
        Arrays.stream(roleIds).forEach(r -> {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setRoleId(r);
            sysUserRole.setUserId(userId);
            userRoleList.add(sysUserRole);
        });
        userRoleService.remove(new QueryWrapper<SysUserRole>().eq("user_id", userId));
        userRoleService.saveBatch(userRoleList);
        return R.ok();
    }

    @RequestMapping("save")
    public R save(@RequestBody SysRole role) {
        if (role.getId() == null || role.getId() == -1) {
            role.setCreateTime(new Date());
            role.setUpdateTime(new Date());
            role.setDelFlag("0");
            sysRoleService.save(role);
        } else {
            role.setUpdateTime(new Date());
            sysRoleService.updateById(role);
        }
        return R.ok();
    }

}