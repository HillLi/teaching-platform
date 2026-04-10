package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_student_answer")
public class StudentAnswer {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Integer itemId;
    private Integer fillNo;
    private String content;
    private String contentHash;
    private Integer count;
    private Boolean isCorrect;
}
