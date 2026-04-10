package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_student_question")
public class StudentQuestion {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer studentId;
    private Integer questionId;
    private String answer;
}
