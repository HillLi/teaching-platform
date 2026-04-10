package labex.dto;

import lombok.Data;

@Data
public class UserTokenVO {
    private int userType; // 0=teacher, 1=student
    private Integer userId;
    private String account;
    private String userName;
    private String clazzNo;
    private String ip;
    private Integer state;
    private Integer error;

    public boolean isAccountLocked() {
        return error != null && error > 5;
    }
}
