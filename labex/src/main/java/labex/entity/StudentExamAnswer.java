package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_student_exam_answer")
public class StudentExamAnswer {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer examItemId;
    private Integer studentId;
    private String answer;
    private String content;
    private String filePath;
    private Integer score;
    private Integer autoScored;
    private LocalDateTime submitTime;
}
