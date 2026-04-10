package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_question")
public class Question {
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;
    private String question;
    private String answer;
    private Integer type;
}
