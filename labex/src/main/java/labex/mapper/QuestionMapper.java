package labex.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import labex.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
