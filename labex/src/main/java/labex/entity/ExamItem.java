package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_exam_item")
public class ExamItem {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer examId;
    private Integer type;
    private String content;
    private String options;
    private String answer;
    private Integer score;
}
