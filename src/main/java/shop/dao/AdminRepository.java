package shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shop.entity.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
}
