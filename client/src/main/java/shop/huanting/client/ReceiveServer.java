package shop.huanting.client;

import entry.ModifyInfo;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xie.xinquan
 * @create 2022/5/9 16:23
 * @company 深圳亿起融网络科技有限公司
 */
@Component
public class ReceiveServer {

    private Map<String, ModifyInfo> modifyInfoMap = new ConcurrentHashMap<>();

    public void addRule(String path, ModifyInfo modifyInfo) {
        this.modifyInfoMap.put(path, modifyInfo);
    }

    public void removeRule(String path) {
        this.modifyInfoMap.remove(path);
    }

    public ModifyInfo getModifyInfo(String path) {
        return modifyInfoMap.get(path);
    }

}
