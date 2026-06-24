package shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import shop.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
