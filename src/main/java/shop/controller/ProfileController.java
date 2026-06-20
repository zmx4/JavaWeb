package shop.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shop.entity.User;
import shop.service.UserService;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 显示个人信息维护页面
     */
    @GetMapping
    public String showProfile(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        // 从数据库重新加载最新数据
        User freshUser = userService.findById(currentUser.getId()).orElse(currentUser);
        session.setAttribute("currentUser", freshUser);
        model.addAttribute("user", freshUser);
        return "profile";
    }

    /**
     * 处理个人信息修改
     */
    @PostMapping
    public String updateProfile(@RequestParam String username,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) String confirmPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // 如果填写了新密码，校验两次输入是否一致
        if (newPassword != null && !newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "两次输入的新密码不一致");
                return "redirect:/profile";
            }
        }

        try {
            User updatedUser = userService.updateUser(currentUser.getId(), username, email, newPassword);
            // 更新 session 中的用户信息
            session.setAttribute("currentUser", updatedUser);
            redirectAttributes.addFlashAttribute("success", "个人信息修改成功");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }
}
