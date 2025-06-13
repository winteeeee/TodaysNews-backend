package com.sig.todaysnews.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDto {
    private Long articleId;
    private String title;
    private String imgUrl;
    private String url;
    private String content;
    private String press;
    private LocalDateTime regdate;
}
