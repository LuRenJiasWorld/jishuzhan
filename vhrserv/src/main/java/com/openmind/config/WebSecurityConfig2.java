package com.openmind.config;

import com.openmind.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 基于数据库的角色认证
 * 创建三张表：user(用户)-->user_role(用户角色映射)<--role(角色)
 * <p>
 * EnableGlobalMethodSecurity 方法安全，prePostEnabled激活@PreAuthorize和@PostAuthorize注解，securedEnabled激活@Secured注解
 *
 * @author zhoujunwen
 * @date 2020-01-07
 * @time 16:24
 * @desc
 */
//@Configuration
//@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig2 extends WebSecurityConfigurerAdapter {
    @Autowired
    UserService userService;

    /**
     * 密码加密方式，NoOpPasswordEncoder不对密码加密
     *
     * @return
     */
    @Bean
    PasswordEncoder passwordEncoder() {
//        return NoOpPasswordEncoder.getInstance(); // NoOpPasswordEncoder不对密码加密
        return new BCryptPasswordEncoder(10);
    }

    /**
     * 角色继承，让角色之间有依赖关系。比如下面集成关系，则说明：
     * · 拥有 DBA 权限的角色拥有ADMIN 和 USER 的权限
     * · 拥有 ADMIN 权限的角色拥有 USER 的权限
     * · 拥有 USER 权限的角色只有 USER 权限
     *
     * @return
     */
    @Bean
    RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        String hierarchy = "ROLE_DBA > ROLE_ADMIN ROLE_ADMIN > ROLE_USER";
        roleHierarchy.setHierarchy(hierarchy);
        return roleHierarchy;
    }

    /**
     * 基于数据库的动态的认证
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
    }

    /**
     * configure(AuthenticationManagerBuilder auth)  实现了认证功能，但是受保护的资源是默认的，而且也不能根据实际情况进行角色管理
     * 如果要实现这些功能，则需要重写configure(HttpSecurity http)方法
     * <p>
     * 如果业务太复杂，可以配置多个HttpSecurity,具体可以看：MultiHttpSecurityConfig
     *
     * @param http
     * @throws Exception
     * @see MultiHttpSecurityConfig
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /*http.authorizeRequests() // 开启HttpSecurity的配置
                .antMatchers("/admin/**") // 匹配/admin/**资源模式
                .hasRole("ADMIN") // 对于/admin/**模式的路径需要 ADMIN 权限,数据库存储权限是必须添加 ROLE_ 前缀
                .antMatchers("/user/**") //  匹配/user/**资源模式
                .access("hasAnyRole('ADMIN', 'USER')") // 对于/user/**的访问需要 ADMIN 或 USER 权限
                .antMatchers("/dba/**") // 匹配/dba/**资源模式
                .access("hasRole('ADMIN') and hasRole('DBA')") // 对于/dba/**的资源必须要有 ADMIN 和 DBA 权限
                .anyRequest() // 对于除了上述以外的其他资源
                .authenticated() // 需要登录权限
                .and()
                .formLogin() // 登录表单
//                .loginPage("/login_page") // **** 默认是spring security提供的登录页面,实现WebMvcConfigurer#addViewControllers 做映射到具体的页面，也可以写一个控制器返回login_page视图层 *
                .loginProcessingUrl("/login") // 可以使用页面登录，也可直接访问接口登录，https://localhost/login?name=admin&passwd=123456
                .usernameParameter("username") // ****默认的是 username
                .passwordParameter("password") // **** 默认是 password
                .successHandler((request, response, auth) -> {
                    Object principal = auth.getPrincipal();
                    response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                    PrintWriter out = response.getWriter();
                    response.setStatus(200);
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", 200);
                    map.put("data", principal);
                    map.put("msg", "登录成功！");
                    out.write(JSON.toJSONString(map));
                    out.flush();
                    out.close();
                }) // **** 认证成功的处理方式 AuthenticationSuccessHandler
                .failureHandler((servletRequest, servletResponse, e) -> {
                    servletResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                    PrintWriter out = servletResponse.getWriter();
                    servletResponse.setStatus(401);
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", 401);
                    if (e instanceof LockedException) {
                        map.put("msg", "账户被锁定，登录失败！");
                    } else if (e instanceof BadCredentialsException) {
                        map.put("msg", "账户名或密码输入错误，登录失败！");
                    } else if (e instanceof DisabledException) {
                        map.put("msg", "账户被禁用，登录失败");
                    } else if (e instanceof AccountExpiredException) {
                        map.put("msg", "战鼓已过期，登录失败！");
                    } else if (e instanceof CredentialsExpiredException) {
                        map.put("msg", "密码已过期，登录失败！");
                    } else {
                        map.put("msg", "登录失败！");
                    }

                    out.write(JSON.toJSONString(map));
                    out.flush();
                    out.close();
                }) // **** 认证失败的处理方式 AuthenticationFailureHandler
                .permitAll() // 对登录相关的接口不需要认证即可访问
                .and()
                .logout() // **** 注销配置
                .logoutUrl("/logout") // 注销URL
                .clearAuthentication(true) // 清除身份认证信息，默认为true
                .invalidateHttpSession(true) // 使session失效，默认为true
                .addLogoutHandler(
                        // 完成数据清理的工作。spring security 默认提供了
                        (httpServletRequest, httpServletResponse, authentication) -> System.out.println("退出时的清理工作！！！！")
                ) // 注销的处理句柄 LogoutHandler
                .logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
//                    httpServletResponse.sendRedirect("/login_page"); // 跳转到登录页面，也可以在这些实现其他注销业务，返回json数据
                }) // 注销成功的处理句柄 LogoutSuccessHandler
                .and()
                .csrf()
                .disable(); // 关闭 csrf
*/
    }
}
