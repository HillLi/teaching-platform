package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_exam")
public class Exam {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String name;
    private String description;
    private Integer duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer createdBy;
    private LocalDateTime time;
    private Boolean flag;
}
