package io.orphea.julia.controllers;


import io.orphea.julia.library.services.GitService;
import io.orphea.passport.library.service.UserService;
import io.orphea.sharedUtils.Response.OkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.lib.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/julia")
//@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Julia", description = "This is a git code repository management service.")
public class juliaController {

    private final UserService userService;
    private final GitService groupService;
    private final OkResponse response = new OkResponse();

    @Operation(summary = "This endpoint can be used to create / update projects")
    @GetMapping("/repository/{RepositoryId}/create")
    public ResponseEntity<?> newRepository(@PathVariable("RepositoryId") UUID RepositoryId) throws InvalidRefNameException, IOException {


        File repositoryDirectory = new File(System.getenv("JULIA_BASE_PATH") + "/" + RepositoryId);

        if (repositoryDirectory.exists()) {
            return new ResponseEntity<>("Repository already exists", HttpStatus.BAD_REQUEST);
        }

        Repository remoteRepository = GitService.getRepository(RepositoryId, "master");

        remoteRepository.create();

        FileWriter fileWriter = new FileWriter(System.getenv("JULIA_BASE_PATH") + "/"
                + RepositoryId + "/.git/config", true);
        fileWriter.append("[http]\n" + "    receivepack = true");
        fileWriter.close();


        return new ResponseEntity<>("Repository created successfully", HttpStatus.OK);
    }

//    @Operation(summary = "It provides project by Name")
//    @GetMapping("/repository/{name}")
//    public ResponseEntity<Object> getProjectByName(@PathVariable("name") String name) {
//
//        List<FolderModel> allProjects = folderRepository.getByType("project");
//        for (FolderModel project : allProjects) {
//            if (project.getName().equals(name)) {
//                if (project.getStatus().equals("active")) {
//                    return new ResponseEntity<>(project, HttpStatus.OK);
//                } else {
//
//                    return new ResponseEntity<>(response.okResponse("The project has been deleted."), HttpStatus.OK);
//                }
//            }
//        }
//        return new ResponseEntity<>("Project with name " + name + " not found.", HttpStatus.NOT_FOUND);
//    }
//
//    @Operation(summary = "It provides project by Id")
//    @GetMapping("/{Id}")
//    public ResponseEntity<Object> getProjectById(@PathVariable("Id") UUID Id, Principal principal) {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();
//
//        resourceViewsModel.setResourceId(Id);
//        resourceViewsModel.setAction("view");
//        resourceViewsModel.setViewedBy(userId);
//
//        resourceViewsRepository.save(resourceViewsModel);
//
//        if (!folderRepository.existsById(Id)) {
//            return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);
//
//        } else if (folderRepository.findById(Id).get().getStatus().equals("inTrash")) {
//            return new ResponseEntity<>(response.okResponse("The project has been deleted."), HttpStatus.OK);
//        }
//        return new ResponseEntity<>(folderRepository.findById(Id).get(), HttpStatus.OK);
//    }


//    @Operation(summary = "It provides list of all kitab.")
//    @GetMapping("/all")
//    public ResponseEntity<List<FolderModel>> getAll() {
//
//        List<FolderModel> all = folderRepository.findAll();
//        List<FolderModel> activeFolderModel = ActiveDisplay.activeDisplay(all);
//
//        return ResponseEntity.ok().body(activeFolderModel);
//    }
//
//
//    @Operation(summary = "It provides list of all projects")
//    @GetMapping("/repository/all")
//    public ResponseEntity<List<FolderModel>> getProjects() {
//
//        List<FolderModel> allProjects = folderRepository.getByType("project");
//        List<FolderModel> activeProjects = ActiveDisplay.activeDisplay(allProjects);
//
//        return ResponseEntity.ok().body(activeProjects);
//    }

}