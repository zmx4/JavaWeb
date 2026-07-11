package shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import shop.entity.DictionaryEntry;
import shop.mapper.DictionaryMapper;
import shop.service.DictionaryService;

@Service
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, DictionaryEntry> implements DictionaryService {

    @Override
    public DictionaryEntry findByWord(String word) {
        if (!StringUtils.hasText(word)) {
            return null;
        }

        LambdaQueryWrapper<DictionaryEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictionaryEntry::getWord, word.trim());
        return this.getOne(wrapper);
    }
}
