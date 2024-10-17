package pl.kurs.anonymoussurveillance.controllers;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.anonymoussurveillance.dto.ImportDto;
import pl.kurs.anonymoussurveillance.dto.ImportStatusDto;
import pl.kurs.anonymoussurveillance.models.ImportStatus;
import pl.kurs.anonymoussurveillance.services.ImportService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/import")
@RequiredArgsConstructor
public class ImportController {
    private final ImportService importService;
    private final ModelMapper modelMapper;

    @Transactional
    @PostMapping
    public ResponseEntity<ImportDto> importPersonFromCsv(@RequestParam("file") MultipartFile file) {
        try {
            Long importId = importService.importFile(file);
            ImportDto importDto = new ImportDto(importId, LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.OK).body(importDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/{importId}")
    public ResponseEntity<ImportStatusDto> getImportStatus(@PathVariable Long importId) {
        ImportStatus importStatus = importService.getImportStatus(importId);

        ImportStatusDto importStatusDto = modelMapper.map(importStatus, ImportStatusDto.class);

        return ResponseEntity.status(HttpStatus.OK).body(importStatusDto);
    }
}
