package entry;

import lombok.Data;

import java.util.List;

/**
 * @author xie.xinquan
 * @create 2022/5/9 16:53
 * @company 深圳亿起融网络科技有限公司
 */
@Data
public class ModifyInfo {

    private String type;

    private String targetPath;

    private List<DeleteHeader> deleteHeaders;

    private List<AddHeader> addHeaders;

    private List<ModifyHeader> modifyHeaders;

    private ModifyRequestBody modifyRequestBody;
}
