package labex.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_exam_clazz")
public class ExamClazz {
    private Integer examId;
    private String clazzNo;
}
