package io.orphea.docket.controllers;

import io.orphea.docket.library.models.Tags;
import io.orphea.docket.library.models.TagsCategory;
import io.orphea.docket.library.models.TagsRequest;
import io.orphea.docket.library.repository.TagCategoryRepository;
import io.orphea.docket.library.repository.TagRepository;
import io.orphea.kitab.library.models.ManageTagsRequest;
import io.orphea.kitab.library.models.DatasetModel;
import io.orphea.kitab.library.repository.DatasetRepository;
import io.orphea.kitab.library.repository.FolderRepository;
import io.orphea.passport.library.repository.UserRepository;
import io.orphea.passport.library.service.AuthzService;
import io.orphea.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import java.security.Principal;
import java.util.*;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/docket/tag")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Docket", description = "This is to tag management.")
public class TagsController {

    private final UserRepository userRepository;
    private final DatasetRepository datasetRepository;
    private final FolderRepository folderRepository;
    private final UserService userService;
    private final AuthzService authzService;

    private final TagRepository tagRepository;
    private final TagCategoryRepository tagCategoryRepository;


    @Operation(summary = "Create new tag.")
    @PostMapping("/create")
    public ResponseEntity<Object> create(Principal principal,
                                         HttpServletRequest httpRequest,
                                         HttpServletResponse servletResponse,
                                         @RequestBody TagsRequest tagsRequest
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isOwner(userId, new UUID(0, 0))) {
            return new ResponseEntity<>("Access Denied to tag creation", HttpStatus.FORBIDDEN);
        }

        if (tagsRequest.getTagsCategoryId() == null) {
            return new ResponseEntity<>("Tag category is required.", HttpStatus.BAD_REQUEST);
        }

        if (!tagCategoryRepository.existsById(tagsRequest.getTagsCategoryId())) {
            return new ResponseEntity<>("Tag category by given Id does not exists.", HttpStatus.BAD_REQUEST);
        }

        if (tagRepository.existsByName(tagsRequest.getName())) {
            return new ResponseEntity<>("Tag name already exists.", HttpStatus.BAD_REQUEST);
        }

        Tags tags = new Tags();

        tags.setName(tagsRequest.getName());

        tags.setDescription(tagsRequest.getDescription());
        tags.setColor(tagsRequest.getColor());

        TagsCategory tagsCategory = tagCategoryRepository.getById(tagsRequest.getTagsCategoryId());

        tags.setTagsCategory(tagsCategory);

        tags.setCreatedBy(userId);
        tags.setCreatedAt(new Date());

        Tags tagSaved = tagRepository.save(tags);

