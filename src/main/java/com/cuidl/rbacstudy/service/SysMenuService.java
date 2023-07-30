package com.cuidl.rbacstudy.service;

import com.cuidl.rbacstudy.entity.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.ArrayList;
import java.util.List;

/**
* @author cuidl
* @description 针对表【sys_menu】的数据库操作Service
* @createDate 2023-07-22 23:06:19
*/
public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> buildTreeMenu(List<SysMenu> menuList);
}
