package shop.huanting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author xie.xinquan
 * @create 2022/4/22 9:52
 * @company 深圳亿起融网络科技有限公司
 */
@SpringBootApplication
public class MainApp {

    public static void main(String[] args) throws URISyntaxException {
        SpringApplication.run(MainApp.class, args);
//        URI uri = new URI("http://quan.shop");
    }
}
