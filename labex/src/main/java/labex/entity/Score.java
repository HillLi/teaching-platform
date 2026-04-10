package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_score")
public class Score {
    @TableId(value = "score_id", type = IdType.AUTO)
    private Integer scoreId;
    private Integer studentId;
    private Integer experimentId;
    private Integer score;
}
