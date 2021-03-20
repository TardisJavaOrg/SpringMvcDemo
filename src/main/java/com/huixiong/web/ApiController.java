package com.huixiong.web;


import com.huixiong.entity.User;
import com.huixiong.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用 RestController 替代Controller，只返回和获取json，作为REST API使用
 */
//@CrossOrigin(origins = "http://local.huixiong.com:8080") // 允许指定网站跨域访问
//@CrossOrigin(origins = "*") // 允许所有网站跨域访问
@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    UserService userService;

    @GetMapping("/users")
    public List<User> users() {
        return userService.getUsers();
    }

    @PostMapping("/users/{id}")
    public User user(@PathVariable("id") long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/signin")
    public Map<String, Object> signin(@RequestBody SignInRequest signInRequest) {
        try {
            User user = userService.signin(signInRequest.email, signInRequest.password);
            return new HashMap<String, Object>() {{
                put("user", user);
            }};
        } catch (Exception e) {
            return new HashMap<String, Object>() {
                {
                    put("error", "SIGNIN_FAIL");
                    put("message", e.getMessage());
                }
            };
        }
    }

    public static class SignInRequest {
        public String email;
        public String password;
    }
}
