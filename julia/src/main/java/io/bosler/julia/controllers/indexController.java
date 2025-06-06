package io.bosler.julia.controllers;


import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@Tag(name = "Index", description = "This is a only for health checks stuff, never delete this. All hell will break loose.")
public class indexController {

    @Hidden
    @RequestMapping("/")
    public String indexRedirect() {
        return "<html><h2>OK</h2></html>";
    }

    @Hidden
    @RequestMapping("/api/ping")
    public String ping() {
        return "pong";
    }


    // Not working yet
    @Hidden
    @RequestMapping("/swagger")
    public String swaggerRedirect() {
        return "redirect:/swagger-ui.html";
    }

}
