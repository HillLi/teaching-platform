package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_experiment_item")
public class ExperimentItem {
    @TableId(value = "experiment_item_id", type = IdType.AUTO)
    private Integer experimentItemId;
    private Integer experimentItemNo;
    private String experimentItemName;
    private Integer experimentItemType;
    private String experimentItemContent;
    private Integer experimentId;
    private String experimentItemAnswer;
    private Integer experimentItemScore;
    private String filePath;
    private Integer state;
}
