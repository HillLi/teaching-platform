package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_student")
public class Student {
    @TableId(value = "student_id", type = IdType.AUTO)
    private Integer studentId;
    private String studentNo;
    private String studentName;
    private String studentPassword;
    private String clazzNo;
    private String memo;
    private Integer state;
    private Integer error;
    private String ip;
}
