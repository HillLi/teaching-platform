package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_student_log")
public class StudentLog {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String account;
    private Integer type;
    private String info;
    private LocalDateTime time;
    private String ip;
}
