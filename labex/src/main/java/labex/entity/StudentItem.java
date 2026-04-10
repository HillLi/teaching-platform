package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_student_item")
public class StudentItem {
    @TableId(value = "student_item_id", type = IdType.AUTO)
    private Integer studentItemId;
    private Integer studentId;
    private Integer itemId;
    private String content;
    private Integer score;
    private LocalDateTime fillTime;
    private Integer scoreFlag;
}
