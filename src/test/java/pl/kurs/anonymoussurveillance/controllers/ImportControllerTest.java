package pl.kurs.anonymoussurveillance.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.anonymoussurveillance.dto.ImportDto;
import pl.kurs.anonymoussurveillance.dto.ImportStatusDto;
import pl.kurs.anonymoussurveillance.models.ImportStatus;
import pl.kurs.anonymoussurveillance.models.Status;
import pl.kurs.anonymoussurveillance.services.ImportService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ImportControllerTest {
    @Mock
    private ImportService importService;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private ImportController importController;
    private ImportStatusDto importStatusDto;
    private ImportStatus importStatus;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        importStatus = new ImportStatus(
                1L,
                Status.COMPLETED,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                100,
                null,
                10.5
        );

        importStatusDto = new ImportStatusDto();
        importStatusDto.setId(1L);
        importStatusDto.setStatus(String.valueOf(Status.COMPLETED));
    }

    @Test
    public void shouldImportPersonFromCsv() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/people_records_sample.csv");
        MockMultipartFile file = new MockMultipartFile("file", "people_records_sample.csv", "text/csv", inputStream);

        Long expectedImportId = 1L;

        when(importService.importFile(any(MultipartFile.class))).thenReturn(expectedImportId);

        ResponseEntity<ImportDto> response = importController.importPersonFromCsv(file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedImportId, response.getBody().getId());
        assertNotNull(response.getBody().getImportDateTime());
    }

    @Test
    public void shouldReturnBadRequestWhenImportFails() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/people_records_sample.csv");
        MockMultipartFile file = new MockMultipartFile("file", "people_records_sample.csv", "text/csv", inputStream);

        when(importService.importFile(any(MultipartFile.class))).thenThrow(new RuntimeException("Import failed"));

        ResponseEntity<ImportDto> response = importController.importPersonFromCsv(file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void shouldGetImportStatus() {
        Long importId = 1L;

        when(importService.getImportStatus(importId)).thenReturn(importStatus);
        when(modelMapper.map(eq(importStatus), eq(ImportStatusDto.class))).thenReturn(importStatusDto);

        ResponseEntity<ImportStatusDto> response = importController.getImportStatus(importId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(importStatusDto.getId(), response.getBody().getId());
        assertEquals(importStatusDto.getStatus(), response.getBody().getStatus());
    }

    @Test
    public void shouldHandleExceptionWhenGettingImportStatusFails() {
        Long importId = 999L;

        when(importService.getImportStatus(importId)).thenThrow(new RuntimeException("Import not found"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            importController.getImportStatus(importId);
        });

        assertEquals("Import not found", exception.getMessage());
    }

    @Test
    public void shouldHandleMissingFileInImport() {
        MultipartFile mockFile = null;

        when(importService.importFile(null)).thenThrow(new RuntimeException("File is missing"));

        ResponseEntity<ImportDto> response = importController.importPersonFromCsv(mockFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }
}
