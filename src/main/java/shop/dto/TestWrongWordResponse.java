package shop.dto;

import lombok.Data;

@Data
public class TestWrongWordResponse {

    private String questionType;

    private String word;

    private String userAnswer;

    private String correctTranslation;
}