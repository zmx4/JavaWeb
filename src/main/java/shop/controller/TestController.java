package shop.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.stereotype.Controller;
import shop.common.Result;
import shop.dto.TestQuestionResponse;
import shop.dto.TestSubmitRequest;
import shop.dto.TestSubmitResponse;
import shop.entity.User;
import shop.service.TestService;

import java.util.List;

@Controller
@RequestMapping("/test")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping
    public String testPage(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("activeProject", "CET4");
        return "test";
    }

    @GetMapping("/result")
    public String resultPage(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/user/login";
        }

        TestSubmitResponse latestResult = (TestSubmitResponse) session.getAttribute("latestTestResult");
        if (latestResult == null) {
            return "redirect:/test";
        }

        model.addAttribute("latestResult", latestResult);
        model.addAttribute("wrongWords", latestResult.getWrongWords());
        return "test-result";
    }

    @GetMapping("/questions")
    @ResponseBody
    public Result<List<TestQuestionResponse>> questions(@RequestParam("project") String project,
                                                        @RequestParam(value = "count", defaultValue = "10") int count,
                                                        HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return Result.error(401, "请先登录");
        }

        try {
            return Result.success("加载成功", testService.generateQuestions(project, count));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/submit")
    @ResponseBody
    public Result<TestSubmitResponse> submit(@Valid @RequestBody TestSubmitRequest request,
                                             BindingResult bindingResult,
                                             HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return Result.error(401, "请先登录");
        }

        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        try {
            TestSubmitResponse response = testService.submit(request.getProject(), loginUser.getId(), request.getAnswers());
            session.setAttribute("latestTestResult", response);
            return Result.success("测试数据已保存", response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}