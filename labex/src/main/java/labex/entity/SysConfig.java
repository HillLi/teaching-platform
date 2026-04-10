package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_sys_config")
public class SysConfig {
    @TableId(value = "param", type = IdType.INPUT)
    private String param;
    private String value;
}
