package shop.huanting.controller;

import com.alibaba.fastjson.JSON;
import entry.ModifyInfo;
import entry.SendData;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import shop.huanting.server.ClientReceive;
import shop.huanting.server.WebSocketServer;

import javax.annotation.Resource;
import java.util.Map;

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

    @GetMapping("/getPathDetail")
    public SendData getPathDetail(@RequestParam("path") String path) {
        Map<String, SendData> pathDetailMap = WebSocketServer.pathDetailMap;
        SendData sendData = pathDetailMap.get(path);
        return sendData;
    }
}
