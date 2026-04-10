package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_teacher")
public class Teacher {
    @TableId(value = "teacher_id", type = IdType.AUTO)
    private Integer teacherId;
    private String teacherAccount;
    private String teacherPassword;
    private String teacherName;
}
