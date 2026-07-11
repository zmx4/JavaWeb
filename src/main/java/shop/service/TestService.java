package shop.service;

import shop.dto.TestAnswerRequest;
import shop.dto.TestQuestionResponse;
import shop.dto.TestSubmitResponse;

import java.util.List;

public interface TestService {

    List<TestQuestionResponse> generateQuestions(String project, int count);

    TestSubmitResponse submit(String project, Long userId, List<TestAnswerRequest> answers);
}