package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_experiment")
public class Experiment {
    @TableId(value = "experiment_id", type = IdType.AUTO)
    private Integer experimentId;
    private Integer experimentNo;
    private String experimentName;
    private Integer experimentType;
    private String instructionType;
    private String experimentRequirement;
    private String experimentContent;
    private Integer state;
}
