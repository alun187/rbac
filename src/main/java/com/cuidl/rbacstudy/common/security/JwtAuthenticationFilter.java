package com.cuidl.rbacstudy.common.security;

import com.cuidl.rbacstudy.common.constant.JwtConstant;
import com.cuidl.rbacstudy.entity.CheckResult;
import com.cuidl.rbacstudy.entity.SysUser;
import com.cuidl.rbacstudy.service.SysUserService;
import com.cuidl.rbacstudy.utils.JwtUtils;
import com.cuidl.rbacstudy.utils.StringUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * jwt认证自定义过滤器
 * @author cuidl
 */
@Slf4j
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    @Resource
    private SysUserService sysUserService;

    @Resource
    private SelfUserDetailsServiceImpl userDetailsService;

    private static final String[] URL_WHITELIST = {
            "/login",
            "/logout",
            "/captcha",
            "/password",
            "/image/**"
    };

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = request.getHeader("token");
        log.info("请求url:" + request.getRequestURI());
        // 如果token是空或者url在白名单里 则放行 让后面的spring security认证过滤器去认证
        if (StringUtil.isEmpty(token) || new ArrayList<String>(Arrays.asList(URL_WHITELIST)).contains(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }
        CheckResult checkResult = JwtUtils.validateJWT(token);
        if (!checkResult.isSuccess()) {
            switch (checkResult.getErrCode()) {
                case JwtConstant.JWT_ERRCODE_NULL: throw new JwtException("Token不存在");
                case JwtConstant.JWT_ERRCODE_FAIL: throw new JwtException("Token验证不通过");
                case JwtConstant.JWT_ERRCODE_EXPIRE: throw new JwtException("Token过期");
                default:throw new JwtException("token验证未知错误");
            }
        }

        Claims claims = JwtUtils.parseJWT(token);
        String username = claims.getSubject();
        SysUser sysUser = sysUserService.getUserByUsername(username);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(username, null, userDetailsService.getUserAuthority(sysUser.getId()));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        chain.doFilter(request, response);
    }

}
