package labex.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExamSubmitDTO {
    private List<ExamSubmitItemDTO> answers;
}
