package shop.service;

import shop.dto.TestAnswerRequest;
import shop.dto.TestQuestionResponse;
import shop.dto.TestSubmitResponse;
import shop.dto.WrongWordBookResponse;

import java.util.List;

public interface TestService {

    List<TestQuestionResponse> generateQuestions(String project, int count);

    TestSubmitResponse submit(String project, Long userId, List<TestAnswerRequest> answers);

    List<WrongWordBookResponse> getWrongWords(Long userId, String project);

    List<TestQuestionResponse> generateWrongWordQuestions(Long userId, String project, int count);
}