package labex.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String account;
    private String password;
    private int type; // 0=teacher, 1=student
}
