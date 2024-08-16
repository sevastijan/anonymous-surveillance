package pl.kurs.anonymoussurveillance.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.anonymoussurveillance.models.ImportStatus;
import pl.kurs.anonymoussurveillance.services.ImportService;

@RestController
@RequestMapping("/api/v1/import")
@RequiredArgsConstructor
public class ImportController {
    private final ImportService importService;

    @Transactional
    @PostMapping
    public ResponseEntity<Long> importCsv(@RequestParam("file") MultipartFile file) {
        try {
            Long importId = importService.importFile(file);
            return ResponseEntity.ok(importId);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }

    }

    @GetMapping("/{importId}")
    public ResponseEntity<ImportStatus> getImportStatus(@PathVariable Long importId) {
        ImportStatus importStatus = importService.getImportStatus(importId);
        return ResponseEntity.ok(importStatus);
    }
}
