package com.cuidl.rbacstudy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cuidl.rbacstudy.entity.SysMenu;
import com.cuidl.rbacstudy.service.SysMenuService;
import com.cuidl.rbacstudy.mapper.SysMenuMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* @author cuidl
* @description 针对表【sys_menu】的数据库操作Service实现
* @createDate 2023-07-22 23:06:19
*/
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService{

    @Override
    public List<SysMenu> buildTreeMenu(List<SysMenu> menuList) {
        ArrayList<SysMenu> resultMenus = new ArrayList<>();
        for (SysMenu menu : menuList) {
            for (SysMenu m : menuList) {
                if (m.getParentId().equals(menu.getId())) {
                    menu.getChildren().add(m);
                }
            }
            if (menu.getParentId() == 0) {
                resultMenus.add(menu);
            }
        }
        return resultMenus;
    }
}




