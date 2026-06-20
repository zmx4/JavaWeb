package shop.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shop.entity.User;
import shop.service.AdminService;
import shop.service.UserService;

import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;

    public AdminController(UserService userService, AdminService adminService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public String userManagement(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!adminService.isAdmin(currentUser.getId())) {
            return "redirect:/home?error=forbidden";
        }

        Set<Long> adminUserIds = adminService.findAllAdminUserIds();
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("adminUserIds", adminUserIds);
        model.addAttribute("currentUserId", currentUser.getId());
        return "admin/users";
    }

    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Long userId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!adminService.isAdmin(currentUser.getId())) {
            return "redirect:/home?error=forbidden";
        }
        if (currentUser.getId().equals(userId)) {
            redirectAttributes.addFlashAttribute("error", "不能删除当前登录的管理员账号");
            return "redirect:/admin/users";
        }

        if (userService.findById(userId).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "用户不存在");
            return "redirect:/admin/users";
        }

        userService.deleteUserById(userId);
        redirectAttributes.addFlashAttribute("success", "用户删除成功");
        return "redirect:/admin/users";
    }
}
