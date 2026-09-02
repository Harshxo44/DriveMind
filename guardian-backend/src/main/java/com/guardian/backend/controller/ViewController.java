package com.guardian.backend.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class ViewController {

    @GetMapping(value = {"/", "/index.html", "/cockpit", "/simulator", "/trips", "/guardian", "/diagnostics", "/codriver", "/vehicles"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> index() throws IOException {
        Resource resource = new ClassPathResource("static/index.html");
        String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok(html);
    }
}
