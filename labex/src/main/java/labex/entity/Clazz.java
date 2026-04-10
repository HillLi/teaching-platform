package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_clazz")
public class Clazz {
    @TableId(value = "no", type = IdType.INPUT)
    private String no;
    private String memo;
    private Integer state;
}
