package io.movetodata.news.library.models;

import io.movetodata.news.library.enums.NewsStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class NewsRequest {
    private String title;
    private String description;
    private Integer priority;
    private NewsStatus status;
    private Date expiration;
}
