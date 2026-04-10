package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_lecture")
public class Lecture {
    @TableId(value = "lecture_id", type = IdType.AUTO)
    private Integer lectureId;
    private String lectureName;
    private Integer lectureType;
    private String lectureFiletype;
}
