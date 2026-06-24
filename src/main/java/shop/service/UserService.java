package shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import shop.dto.LoginRequest;
import shop.dto.RegisterRequest;
import shop.entity.User;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     */
    User login(LoginRequest request);
}
