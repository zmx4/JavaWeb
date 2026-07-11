package shop.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WrongWordBookResponse {

    private Long id;

    private String questionType;

    private String project;

    private String word;

    private String correctTranslation;

    private String userAnswer;

    private LocalDateTime createTime;
}
