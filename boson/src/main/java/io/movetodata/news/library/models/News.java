package io.movetodata.news.library.models;

import io.movetodata.news.library.enums.NewsStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "home_news")
public class News {
    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(columnDefinition = "TEXT")
    public String title;
    @Column(columnDefinition = "TEXT")
    public String description;

    @Enumerated(EnumType.STRING)
    public NewsStatus status;

    @Column(nullable = false)
    public Integer priority;

    public Date expiration;

    public Date createdAt = new Date();
    public Date updatedAt = new Date();
    public UUID createdBy;
    public UUID updatedBy;
}
