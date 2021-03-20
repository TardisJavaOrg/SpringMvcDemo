package com.huixiong.web;

import com.huixiong.entity.User;
import com.huixiong.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

@Controller
@RequestMapping("/")
public class UserController {
    public static final String KEY_USER = "key_user";
    @Autowired
    UserService userService;

    @GetMapping("/index")
    public String index() {
        return "index.html";
    }

    @PostMapping("/login")
    public ModelAndView login(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session
    ) {
        User user = null;
        try {
            user = userService.login(email, password);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return new ModelAndView("redirect:/index");
        }
        System.out.println(user.getName());
        return new ModelAndView("profile.html");
    }

    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleUnknowException(Exception e) {
        return new ModelAndView("500.html", new HashMap<String, String>() {{
            put("error", e.getClass().getSimpleName());
            put("message", e.getClass().getSimpleName());
        }});
    }
}
