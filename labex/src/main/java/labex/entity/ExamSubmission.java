package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_exam_submission")
public class ExamSubmission {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer examId;
    private Integer studentId;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private BigDecimal totalScore;
    private Integer status;
}
