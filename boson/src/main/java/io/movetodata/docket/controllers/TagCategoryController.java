package io.movetodata.docket.controllers;

import io.movetodata.docket.library.models.Tags;
import io.movetodata.docket.library.models.TagsCategory;
import io.movetodata.docket.library.models.TagsCategoryResponse;
import io.movetodata.docket.library.repository.TagCategoryRepository;
import io.movetodata.docket.library.repository.TagRepository;
import io.movetodata.kitab.library.models.DatasetModel;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.passport.library.repository.UserRepository;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.sharedutils.models.PageSettings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.security.Principal;
import java.util.*;

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
                                         @RequestBody TagsCategory tagsCategory
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isOwner(userId, new UUID(0, 0))) {
            return new ResponseEntity<>("Access Denied to tag category creation", HttpStatus.FORBIDDEN);
        }

        if (tagCategoryRepository.existsByName(tagsCategory.getName())) {
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

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isOwner(userId, new UUID(0, 0))) {
            return new ResponseEntity<>("Access Denied to updating tag category", HttpStatus.FORBIDDEN);
        }

        if (tagCategoryRepository.existsByNameAndIdNot(updateTagsCategory.getName(), id)) {
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

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isOwner(userId, new UUID(0, 0))) {
            return new ResponseEntity<>("Access Denied to delete category", HttpStatus.FORBIDDEN);
        }

        if (!tagCategoryRepository.existsById(Id)) {
            return new ResponseEntity<>("Tag Category Id does not exists.", HttpStatus.BAD_REQUEST);
        }

        List<Tags> tags = tagCategoryRepository.getById(Id).getTags();
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

        UUID userId = userService.getUser(principal.getName()).getId();

        // TODO : authz

        if (!tagCategoryRepository.existsById(Id)) {
            return new ResponseEntity<>("Tag Category Id does not exists.", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(tagCategoryRepository.findById(Id), HttpStatus.OK);
    }

//    @Operation(summary = "Get by All tag category.")
//    @GetMapping("/all")
//    public ResponseEntity<Object> getAl(Principal principal,
//                                        @RequestParam String searchText,
//                                        PageSettings pageSettings,
//                                        HttpServletRequest httpRequest,
//                                        HttpServletResponse servletResponse
//    ) throws Exception {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        // TODO : authz
//        Sort sortedCategories = pageSettings.buildSort();
//        Sort sortedTags = pageSettings.buildSort();
//        Map<String, Object> responseBody = new HashMap<>();
//
//        Pageable paginatedCategories = PageRequest.of(pageSettings.getPage(), pageSettings.getElementPerPage(), sortedCategories);
//        Pageable paginatedTags = PageRequest.of(pageSettings.getPage(), pageSettings.getElementPerPage(), sortedTags);
//        if (searchText != null && !searchText.isEmpty()) {
//            Page<TagsCategory> result = tagCategoryRepository.findByNameContaining(searchText, paginatedCategories);
//            Page<Tags> tagsResult = tagRepository.findByNameContaining(searchText, paginatedTags);
//            responseBody.put("categories", result);
//            responseBody.put("tags", tagsResult);
//
//            return new ResponseEntity<>(responseBody, HttpStatus.OK);
//        }
//        Page<TagsCategory> allCategoriesWithoutSearchText = tagCategoryRepository.findAllByOrderByUpdatedAtDesc(paginatedCategories);
//        responseBody.put("categories",allCategoriesWithoutSearchText);
//        responseBody.put("tags",null);
//        return new ResponseEntity<>(responseBody, HttpStatus.OK);
//    }

    @Operation(summary = "Get by All tag category.")
    @GetMapping("/all")
    public ResponseEntity<Object> getAl2(Principal principal,
                                         PageSettings pageSettings,
                                         HttpServletRequest httpRequest,
                                         HttpServletResponse servletResponse
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();

        // TODO : authz
//        Sort sortedCategories = pageSettings.buildSort();
//        Pageable paginatedCategories = PageRequest.of(pageSettings.getPage(), pageSettings.getElementPerPage(), sortedCategories);
//        if (searchText != null && !searchText.isEmpty()) {
//            Page<TagsCategory> result = tagCategoryRepository.findByNameContaining(searchText, paginatedCategories);
//            return new ResponseEntity<>(result, HttpStatus.OK);
//        }
        List<TagsCategory> allCategories = tagCategoryRepository.findAll();
        List<TagsCategoryResponse> allCategoryResponse = new ArrayList<>();
        for (TagsCategory tagsCategory : allCategories) {
            TagsCategoryResponse tagsCategoryResponse = new TagsCategoryResponse();

            tagsCategoryResponse.setId(tagsCategory.getId());
            tagsCategoryResponse.setName(tagsCategory.getName());
            tagsCategoryResponse.setDescription(tagsCategory.getDescription());
            tagsCategoryResponse.setEnabled(tagsCategory.isEnabled());
            tagsCategoryResponse.setCreatedAt(tagsCategory.getCreatedAt());
            tagsCategoryResponse.setCreatedBy(tagsCategory.getCreatedBy());
            tagsCategoryResponse.setUpdatedBy(tagsCategory.getUpdatedBy());
            tagsCategoryResponse.setUpdatedAt(tagsCategory.getUpdatedAt());
            allCategoryResponse.add(tagsCategoryResponse);

        }
        return new ResponseEntity<>(allCategories, HttpStatus.OK);
    }

    @Operation(summary = "Get by All tag names.")
    @GetMapping("/categoryNames")
    public ResponseEntity<Object> getCategoryNames(Principal principal){
        List<String> result=tagCategoryRepository.findCategoryNames();
        return new ResponseEntity<>(result,HttpStatus.OK);
    }

    @Operation(summary = "Get Tags by catgeory name")
    @GetMapping("/categoryDetailsAndTags/{categoryName}")
    public ResponseEntity<Object> getTagsByCategoryName(Principal principal,@PathVariable("categoryName") String categoryName){
        TagsCategory tagsCategory=tagCategoryRepository.findByName(categoryName);
        return new ResponseEntity<>(tagsCategory,HttpStatus.OK);
    }
}