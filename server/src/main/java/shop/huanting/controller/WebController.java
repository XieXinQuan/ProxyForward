package shop.huanting.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xie.xinquan
 * @create 2022/5/7 9:52
 * @company 深圳亿起融网络科技有限公司
 */
@RestController
public class WebController {

    @GetMapping("/")
    public String welcome() {
        return "Hello World！！！";
    }
}
