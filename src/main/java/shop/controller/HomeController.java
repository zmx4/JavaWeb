package shop.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import shop.entity.DictionaryEntry;
import shop.entity.User;
import shop.service.DictionaryService;

@Controller
public class HomeController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DictionaryService dictionaryService;

    public HomeController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/user/login";
        }
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("currentTime", LocalDateTime.now().format(TIME_FORMATTER));
        return "index";
    }

    @GetMapping("/dictionary")
    public String dictionary(@RequestParam(value = "word", required = false) String word,
                             HttpSession session,
                             Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("searchWord", StringUtils.hasText(word) ? word.trim() : "");

        if (StringUtils.hasText(word)) {
            DictionaryEntry entry = dictionaryService.findByWord(word);
            if (entry != null) {
                model.addAttribute("dictionaryEntry", entry);
            } else {
                model.addAttribute("error", "未找到该单词");
            }
        }

        return "dictionary";
    }
}
