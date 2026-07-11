package shop.dto;

import lombok.Data;

@Data
public class TestAnswerRequest {

    private String questionId;

    private String questionType;

    private String word;

    private String answer;
}