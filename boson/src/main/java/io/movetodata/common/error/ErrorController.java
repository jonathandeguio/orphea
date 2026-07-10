package io.movetodata.common.error;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/errors")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Errors", description = "Log for frontend errors")
public class ErrorController {
    @Autowired
    ErrorService errorService;

    @GetMapping()
    public ResponseEntity<Object> getErrors() {
        return ResponseEntity.accepted().body(errorService.getAllErrors());
    }

    @PostMapping()
    public ResponseEntity<Object> saveError(@RequestBody ErrorDTO errorModel) {
        return ResponseEntity.accepted().body(errorService.save(errorModel));
    }
}
