package shop.huanting.controller;

import entry.ModifyInfo;
import org.springframework.web.bind.annotation.*;
import shop.huanting.server.ClientReceive;
import shop.huanting.server.WebSocketServer;

import javax.annotation.Resource;

/**
 * @author xie.xinquan
 * @create 2022/5/7 9:52
 * @company 深圳亿起融网络科技有限公司
 */
@RestController
public class WebController {

    @Resource
    private ClientReceive clientReceive;

    @GetMapping("/")
    public String welcome() {
        return "Hello World！！！";
    }

    @PostMapping("/addRule")
    public String addRule(@RequestBody ModifyInfo modifyInfo) {
        modifyInfo.setType("addRule");
        clientReceive.sendMsg(modifyInfo);
        return "Success";
    }

    @PostMapping("/removeRule")
    public String removeRule(@RequestParam("path") String path) {
        ModifyInfo modifyInfo = new ModifyInfo();
        modifyInfo.setType("removeRule");
        modifyInfo.setTargetPath(path);
        clientReceive.sendMsg(modifyInfo);
        return "Success";
    }
}
