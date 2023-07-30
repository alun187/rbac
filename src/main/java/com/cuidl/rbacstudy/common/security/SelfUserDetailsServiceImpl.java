package com.cuidl.rbacstudy.common.security;

import com.cuidl.rbacstudy.common.exception.UserCountLockException;
import com.cuidl.rbacstudy.entity.SysUser;
import com.cuidl.rbacstudy.service.SysUserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author cuidl
 */
@Service
public class SelfUserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户名或者密码错误！");
        } else if ("1".equals(user.getStatus())) {
            throw new UserCountLockException("该账号已被封禁，具体请联系管理员！");
        }
        return new User(username, user.getPassword(), getUserAuthority(user.getId()));
    }

    public List<GrantedAuthority> getUserAuthority(Long id) {
        String authority = sysUserService.getAuthorityById(id);
        return AuthorityUtils.commaSeparatedStringToAuthorityList(authority);
    }
}
