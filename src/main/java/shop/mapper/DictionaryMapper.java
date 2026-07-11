package shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import shop.entity.DictionaryEntry;

@Mapper
public interface DictionaryMapper extends BaseMapper<DictionaryEntry> {
}