        return new ResponseEntity<>(tagSaved, HttpStatus.OK);
    }

    @Operation(summary = "Update existing tag by ID.")
    @PutMapping("/update/{Id}")
    public ResponseEntity<Object> updateById(Principal principal,
                                             @RequestBody TagsRequest tagsUpdate,
                                             @PathVariable("Id") UUID Id
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isOwner(userId, new UUID(0, 0))) {
            return new ResponseEntity<>("Access Denied to update tag", HttpStatus.FORBIDDEN);
        }

        if (tagRepository.existsByNameAndIdNot(tagsUpdate.getName(), Id)) {
            return new ResponseEntity<>("Tag name already exists.", HttpStatus.BAD_REQUEST);
        }

        Tags tags = tagRepository.getById(Id);

        tags.setName(tagsUpdate.getName());
        tags.setDescription(tagsUpdate.getDescription());
        tags.setColor(tagsUpdate.getColor());
        tags.setUpdatedBy(userId);
        tags.setUpdatedAt(new Date());

        Tags tagSaved = tagRepository.save(tags);

        return new ResponseEntity<>(tagSaved, HttpStatus.OK);
    }

    @Operation(summary = "Delete tag.")
    @GetMapping("/delete/{Id}")
    public ResponseEntity<Object> delete(Principal principal,
                                         HttpServletRequest httpRequest,
                                         HttpServletResponse servletResponse,
                                         @PathVariable("Id") UUID Id
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isOwner(userId, new UUID(0, 0))) {
            return new ResponseEntity<>("Access Denied to tag creation", HttpStatus.FORBIDDEN);
        }

        if (!tagRepository.existsById(Id)) {
            return new ResponseEntity<>("Tag Id does not exists.", HttpStatus.BAD_REQUEST);
        }

        Tags tag= tagRepository.getById(Id);
        List<DatasetModel> datasetModels = datasetRepository.findDatasetModelsByTags(tag);
        if (!datasetModels.isEmpty()) {
            for (DatasetModel datasetModel : datasetModels) {
                datasetModel.tags.remove(tag);
            }
        }
        tagRepository.deleteById(Id);

        return new ResponseEntity<>("Tag deleted.", HttpStatus.OK);
    }

    @Operation(summary = "Get by ID tag.")
    @GetMapping("/{Id}")
    public ResponseEntity<Object> getById(Principal principal,
                                          HttpServletRequest httpRequest,
                                          HttpServletResponse servletResponse,
                                          @PathVariable("Id") UUID Id
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        // TODO : authz

        if (!tagRepository.existsById(Id)) {
            return new ResponseEntity<>("Tag Category Id does not exists.", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(tagRepository.findById(Id), HttpStatus.OK);
    }

    @Operation(summary = "Get by All tag.")
    @GetMapping("/all")
    public ResponseEntity<Object> getAl(Principal principal,
                                        HttpServletRequest httpRequest,
                                        HttpServletResponse servletResponse
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        // TODO : authz


        tagRepository.findAllByOrderByName();

        return new ResponseEntity<>(tagRepository.findAllByOrderByName(), HttpStatus.OK);
    }

    @Operation(summary = "Assign tag to dataset dataset by Id")
    @PostMapping("/manage")
    public ResponseEntity<Object> addTagToDataset(@Valid @RequestBody ManageTagsRequest manageTagsRequest, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!folderRepository.existsById(manageTagsRequest.getDatasetId())) {
            return new ResponseEntity<>("Id " + manageTagsRequest.getDatasetId() + " does not exist.", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isEditor(userId, manageTagsRequest.getDatasetId())) {
            return new ResponseEntity<>("Access Denied to " + manageTagsRequest.getDatasetId(), HttpStatus.FORBIDDEN);
        }

        if (manageTagsRequest.getAction() == null) {
            return new ResponseEntity<>("Action is required, either add or remove.", HttpStatus.BAD_REQUEST);
        }

        if ((!Objects.equals(manageTagsRequest.getAction(), "add")) && (!Objects.equals(manageTagsRequest.getAction(), "remove"))) {
            return new ResponseEntity<>("Not a valid action type, use add or remove", HttpStatus.BAD_REQUEST);
        }

        DatasetModel datasetModel = datasetRepository.findDatasetModelById(manageTagsRequest.getDatasetId());

        List<Tags> tagsList = datasetModel.getTags();

        for (UUID tag : manageTagsRequest.getTagIds()) {

            if(!tagRepository.existsById(tag)){
                return new ResponseEntity<>("Tags not found " + tag, HttpStatus.BAD_REQUEST);
            }

            Tags tags = tagRepository.getById(tag);

            if (tagsList.contains(tags)) {
                if (Objects.equals(manageTagsRequest.getAction(), "remove")) {
                    tagsList.remove(tags);
                }
            } else {
                if (Objects.equals(manageTagsRequest.getAction(), "add")) {
                    tagsList.add(tags);
                }
            }
        }
        datasetModel.setTags(tagsList);

        datasetModel.setUpdatedAt(new Date());
        datasetModel.setUpdatedBy(userId);

        DatasetModel datasetModelSaved = datasetRepository.save(datasetModel);

        return new ResponseEntity<>("Tags saved", HttpStatus.OK);
    }

    @Operation(summary = "Get tag by dataset ID tag.")
    @GetMapping("/getTagsByDatasetId/{datasetId}")
    public ResponseEntity<Object> getByDatasetId(Principal principal,
                                                 HttpServletRequest httpRequest,
                                                 HttpServletResponse servletResponse,
                                                 @PathVariable("datasetId") UUID datasetId
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        DatasetModel datasetModel = datasetRepository.findDatasetModelById(datasetId);

        return new ResponseEntity<>(datasetModel.getTags(), HttpStatus.OK);

    }
}
