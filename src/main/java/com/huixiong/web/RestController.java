package com.huixiong.web;

import com.huixiong.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/rest")
public class RestController {
    /**
     * 注解PostMapping：consumes表示接受MIME类型为json，produces表示返回类型为json
     * 注解ResponseBody：表示返回的String无需处理，作为内容直接写入HttpServletResponse中
     * 注解RequestBody： 表示输入的类型根据直接直接反序列化到User这个JavaBean中
     * 这种方式编写 REST API 比较麻烦
     * @param user
     * @return
     */
    @PostMapping(value = "/",
            consumes = "application/json;charset=UTF-8",
            produces = "application/json;charset=UTF-8"
    )
    @ResponseBody
    public String rest(@RequestBody User user) {
        return "{\"result\": \""+user.getEmail()+"\"}";
    }

}
