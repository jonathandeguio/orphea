package io.orphea.news.controllers;

import io.orphea.news.library.enums.NewsStatus;
import io.orphea.news.library.models.News;
import io.orphea.news.library.models.NewsRequest;
import io.orphea.news.library.repository.NewsRepository;
import io.orphea.passport.library.Auth;
import io.orphea.passport.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "News", description = "This is a News management controller.")
public class NewsController {
    private final NewsRepository newsRepository;
    @Operation(summary = "Get all news with priority and createdAt time")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllNews() {
        try {
            List<News> news = newsRepository.findAllByStatusAndExpirationGreaterThanOrderByPriorityAscCreatedAtDesc(NewsStatus.ACTIVE, new Date());
            return ResponseEntity.ok().body(news);
        } catch (Exception e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "create a latest news")
    @PostMapping("/latest")
    @PreAuthorize(Auth.PLATFORM_ADMIN)
    public ResponseEntity<Object> getAllNews(@AuthenticationPrincipal AuthUser authUser, @RequestBody NewsRequest latestNews) {
        try {
            News news = new News();
            news.setTitle(latestNews.getTitle());
            news.setPriority(latestNews.getPriority());
            news.setDescription(latestNews.getDescription());
            news.setCreatedAt(new Date());
            news.setCreatedBy(authUser.getId());
            news.setExpiration(latestNews.getExpiration());
            news.setStatus(NewsStatus.ACTIVE);
            newsRepository.save(news);
            return ResponseEntity.ok().body("Latest News Posted.");
        } catch (Exception e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "update latest news")
    @PutMapping("/edit/{id}")
    @PreAuthorize(Auth.PLATFORM_ADMIN)
    public ResponseEntity<Object> getAllNews(@AuthenticationPrincipal AuthUser authUser,
                                             @PathVariable("id") UUID id,
                                             @RequestBody NewsRequest latestNews) {
        try {
            News news = newsRepository.getReferenceById(id);
            news.setTitle(latestNews.getTitle());
            news.setPriority(latestNews.getPriority());
            news.setDescription(latestNews.getDescription());
            news.setStatus(latestNews.getStatus());
            news.setExpiration(latestNews.getExpiration());
            news.setUpdatedBy(authUser.getId());
            news.setUpdatedAt(new Date());
            newsRepository.save(news);
            return ResponseEntity.ok().body(news);
        } catch (Exception e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.BAD_REQUEST);
        }
    }
}
