package shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dictionary")
public class DictionaryEntry {

    @TableId(value = "word", type = IdType.INPUT)
    private String word;

    private String translation;
}
