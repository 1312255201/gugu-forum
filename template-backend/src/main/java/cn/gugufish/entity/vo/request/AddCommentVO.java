package cn.gugufish.entity.vo.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AddCommentVO {
    @Min(1)
    int tid;
    String content;
    @Min(-1)
    int quote;
}