package io.movetodata.news.library.repository;

import io.movetodata.news.library.enums.NewsStatus;
import io.movetodata.news.library.models.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface NewsRepository extends JpaRepository<News,String> {
    List<News> findAllByStatusAndExpirationGreaterThanOrderByPriorityAscCreatedAtDesc(NewsStatus status, Date expiration);

    News getReferenceById(UUID id);
}
