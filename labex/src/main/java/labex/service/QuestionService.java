package labex.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import labex.entity.Question;
import labex.entity.QuestionType;
import labex.mapper.QuestionMapper;
import labex.mapper.QuestionTypeMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionTypeMapper questionTypeMapper;
    private final JdbcTemplate jdbcTemplate;

    public QuestionService(QuestionMapper questionMapper, QuestionTypeMapper questionTypeMapper, JdbcTemplate jdbcTemplate) {
        this.questionMapper = questionMapper;
        this.questionTypeMapper = questionTypeMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Question> listQuestions() {
        return questionMapper.selectList(new QueryWrapper<>());
    }

    public void addQuestion(Question q) {
        // t_question uses subquery-based auto-increment, not standard AI
        if (q.getId() == null) {
            Integer maxId = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(MAX(id), 0) + 1 FROM t_question", Integer.class);
            q.setId(maxId);
        }
        questionMapper.insert(q);
    }

    public void updateQuestion(Question q) {
        questionMapper.updateById(q);
    }

    public void deleteQuestion(Integer id) {
        questionMapper.deleteById(id);
    }

    public List<QuestionType> listTypes() {
        return questionTypeMapper.selectList(new QueryWrapper<>());
    }
}
