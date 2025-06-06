package io.bosler.news.library.repository;

import io.bosler.news.library.enums.NewsStatus;
import io.bosler.news.library.models.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface NewsRepository extends JpaRepository<News,String> {
    List<News> findAllByStatusAndExpirationGreaterThanOrderByPriorityAscCreatedAtDesc(NewsStatus status, Date expiration);

    News getReferenceById(UUID id);
}
