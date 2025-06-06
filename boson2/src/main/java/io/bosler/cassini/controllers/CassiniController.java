package io.bosler.cassini.controllers;

import io.bosler.docket.library.models.TagsCategory;
import io.bosler.docket.library.repository.TagCategoryRepository;
import io.bosler.kitab.library.repository.FolderRepository;
import io.bosler.passport.library.repository.UserRepository;
import io.bosler.passport.library.service.AuthzService;
import io.bosler.passport.library.service.UserService;
import io.bosler.sharedUtils.Response.OkResponse;
import io.bosler.synchro.library.models.PostgresSyncSpecification;
import io.bosler.synchro.library.repository.PostgresSyncRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/cassini")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cassini", description = "This is to dataset monitoring system.")
public class CassiniController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthzService authzService;

    private final TagCategoryRepository tagCategoryRepository;



    @Operation(summary = "Create new dataset monitor.")
    @PostMapping("/create")
    public ResponseEntity<Object> create(Principal principal
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isOwner(userId, new UUID(0, 0))) {
            return new ResponseEntity<>("Access Denied to tag category creation", HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>("test", HttpStatus.OK);
    }
}
