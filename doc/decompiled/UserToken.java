// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.model.UserToken
// Session-scoped authentication token model
// ============================================================
package labex.model;

import java.io.Serializable;

/**
 * UserToken is stored in the HTTP session to represent an authenticated user.
 * It is created upon successful login and validated by SecurityInterceptor
 * on every subsequent request.
 *
 * Fields correspond to data from both t_student and t_teacher tables,
 * as this platform supports both student and teacher logins.
 *
 * Stored as session attribute "userToken".
 */
public class UserToken implements Serializable {

    /** User type: 0 = teacher, 1 = student */
    private int userType;

    /** Database primary key (student_id or teacher_id) */
    private int userId;

    /** Login account (student_no for students, teacher_account for teachers) */
    private String account;

    /** Display name (student_name or teacher_name) */
    private String userName;

    /** For students: class number (clazz_no). Null for teachers. */
    private String clazzNo;

    /** IP address at the time of login, for session binding */
    private String ip;

    /** Account state flag (from t_student.state or always 1 for teachers) */
    private int state;

    /** Login error count (from t_student.error) */
    private int error;

    public UserToken() {
    }

    // ---- Getters and Setters ----

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getClazzNo() {
        return clazzNo;
    }

    public void setClazzNo(String clazzNo) {
        this.clazzNo = clazzNo;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    // ---- Utility Methods ----

    /**
     * Check if this account is locked due to too many failed login attempts.
     */
    public boolean isAccountLocked() {
        return error > 5; // Threshold for account lockout
    }

    /**
     * Check if this user is a teacher.
     */
    public boolean isTeacher() {
        return userType == 0;
    }

    /**
     * Check if this user is a student.
     */
    public boolean isStudent() {
        return userType == 1;
    }
}
