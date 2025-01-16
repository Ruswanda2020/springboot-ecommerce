package com.oneDev.ecommerce.controller.admin;

import com.oneDev.ecommerce.service.BulkReindexService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/reindex")
@SecurityRequirement(name = "Bearer")
public class AdminReindexController {

    private final BulkReindexService bulkReindexService;

    @PostMapping("/products")
    public ResponseEntity<String> reindexController(){

        try {
            bulkReindexService.reindexAllProducts();
            return ResponseEntity.ok("Reindex completed");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error while reindex: " + e.getMessage());
        }
    }
}
