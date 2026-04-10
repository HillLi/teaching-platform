package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_student_excercise")
public class StudentExercise {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Integer itemId;
    private Integer studentId;
    private String answer;
    private String content;
    private Integer score;
    private LocalDateTime fillTime;
}
