package shop.dto;

import lombok.Data;

import java.util.List;

@Data
public class TestQuestionResponse {

    private String questionId;

    private String questionType;

    private String word;

    private String prompt;

    private List<String> options;
}