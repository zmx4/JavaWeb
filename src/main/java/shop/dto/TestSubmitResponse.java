package shop.dto;

import lombok.Data;

import java.util.List;

@Data
public class TestSubmitResponse {

    private String project;

    private int totalCount;

    private int correctCount;

    private int wrongCount;

    private int savedCount;

    private List<TestWrongWordResponse> wrongWords;

    private String redirectUrl;
}