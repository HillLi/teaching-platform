package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_assistant")
public class Assistant {
    @TableId(value = "assistant_account", type = IdType.INPUT)
    private String assistantAccount;
    private String assistantPassword;
    private String assistantStudentNo;
    private String assistantStudentName;
    private String assistantStudentClazz;
}
