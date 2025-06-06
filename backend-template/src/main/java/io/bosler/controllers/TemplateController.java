package io.bosler.Controllers;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.bosler.library.services.TemplateRepository;
import io.bosler.library.models.TemplateModel;

import java.util.List;

@RestController
public class TemplateController {

    private final TemplateRepository templateRepository;

    public TemplateController(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Tag(name = "Retrieve", description = "Get operations.")
    @GetMapping("/Template")
    public ResponseEntity<List<TemplateModel>> getTemplate() {

        return new ResponseEntity<>(templateRepository.findAll(), HttpStatus.OK);
    }

    @Tag(name = "Retrieve", description = "Get by ID operations.")
    @GetMapping("/Template/{Id}")
    public ResponseEntity<String> getTemplateById(@PathVariable("Id") Long Id) {

        boolean exists = templateRepository.existsById(Id);
        if(!exists) {
            return new ResponseEntity<>("Template Id " + Id + " not found!", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(templateRepository.getById(Id).getName(), HttpStatus.OK);
    }

    @Tag(name = "Create", description = "Create operations.")
    @PostMapping("/Template")
    TemplateModel newTemplate(@RequestBody TemplateModel newTemplate) {
        return templateRepository.save(newTemplate);
    }

    @Tag(name = "Delete", description = "Delete operations.")
    @DeleteMapping(path = "/Tempalte/{templateId}")
    public ResponseEntity<String> deleteTemplate(@PathVariable("templateId") Long templateId) {
        boolean exists = templateRepository.existsById(templateId);

        if(!exists) {
            return new ResponseEntity<>("templateId " + templateId + " does not exits!", HttpStatus.NOT_FOUND);
//            throw new HttpStatus.NOT_FOUND("templateId " + templateId + " does not exits!");
        }
        templateRepository.deleteById(templateId);

        return new ResponseEntity<>("templateId " + templateId + " deleted successfully!", HttpStatus.OK);
    }

}

