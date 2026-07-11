package shop.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ControllerAdvice;
import shop.entity.User;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("loginUser")
    public User loginUser(HttpSession session) {
        return (User) session.getAttribute("loginUser");
    }
}