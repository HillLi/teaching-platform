package labex.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import labex.common.BusinessException;
import labex.dto.LoginRequest;
import labex.dto.SessionUtil;
import labex.dto.UserTokenVO;
import labex.entity.Student;
import labex.entity.SysLog;
import labex.entity.Teacher;
import labex.mapper.StudentMapper;
import labex.mapper.SysLogMapper;
import labex.mapper.TeacherMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final SysLogMapper sysLogMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(TeacherMapper teacherMapper, StudentMapper studentMapper, SysLogMapper sysLogMapper) {
        this.teacherMapper = teacherMapper;
        this.studentMapper = studentMapper;
        this.sysLogMapper = sysLogMapper;
    }

    public UserTokenVO login(LoginRequest req, HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        if (req.getType() == 0) {
            return loginTeacher(req, ip);
        } else if (req.getType() == 1) {
            return loginStudent(req, ip);
        } else {
            throw new BusinessException("无效的用户类型");
        }
    }

    private UserTokenVO loginTeacher(LoginRequest req, String ip) {
        Teacher teacher = teacherMapper.selectOne(
                new QueryWrapper<Teacher>().eq("teacher_account", req.getAccount()));
        if (teacher == null) {
            logSys(req.getAccount(), 2, "登录失败-账号不存在", ip);
            throw new BusinessException("账号或密码错误");
        }

        if (!checkPassword(req.getPassword(), teacher.getTeacherPassword())) {
            logSys(req.getAccount(), 2, "登录失败-密码错误", ip);
            throw new BusinessException("账号或密码错误");
        }

        logSys(req.getAccount(), 1, "登录成功", ip);

        // Upgrade password to BCrypt if it was MD5
        if (!teacher.getTeacherPassword().startsWith("$2a$")) {
            teacher.setTeacherPassword(passwordEncoder.encode(req.getPassword()));
            teacherMapper.updateById(teacher);
        }

        UserTokenVO token = new UserTokenVO();
        token.setUserType(0);
        token.setUserId(teacher.getTeacherId());
        token.setAccount(teacher.getTeacherAccount());
        token.setUserName(teacher.getTeacherName());
        token.setIp(ip);
        token.setState(1); // Teachers always active
        token.setError(0);
        return token;
    }

    private UserTokenVO loginStudent(LoginRequest req, String ip) {
        Student student = studentMapper.selectOne(
                new QueryWrapper<Student>().eq("student_no", req.getAccount()));
        if (student == null) {
            logSys(req.getAccount(), 2, "登录失败-账号不存在", ip);
            throw new BusinessException("账号或密码错误");
        }

        if (!checkPassword(req.getPassword(), student.getStudentPassword())) {
            // Increment error count
            student.setError(student.getError() != null ? student.getError() + 1 : 1);
            studentMapper.updateById(student);
            logSys(req.getAccount(), 2, "登录失败-密码错误", ip);
            throw new BusinessException("账号或密码错误");
        }

        // Update last IP
        student.setIp(ip);
        studentMapper.updateById(student);

        logSys(req.getAccount(), 1, "登录成功", ip);

        // Upgrade password to BCrypt if it was MD5
        if (!student.getStudentPassword().startsWith("$2a$")) {
            student.setStudentPassword(passwordEncoder.encode(req.getPassword()));
            studentMapper.updateById(student);
        }

        UserTokenVO token = new UserTokenVO();
        token.setUserType(1);
        token.setUserId(student.getStudentId());
        token.setAccount(student.getStudentNo());
        token.setUserName(student.getStudentName());
        token.setClazzNo(student.getClazzNo());
        token.setIp(ip);
        token.setState(student.getState());
        token.setError(0);
        return token;
    }

    private boolean checkPassword(String rawPassword, String storedPassword) {
        // Try BCrypt first
        if (storedPassword.startsWith("$2a$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        // Fall back to MD5
        String md5 = DigestUtils.md5Hex(rawPassword);
        return md5.equalsIgnoreCase(storedPassword);
    }

    private void logSys(String account, int type, String info, String ip) {
        SysLog log = new SysLog();
        log.setAccount(account);
        log.setType(type);
        log.setInfo(info);
        log.setIp(ip);
        sysLogMapper.insert(log);
    }
}
