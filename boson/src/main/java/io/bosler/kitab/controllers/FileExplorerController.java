package io.bosler.kitab.controllers;

import io.bosler.kitab.library.enums.ResourceSubtype;
import io.bosler.kitab.library.enums.ResourceType;
import io.bosler.kitab.library.models.Favourites;
import io.bosler.kitab.library.models.ResourceModel;
import io.bosler.kitab.library.repository.FavouritesRepository;
import io.bosler.kitab.library.repository.ResourceViewsRepository;
import io.bosler.kitab.library.services.ResourceService;
import io.bosler.passport.exception.UnauthorizedException;
import io.bosler.passport.library.Auth;
import io.bosler.passport.library.models.User;
import io.bosler.passport.library.service.AuthzService;
import io.bosler.passport.library.service.UserService;
import io.bosler.passport.security.AuthUser;
import io.bosler.sharedutils.DTO.PageToPageDTOMapper;
import io.bosler.sharedutils.Response.ErrorDTO;
import io.bosler.sharedutils.models.PageSettings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kitab/explorer")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "File Explorer", description = "This is a file explorer service.")
public class FileExplorerController {
    @Inject
    ResourceService resourceService;
    @Inject
    AuthzService authzService;
    @Inject
    FavouritesRepository favouritesRepository;
    @Inject
    UserService userService;
    @Inject
    ResourceViewsRepository resourceViewsRepository;

    @Inject
    PageToPageDTOMapper pageToPageDTOMapper;

    // PROJECTS CONTROLLER --------------------------------------------------------------------------------------------
    @Operation(summary = "It provides list of all active projects.")
    @GetMapping("/projects")
    public ResponseEntity<Object> projects(Principal principal) {
        User user = userService.getUser(principal.getName());
        List<ResourceModel> activeProjects = resourceService.getActiveProjects(user.getId());

        return ResponseEntity.ok().body(activeProjects);
    }

