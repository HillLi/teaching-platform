package labex.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import labex.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    List<Map<String, Object>> selectClazzExperimentScore(@Param("clazzNo") String clazzNo, @Param("expId") int expId);

    List<Map<String, Object>> selectStudentExperimentScore(@Param("studentId") int studentId);

    List<Map<String, Object>> selectStudentExperimentItemScore(@Param("studentId") int studentId, @Param("expId") int expId);

    List<Map<String, Object>> selectClazzExperimentAnswers(@Param("expId") int expId, @Param("clazzNo") String clazzNo);
}
