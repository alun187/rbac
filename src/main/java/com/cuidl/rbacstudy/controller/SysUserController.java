package com.cuidl.rbacstudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cuidl.rbacstudy.common.constant.Constant;
import com.cuidl.rbacstudy.entity.PageBean;
import com.cuidl.rbacstudy.entity.SysRole;
import com.cuidl.rbacstudy.entity.SysUser;
import com.cuidl.rbacstudy.service.SysRoleService;
import com.cuidl.rbacstudy.service.SysUserRoleService;
import com.cuidl.rbacstudy.service.SysUserService;
import com.cuidl.rbacstudy.utils.DateUtil;
import com.cuidl.rbacstudy.utils.R;
import com.cuidl.rbacstudy.utils.StringUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理
 *
 * @author cuidl
 */
@RestController
@RequestMapping("sys/user")
public class SysUserController {

    @Resource
    private SysUserService userService;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Resource
    private SysRoleService roleService;

    @Resource
    private SysUserRoleService userRoleService;

    @Value("${avatarImagesFilePath}")
    private String avatarImagesFilePath;

    private static final String SRC = "image/userAvatar/";

    @RequestMapping("save")
    @PreAuthorize("hasAnyAuthority('system:user:add','system:user:edit')")
    public R save(@RequestBody SysUser user) {
        if (user.getId() == null || user.getId() == -1) {
            user.setCreateTime(new Date());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setDelFlag("0");
            userService.save(user);
        } else {
            user.setUpdateTime(new Date());
            userService.updateById(user);
        }
        return R.ok();
    }

    /**
     * 添加或者修改
     * @param user
     * @return
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('system:role:add')"+"||"+"hasAuthority('system:role:edit')")
    public R updatePwd(@RequestBody SysUser user) {
        SysUser sysUser = userService.getById(user.getId());
        if (!passwordEncoder.matches(user.getOldPassword(), sysUser.getPassword())) {
            return R.error("旧密码输入有误");
        }
        sysUser.setPassword(passwordEncoder.encode(user.getNewPassword()));
        sysUser.setUpdateTime(new Date());
        userService.updateById(sysUser);
        return R.ok();
    }

    /**
     * 上传用户头像图片
     *
     * @param file 上传头像
     * @return map
     * @throws IOException
     */
    @RequestMapping("uploadImage")
    @PreAuthorize("hasAnyAuthority('system:user:edit')")
    public Map<String, Object> uploadImage(MultipartFile file) throws IOException {
        HashMap<String, Object> resultMap = new HashMap<>();
        if (!file.isEmpty()) {
            String filename = file.getOriginalFilename();
            assert filename != null;
            String suffixName = filename.substring(filename.lastIndexOf("."));
            String newFileName = DateUtil.getCurrentDateStr() + suffixName;
            FileUtils.copyInputStreamToFile(file.getInputStream(), new File(avatarImagesFilePath + newFileName));
            resultMap.put("code", 0);
            resultMap.put("msg", "上传成功");
            HashMap<String, Object> data = new HashMap<>();
            data.put("title", newFileName);
            data.put("src", SRC + newFileName);
            resultMap.put("data", data);
        }
        return resultMap;
    }

    /**
     * 修改用户头像
     *
     * @param user
     * @return
     */
    @RequestMapping("/updateAvatar")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public R updateAvatar(@RequestBody SysUser user) {
        SysUser sysUser = userService.getById(user.getId());
        sysUser.setUpdateTime(new Date());
        sysUser.setAvatar(user.getAvatar());
        userService.updateById(sysUser);
        return R.ok();
    }

    /**
     * 根据条件分页查询用户信息
     *
     * @param pageBean
     * @return
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('system:user:query')")
    public R list(@RequestBody PageBean pageBean) {
        String query = pageBean.getQuery().trim();
        Page<SysUser> userPage = userService.page(new Page<SysUser>(pageBean.getPageNum(), pageBean.getPageSize()),
                new QueryWrapper<SysUser>().like(StringUtil.isNotEmpty(query), "username", query));
        List<SysUser> userList = userPage.getRecords();
        userList.forEach(user -> {
            List<SysRole> roleList = roleService.list(new QueryWrapper<SysRole>().inSql("id",
                    String.format("select role_id from sys_user_role where user_id = '%s'", user.getId())));
            user.setSysRoleList(roleList);
        });
        HashMap<String, Object> data = new HashMap<>();
        data.put("userList", userList);
        data.put("total", userPage.getTotal());
        return R.ok(data);
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:query')")
    public R findById(@PathVariable(value = "id") Integer id) {
        SysUser sysUser = userService.getById(id);
        Map<String, Object> map = new HashMap<>();
        map.put("sysUser", sysUser);
        return R.ok(map);
    }

    /**
     * 验证用户名
     *
     * @param sysUser
     * @return
     */
    @PostMapping("/checkUserName")
    @PreAuthorize("hasAuthority('system:user:query')")
    public R checkUserName(@RequestBody SysUser sysUser) {
        if (userService.getUserByUsername(sysUser.getUsername()) == null) {
            return R.ok();
        } else {
            return R.error();
        }
    }

    /**
     * 删除
     *
     * @param ids
     * @return
     */
    @Transactional
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public R delete(@RequestBody Long[] ids) {
        userService.update(new UpdateWrapper<SysUser>().set("del_flag", "1").in("id", (Object[]) ids));
        return R.ok();
    }

    /**
     * 重置密码
     *
     * @param id
     * @return
     */
    @GetMapping("/resetPassword/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public R resetPassword(@PathVariable(value = "id") Integer id) {
        SysUser sysUser = userService.getById(id);
        sysUser.setPassword(passwordEncoder.encode(Constant.DEFAULT_PASSWORD));
        sysUser.setUpdateTime(new Date());
        userService.updateById(sysUser);
        return R.ok();
    }

    /**
     * 更新status状态
     *
     * @param id
     * @param status
     * @return
     */
    @GetMapping("/updateStatus/{id}/status/{status}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public R updateStatus(@PathVariable(value = "id") Integer id, @PathVariable(value = "status") String status) {
        SysUser sysUser = userService.getById(id);
        sysUser.setStatus(status);
        userService.saveOrUpdate(sysUser);
        return R.ok();
    }

}
