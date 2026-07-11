package shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_record")
public class TestRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("question_type")
    private String questionType;

    private String project;

    private String word;

    @TableField("correct_translation")
    private String correctTranslation;

    @TableField("user_answer")
    private String userAnswer;

    @TableField("is_correct")
    private Boolean correct;

    @TableField("create_time")
    private LocalDateTime createTime;
}