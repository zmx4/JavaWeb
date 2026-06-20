package shop.controller;

import shop.entity.Role;
import shop.entity.User;
import shop.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 显示登录页面
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    /**
     * 处理登录请求
     */
    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       Model model) {
        try {
            User user = userService.loginUser(username, password);
            session.setAttribute("currentUser", user);
            return "redirect:/home";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    /**
     * 显示注册页面
     */
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("roles", Role.values());
        return "register";
    }

    /**
     * 处理注册请求
     */
    @PostMapping("/register")
    public String register(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          @RequestParam(required = false) String email,
                          @RequestParam(defaultValue = "CUSTOMER") String role,
                          Model model) {
        // 验证密码是否一致
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的密码不一致");
            model.addAttribute("roles", Role.values());
            return "register";
        }

        try {
            Role userRole = Role.valueOf(role);
            userService.registerUser(username, password, email, userRole);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "无效的角色选择");
            model.addAttribute("roles", Role.values());
            return "register";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", Role.values());
            return "register";
        }
    }

    /**
     * 首页（需要登录）
     */
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);
        return "home";
    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
