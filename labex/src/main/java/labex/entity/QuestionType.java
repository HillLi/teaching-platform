package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_question_type")
public class QuestionType {
    @TableId(value = "type_id", type = IdType.INPUT)
    private Integer typeId;
    private String typeName;
}
