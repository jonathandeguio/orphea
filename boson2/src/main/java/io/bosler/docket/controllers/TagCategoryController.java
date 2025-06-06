package io.bosler.docket.controllers;

import io.bosler.docket.library.models.Tags;
import io.bosler.docket.library.models.TagsCategory;
import io.bosler.docket.library.repository.TagCategoryRepository;
import io.bosler.docket.library.repository.TagRepository;
import io.bosler.kitab.library.models.DatasetModel;
import io.bosler.kitab.library.repository.DatasetRepository;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/docket/category")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Docket", description = "This is to tag category management.")
public class TagCategoryController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthzService authzService;

    private final TagCategoryRepository tagCategoryRepository;
    private final DatasetRepository datasetRepository;
    private final TagRepository tagRepository;



    @Operation(summary = "Create new tag category.")
    @PostMapping("/create")
    public ResponseEntity<Object> create(Principal principal,
                                         HttpServletRequest httpRequest,
                                         HttpServletResponse servletResponse,
                                         @RequestBody TagsCategory tagsCategory
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isOwner(userId, new UUID(0, 0))) {
            return new ResponseEntity<>("Access Denied to tag category creation", HttpStatus.FORBIDDEN);
        }

        if(tagCategoryRepository.existsByName(tagsCategory.getName())) {
            return new ResponseEntity<>("Tag Category name already exists.", HttpStatus.BAD_REQUEST);
        }

        tagsCategory.setCreatedBy(userId);
        tagsCategory.setCreatedAt(new Date());

        TagsCategory tagsCategorySaved = tagCategoryRepository.save(tagsCategory);

        return new ResponseEntity<>(tagsCategorySaved, HttpStatus.OK);
    }

    @Operation(summary = "Update existing tag category by Id.")
    @PutMapping("/update/{Id}")
    public ResponseEntity<Object> update(Principal principal,
                                         HttpServletRequest httpRequest,
                                         HttpServletResponse servletResponse,
                                         @PathVariable("Id") UUID id,
                                         @RequestBody TagsCategory updateTagsCategory
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isOwner(userId, new UUID(0, 0))) {
            return new ResponseEntity<>("Access Denied to updating tag category", HttpStatus.FORBIDDEN);
        }

        if(tagCategoryRepository.existsByNameAndIdNot(updateTagsCategory.getName(), id)) {
            return new ResponseEntity<>("Tag Category name already exists.", HttpStatus.BAD_REQUEST);
        }

        TagsCategory existingTagsCategory = tagCategoryRepository.getById(id);

        existingTagsCategory.setName(updateTagsCategory.getName());
        existingTagsCategory.setDescription(updateTagsCategory.getDescription());
        existingTagsCategory.setEnabled(updateTagsCategory.isEnabled());
        existingTagsCategory.setUpdatedBy(userId);
        existingTagsCategory.setUpdatedAt(new Date());

        TagsCategory tagsCategorySaved = tagCategoryRepository.save(existingTagsCategory);

        return new ResponseEntity<>(tagsCategorySaved, HttpStatus.OK);
    }

    @Operation(summary = "Delete tag category.")
    @GetMapping("/delete/{Id}")
    public ResponseEntity<Object> delete(Principal principal,
                                         HttpServletRequest httpRequest,
                                         HttpServletResponse servletResponse,
                                         @PathVariable("Id") UUID Id
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isOwner(userId, new UUID(0, 0))) {
            return new ResponseEntity<>("Access Denied to delete category", HttpStatus.FORBIDDEN);
        }

        if(!tagCategoryRepository.existsById(Id)) {
            return new ResponseEntity<>("Tag Category Id does not exists.", HttpStatus.BAD_REQUEST);
        }

        List<Tags> tags= tagCategoryRepository.getById(Id).getTags();
        List<DatasetModel> datasetModels = new ArrayList<>();
        for (Tags tag : tags) {
            datasetModels.addAll(datasetRepository.findDatasetModelsByTags(tag));
        }
        if (!datasetModels.isEmpty()) {
            for (DatasetModel datasetModel : datasetModels) {
                datasetModel.tags.removeAll(tags);
            }
        }

        tagCategoryRepository.deleteById(Id);

        return new ResponseEntity<>("Tag Category deleted.", HttpStatus.OK);
    }

    @Operation(summary = "Get by ID tag category.")
    @GetMapping("/{Id}")
    public ResponseEntity<Object> getById(Principal principal,
                                         HttpServletRequest httpRequest,
                                         HttpServletResponse servletResponse,
                                         @PathVariable("Id") UUID Id
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        // TODO : authz

        if(!tagCategoryRepository.existsById(Id)) {
            return new ResponseEntity<>("Tag Category Id does not exists.", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(tagCategoryRepository.findById(Id), HttpStatus.OK);
    }

    @Operation(summary = "Get by All tag category.")
    @GetMapping("/all")
    public ResponseEntity<Object> getAl(Principal principal,
                                          HttpServletRequest httpRequest,
                                          HttpServletResponse servletResponse
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        // TODO : authz


        tagCategoryRepository.findAllByOrderByName();

        return new ResponseEntity<>(tagCategoryRepository.findAllByOrderByName(), HttpStatus.OK);
    }
}
