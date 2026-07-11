package shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import shop.entity.DictionaryEntry;

public interface DictionaryService extends IService<DictionaryEntry> {

    DictionaryEntry findByWord(String word);
}
