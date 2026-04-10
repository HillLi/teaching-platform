package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_student_item_log")
public class StudentItemLog {
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;
    private Integer studentItem;
    private String content;
    private LocalDateTime fillTime;
}
