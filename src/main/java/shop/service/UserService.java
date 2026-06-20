package shop.service;

import shop.dao.UserRepository;
import shop.entity.Role;
import shop.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    /**
     * 用户注册
     */
    @Transactional
    public User registerUser(String username, String password, String email, Role role) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (email != null && userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建新用户（实际项目中应该对密码进行加密）
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // TODO: 使用BCrypt加密密码
        user.setEmail(email);
        user.setRole(role != null ? role : Role.CUSTOMER);

        return userRepository.save(user);
    }

    /**
     * 用户登录
     */
    public User loginUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户名或密码错误");
        }

        User user = userOpt.get();
        
        // 验证密码（实际项目中应该使用BCrypt验证）
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }

        return user;
    }

    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 管理员查看所有用户
     */
    public List<User> findAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    /**
     * 根据ID查找用户
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * 管理员删除用户
     */
    @Transactional
    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }
}
