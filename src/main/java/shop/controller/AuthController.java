package shop.controller;

import shop.entity.User;
import shop.service.AdminService;
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

    private final AdminService adminService;

    public AuthController(UserService userService, AdminService adminService) {
        this.userService = userService;
        this.adminService = adminService;
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
    public String showRegisterPage() {
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
                          Model model) {
        // 验证密码是否一致
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的密码不一致");
            return "register";
        }

        try {
            userService.registerUser(username, password, email);
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
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
        model.addAttribute("userIsAdmin", adminService.isAdmin(currentUser.getId()));
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
