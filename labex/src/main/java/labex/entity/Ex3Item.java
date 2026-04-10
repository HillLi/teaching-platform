package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_ex3_item")
public class Ex3Item {
    @TableId(value = "excercise_item_id", type = IdType.AUTO)
    private Integer excerciseItemId;
    private Integer excerciseId;
    private String question;
    private String options;
    private String answer;
    private Integer type;
}
