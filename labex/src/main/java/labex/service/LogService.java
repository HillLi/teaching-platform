package labex.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import labex.entity.StudentLog;
import labex.entity.SysLog;
import labex.mapper.StudentLogMapper;
import labex.mapper.SysLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {

    private final SysLogMapper sysLogMapper;
    private final StudentLogMapper studentLogMapper;

    public LogService(SysLogMapper sysLogMapper, StudentLogMapper studentLogMapper) {
        this.sysLogMapper = sysLogMapper;
        this.studentLogMapper = studentLogMapper;
    }

    public Page<SysLog> listSysLogs(String account, Integer type, int pageNum, int pageSize) {
        QueryWrapper<SysLog> qw = new QueryWrapper<>();
        if (account != null && !account.isEmpty()) {
            qw.like("account", account);
        }
        if (type != null) {
            qw.eq("type", type);
        }
        qw.orderByDesc("time");
        return sysLogMapper.selectPage(new Page<>(pageNum, pageSize), qw);
    }

    public List<StudentLog> listStudentLogs(Integer studentId) {
        QueryWrapper<StudentLog> qw = new QueryWrapper<>();
        if (studentId != null) {
            qw.inSql("account", "SELECT student_no FROM t_student WHERE student_id = " + studentId);
        }
        qw.orderByDesc("time");
        return studentLogMapper.selectList(qw);
    }
}
