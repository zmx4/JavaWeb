package shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TestSubmitRequest {

    @NotBlank(message = "请选择测试项目")
    private String project;

    @NotEmpty(message = "请先加载测试题目")
    @Valid
    private List<TestAnswerRequest> answers;
}