package com.cuidl.rbacstudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cuidl.rbacstudy.entity.PageBean;
import com.cuidl.rbacstudy.entity.SysRole;
import com.cuidl.rbacstudy.entity.SysRoleMenu;
import com.cuidl.rbacstudy.entity.SysUserRole;
import com.cuidl.rbacstudy.service.SysRoleMenuService;
import com.cuidl.rbacstudy.service.SysRoleService;
import com.cuidl.rbacstudy.service.SysUserRoleService;
import com.cuidl.rbacstudy.utils.R;
import com.cuidl.rbacstudy.utils.StringUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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

    @Resource
    private SysRoleMenuService roleMenuService;

    @GetMapping("/listAll")
    @PreAuthorize("hasAuthority('system:role:query')")
    public R listAll() {
        Map<String, Object> resultMap = new HashMap<>();
        List<SysRole> roleList = sysRoleService.list(new QueryWrapper<SysRole>().eq("del_flag", 0));
        resultMap.put("roleList", roleList);
        return R.ok(resultMap);
    }

    /**
     * 添加或者修改
     * @param role
     * @return
     */
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

    /**
     * 根据条件分页查询角色列表
     *
     * @param pageBean
     * @return
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('system:role:query')")
    public R list(@RequestBody PageBean pageBean) {
        String query = pageBean.getQuery().trim();
        Page<SysRole> rolePage = sysRoleService.page(new Page<SysRole>(pageBean.getPageNum(), pageBean.getPageSize()),
                new QueryWrapper<SysRole>().like(StringUtil.isNotEmpty(query), "name", query).eq("del_flag", "0"));

        List<SysRole> roleList = rolePage.getRecords();
        HashMap<String, Object> data = new HashMap<>();
        data.put("roleList", roleList);
        data.put("total", rolePage.getTotal());
        return R.ok(data);
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:query')")
    public R findById(@PathVariable(value = "id") Integer id) {
        SysRole role = sysRoleService.getById(id);
        HashMap<String, Object> map = new HashMap<>();
        map.put("sysRole", role);
        return R.ok(map);
    }

    /**
     * 删除
     *
     * @param ids
     * @return
     */
    @Transactional
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public R delete(@RequestBody Long[] ids) {
        List<SysRole> roleList = sysRoleService.listByIds(Arrays.asList(ids));
        for (SysRole role : roleList) {
            role.setDelFlag("1");
        }
        sysRoleService.updateBatchById(roleList);
        userRoleService.remove(new QueryWrapper<SysUserRole>().in("role_id", ids));
        return R.ok();
    }

    /**
     * 获取当前角色的权限菜单
     *
     * @param id
     * @return
     */
    @GetMapping("/menus/{id}")
    @PreAuthorize("hasAuthority('system:role:query')")
    public R menus(@PathVariable(value = "id") Integer id) {
        List<SysRoleMenu> menus = roleMenuService.list(new QueryWrapper<SysRoleMenu>().eq("role_id", id));
        List<Long> menuIds = menus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        return R.ok().put("menuIdList", menuIds);
    }

    /**
     * 更新角色权限信息
     *
     * @param id
     * @param menuIds
     * @return
     */
    @Transactional
    @PostMapping("/updateMenus/{id}")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public R updateMenus(@PathVariable(value = "id") Long id, @RequestBody Long[] menuIds) {
        roleMenuService.remove(new QueryWrapper<SysRoleMenu>().eq("role_id", id));
        List<SysRoleMenu> list = new ArrayList<>();
        Arrays.stream(menuIds).forEach(menuId -> {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(id);
            roleMenu.setMenuId(menuId);
            list.add(roleMenu);
        });
        roleMenuService.saveBatch(list);
        return R.ok();
    }
}