    // TODO: CHECK WHETHER PARENT IS DELETED
    @Operation(summary = "It provides root directory structure of a project.")
    @GetMapping("/projectRoot/{id}")
    @PreAuthorize(Auth.VIEWER)
    public ResponseEntity<Object> projectTree(@PathVariable("id") UUID id, Principal principal) {
        User user = userService.getUser(principal.getName());

        ResourceModel projectRoot = resourceService.getProjectTree(id, user.getId());

        if (projectRoot != null) return ResponseEntity.ok().body(projectRoot);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorDTO(401, "Either no such resource Exist or you don't have permission to access it."));
    }

    // TODO: CHECK FOR PERMISSION BEFORE CREATING
    @Operation(summary = "Creates new Folder")
    @PostMapping("/folder")
    public ResponseEntity<Object> newFolder(@RequestBody ResourceRequest request, Principal principal) {
        User user = userService.getUser(principal.getName());

        if (!authzService.isEditor(user.getId(), request.getParent())) {
            throw new UnauthorizedException();
        }

        return ResponseEntity.ok().body(resourceService.newResource(request.getName(), request.description, request.getType(), ResourceSubtype.FOLDER, user.getId(), request.getParent()));
    }

    @Operation(summary = "It provides resource based on id.")
    @GetMapping("/resource/{id}")
    @PreAuthorize(Auth.VIEWER)
    public ResponseEntity<Object> getResource(@PathVariable("id") UUID id, Principal principal) {
        User user = userService.getUser(principal.getName());

        ResourceModel resource = resourceService.getResourceModelWithChildren(id, user.getId());
        return ResponseEntity.ok().body(resource);
    }

    // TODO: CREATE PROJECT
    // TODO: PROJECT BY NAME ?
    // TODO: UPDATE RESOURCE
    // TODO: PERMANENT DELETE
    // TODO: isDEV
    // TODO: Get ID BY PATH

    @Operation(summary = "Modifies the resource.")
    @PutMapping("/resource/{id}")
    public ResponseEntity<Object> putResource(@PathVariable("id") UUID id, @RequestBody ResourceRequest request, Principal principal) {
        User user = userService.getUser(principal.getName());

        if (request.getParent() != null && !authzService.isEditor(user.getId(), request.getParent())) {
            throw new UnauthorizedException();
        }

        ResourceModel resource = resourceService.updateResource(id, request.getName(), request.getDescription(), user, request.getParent());
        return ResponseEntity.ok().body(resource);
    }

    @Operation(summary = "It provides Kitab list of recently viewed/updated/created resources.")
    @GetMapping("/recent")
    public ResponseEntity<Object> recent(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).getId();

        return new ResponseEntity<>(resourceService.getRecentlyViewed(userId), HttpStatus.OK);
    }

    @Operation(summary = "It provides Kitab list of updated by you resources.")
    @GetMapping("/updatedByYou")
    public ResponseEntity<Object> updatedByYou(@AuthenticationPrincipal AuthUser authUser, PageSettings pageSettings) {
        UUID userId = authUser.getId();
        return ResponseEntity.ok().body(pageToPageDTOMapper.pageToPageDTO(
                resourceService.getUpdatedByYou(pageSettings, userId)));
    }

    @Operation(summary = "It provides Kitab list of created by you resources.")
    @GetMapping("/createdByYou")
    public ResponseEntity<Object> createdByYou(@AuthenticationPrincipal AuthUser authUser, PageSettings pageSettings) {
        UUID userId = authUser.getId();
        return ResponseEntity.ok().body(pageToPageDTOMapper.pageToPageDTO(
                resourceService.getCreatedByYou(pageSettings, userId)));
    }

    // TRASH CONTROLLERS ----------------------------------------------------------------------------------------------
    @Operation(summary = "It provides root directory structure of a project.")
    @GetMapping("/trash/{projectId}")
    public ResponseEntity<Object> projectTrash(@PathVariable("projectId") UUID projectId, Principal principal) {
        User user = userService.getUser(principal.getName());

        List<ResourceModel> inTrash = resourceService.getInTrash(projectId, user.getId());
        return ResponseEntity.ok().body(inTrash);
    }

    @Operation(summary = "It provides root directory structure of a project.")
    @PostMapping("/trash/{resourceId}")
    public ResponseEntity<Object> addToTrash(@PathVariable("resourceId") UUID resourceId, Principal principal) throws SchedulerException {
        User user = userService.getUser(principal.getName());
        if (authzService.isEditor(user.getId(), resourceId)) {
            ResourceModel inTrash = resourceService.addToTrash(resourceId);
            if (inTrash != null) return ResponseEntity.ok().body(inTrash);

            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ErrorDTO(HttpStatus.EXPECTATION_FAILED.value(), "No such resource exist or already in trash."));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorDTO(HttpStatus.UNAUTHORIZED.value(), "You don't have permission to delete this file."));
    }

    @Operation(summary = "It provides root directory structure of a project.")
    @DeleteMapping("/trash/{resourceId}")
    public ResponseEntity<Object> removeFromTrash(@PathVariable("resourceId") UUID resourceId, Principal principal) throws SchedulerException {
        User user = userService.getUser(principal.getName());
        if (authzService.isEditor(user.getId(), resourceId)) {
            ResourceModel inTrash = resourceService.removeFromTrash(resourceId);
            if (inTrash != null) return ResponseEntity.ok().body(inTrash);

            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ErrorDTO(HttpStatus.EXPECTATION_FAILED.value(), "No such resource in trash."));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorDTO(HttpStatus.UNAUTHORIZED.value(), "You don't have permission to restore this file."));
    }

    @Operation(summary = "It deletes all files from trash of a project.")
    @DeleteMapping("/permanentDelete/{resourceId}")
    public ResponseEntity<Object> permanentDelete(@PathVariable("resourceId") UUID resourceId, Principal principal) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId();
        if (authzService.isOwner(userId, resourceId)) {
            // TODO : this should detect type and based on that it should delete the
            // physical stuff also.
            // may be initially, just mark inTrash, then cleaning service deletes it.

            resourceService.permanentDelete(resourceId, userId);

            return ResponseEntity.status(HttpStatus.OK).body("Deleted successfully");
        }
        throw new UnauthorizedException("You don't have permission to delete files permanently.");
    }

    // FAVOURITES CONTROLLERS -----------------------------------------------------------------------------------------
    @Operation(summary = "It provides Kitab list of favourites resources.")
    @GetMapping("/favourites")
    public ResponseEntity<Object> getFavourites(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).getId();

        return new ResponseEntity<>(resourceService.getFavourites(userId), HttpStatus.OK);
    }

    @Operation(summary = "Add or remove favourite resources.")
    @PostMapping("/favourites/{resourceId}")
    public ResponseEntity<Object> addFavourite(Principal principal, @PathVariable("resourceId") UUID resourceId) {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (favouritesRepository.existsByUserIdAndResourceId(userId, resourceId)) {
            return new ResponseEntity<>("Favourite Already Exists", HttpStatus.OK);
        } else {
            favouritesRepository.save(Favourites.builder().userId(userId).resourceId(resourceId).build());
            return new ResponseEntity<>("Favourite Added", HttpStatus.OK);
        }
    }

    @Operation(summary = "Add or remove favourite resources.")
    @DeleteMapping("/favourites/{resourceId}")
    public ResponseEntity<Object> removeFavourite(Principal principal, @PathVariable("resourceId") UUID resourceId) {
        UUID userId = userService.getUser(principal.getName()).getId();

        Optional<Favourites> fav = favouritesRepository.findByUserIdAndResourceId(userId, resourceId);
        if (fav.isPresent()) {
            favouritesRepository.delete(fav.get());
            return new ResponseEntity<>("Favourite Removed", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Favourite Not Found", HttpStatus.OK);
        }
    }

    @Getter
    @Setter
    public static class ResourceRequest {
        private String name;
        private String description;
        private UUID parent;
        private ResourceType type;
        private ResourceSubtype subtype;
    }
}
