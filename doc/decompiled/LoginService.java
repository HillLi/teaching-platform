// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.service.LoginService
// Inner classes: StudentRowMapper, UserRowMapper
// ============================================================
package labex.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import labex.common.exception.*;
import labex.model.UserToken;

/**
 * LoginService handles authentication for both students and teachers.
 *
 * Database tables used:
 *   t_teacher: (teacher_id, teacher_account, teacher_password[MD5], teacher_name)
 *   t_student: (student_id, student_no, student_password[MD5], student_name,
 *               clazz_no, memo, state, error, ip)
 *   t_sys_log: (id, account, type[1=success,2=failure], info, time, ip)
 *
 * Passwords are stored as MD5 hashes (char(32) in database).
 */
@Service
public class LoginService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public LoginService() {
    }

    /**
     * Authenticate a user and return a UserToken on success.
     *
     * @param account  The login account (student_no or teacher_account)
     * @param password The raw password (will be MD5-hashed for comparison)
     * @param type     0=teacher, 1=student
     * @param ip       The remote IP address
     * @return UserToken on successful authentication
     * @throws LoginFailedException if credentials don't match
     * @throws LoginForbiddenException if account is disabled
     */
    public UserToken login(String account, String password, int type, String ip) {
        String md5Password = md5(password);

        if (type == 0) {
            // Teacher login
            return loginTeacher(account, md5Password, ip);
        } else {
            // Student login
            return loginStudent(account, md5Password, ip);
        }
    }

    /**
     * Authenticate a teacher against t_teacher table.
     */
    private UserToken loginTeacher(String account, String md5Password, String ip) {
        try {
            String sql = "SELECT teacher_id, teacher_account, teacher_name " +
                         "FROM t_teacher WHERE teacher_account = ? AND teacher_password = ?";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, account, md5Password);

            UserToken token = new UserToken();
            token.setUserType(0);
            token.setUserId(((Number) row.get("teacher_id")).intValue());
            token.setAccount((String) row.get("teacher_account"));
            token.setUserName((String) row.get("teacher_name"));
            token.setIp(ip);
            token.setState(1); // Teachers always active
            return token;
        } catch (EmptyResultDataAccessException e) {
            throw new LoginFailedException();
        }
    }

    /**
     * Authenticate a student against t_student table.
     * Also checks state (disabled) and updates the IP address.
     */
    private UserToken loginStudent(String account, String md5Password, String ip) {
        try {
            String sql = "SELECT student_id, student_no, student_name, clazz_no, " +
                         "state, error FROM t_student " +
                         "WHERE student_no = ? AND student_password = ?";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, account, md5Password);

            int state = row.get("state") != null ?
                    ((Number) row.get("state")).intValue() : 0;
            int error = row.get("error") != null ?
                    ((Number) row.get("error")).intValue() : 0;

            // Check if account is disabled
            if (state == 0) {
                // Reset error count on successful login
                jdbcTemplate.update(
                    "UPDATE t_student SET ip = ?, error = 0 WHERE student_no = ?",
                    ip, account);
                throw new LoginForbiddenException();
            }

            // Reset error count on successful login
            int studentId = ((Number) row.get("student_id")).intValue();
            jdbcTemplate.update(
                "UPDATE t_student SET ip = ?, error = 0 WHERE student_id = ?",
                ip, studentId);

            UserToken token = new UserToken();
            token.setUserType(1);
            token.setUserId(studentId);
            token.setAccount((String) row.get("student_no"));
            token.setUserName((String) row.get("student_name"));
            token.setClazzNo((String) row.get("clazz_no"));
            token.setIp(ip);
            token.setState(state);
            token.setError(0);
            return token;
        } catch (LoginForbiddenException e) {
            throw e;
        } catch (EmptyResultDataAccessException e) {
            // Increment error count for the account
            incrementErrorCount(account);
            throw new LoginFailedException();
        }
    }

    /**
     * Increment the failed login counter for a student account.
     * If error count exceeds threshold, disable the account.
     */
    private void incrementErrorCount(String account) {
        jdbcTemplate.update(
            "UPDATE t_student SET error = COALESCE(error, 0) + 1 " +
            "WHERE student_no = ?", account);
    }

    /**
     * Log a login/logout event to t_sys_log.
     *
     * @param account The user account
     * @param type    1=success, 2=failure
     * @param info    Description (e.g., "登录成功", "登录失败", "您已成功注销！")
     * @param ip      Remote IP address
     */
    public void logLogin(String account, int type, String info, String ip) {
        jdbcTemplate.update(
            "INSERT INTO t_sys_log (account, type, info, time, ip) VALUES (?, ?, ?, NOW(), ?)",
            account, type, info, ip);
    }

    /**
     * Simple MD5 hash utility.
     * Passwords in the database are stored as MD5 hex strings (char(32)).
     */
    private String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 hashing failed", e);
        }
    }

    // ---- Inner Classes: Row Mappers ----

    /**
     * RowMapper for mapping t_student rows to a simple Map/DTO.
     * Maps columns: student_id, student_no, student_name, student_password,
     *               clazz_no, memo, state, error, ip
     */
    public static class StudentRowMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Map.of(
                "student_id", rs.getInt("student_id"),
                "student_no", rs.getString("student_no"),
                "student_name", rs.getString("student_name"),
                "student_password", rs.getString("student_password"),
                "clazz_no", rs.getString("clazz_no"),
                "memo", rs.getString("memo"),
                "state", rs.getInt("state"),
                "error", rs.getInt("error"),
                "ip", rs.getString("ip")
            );
        }
    }

    /**
     * RowMapper for mapping t_teacher rows to a simple Map/DTO.
     * Maps columns: teacher_id, teacher_account, teacher_password, teacher_name
     */
    public static class UserRowMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Map.of(
                "teacher_id", rs.getInt("teacher_id"),
                "teacher_account", rs.getString("teacher_account"),
                "teacher_password", rs.getString("teacher_password"),
                "teacher_name", rs.getString("teacher_name")
            );
        }
    }
}
