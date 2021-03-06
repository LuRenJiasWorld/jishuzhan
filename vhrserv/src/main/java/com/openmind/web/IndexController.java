package com.openmind.web;

import com.openmind.annotation.AccessLimit;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * IndexController
 *
 * @author zhoujunwen
 * @date 2020-01-06
 * @time 20:54
 * @desc
 */
@Controller
@RequestMapping("/")
public class IndexController {
    @AccessLimit(limit = 100, sec = 10)
    @RequestMapping("/index")
    public String index() {
        return "index";
    }

}
