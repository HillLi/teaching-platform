package labex.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import labex.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {
}
