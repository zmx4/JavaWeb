package shop.service;

import org.springframework.stereotype.Service;
import shop.dao.AdminRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        return adminRepository.existsById(userId);
    }

    public Set<Long> findAllAdminUserIds() {
        return adminRepository.findAll()
                .stream()
                .map(admin -> admin.getUserId())
                .collect(Collectors.toSet());
    }
}
