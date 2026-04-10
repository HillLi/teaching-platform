package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_ex3")
public class Ex3 {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer no;
    private String name;
    private Integer extype;
    private Integer type;
    private String description;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
}
