package com.sentinel;

import jakarta.persistence.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

// ─────────────────────────────────────────────────────────────────────────────
//  SENTINEL-IX  |  Data Leak Prevention Engine
//  Single-file Spring Boot Application
// ─────────────────────────────────────────────────────────────────────────────

@SpringBootApplication
public class SentinelApplication {
    public static void main(String[] args) {
        SpringApplication.run(SentinelApplication.class, args);
    }
}

// ── JPA Entity ────────────────────────────────────────────────────────────────

@Entity
@Table(name = "audit_logs")
class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_preview", length = 50)
    private String contentPreview;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScanStatus status;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // ── Constructors ──────────────────────────────────────────────────────────

    public AuditLog() {}

    public AuditLog(String contentPreview, ScanStatus status) {
        this.contentPreview = contentPreview;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getContentPreview() { return contentPreview; }
    public void setContentPreview(String contentPreview) { this.contentPreview = contentPreview; }

    public ScanStatus getStatus() { return status; }
    public void setStatus(ScanStatus status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // ── Formatted timestamp for JSON ──────────────────────────────────────────
    public String getFormattedTimestamp() {
        return timestamp != null
                ? timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;
    }
}

// ── Enum ───────────────────────────────────────────────────────────────────────

enum ScanStatus {
    BLOCKED, CLEAN
}

// ── JPA Repository ────────────────────────────────────────────────────────────

interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByTimestampDesc();
}

// ── DLP Scanner Service ───────────────────────────────────────────────────────

@org.springframework.stereotype.Service
class DlpScannerService {

    private static final List<String> SENSITIVE_KEYWORDS = List.of(
            "password", "secret", "api_key", "apikey",
            "token", "private_key", "ssn", "credit_card",
            "passwd", "auth_token", "access_key"
    );

    private final AuditLogRepository repository;

    DlpScannerService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public AuditLog scan(String inputText) {
        String lowerInput = inputText.toLowerCase();

        ScanStatus status = SENSITIVE_KEYWORDS.stream()
                .anyMatch(lowerInput::contains)
                ? ScanStatus.BLOCKED
                : ScanStatus.CLEAN;

        // Truncate preview to 50 chars
        String preview = inputText.length() > 50
                ? inputText.substring(0, 47) + "..."
                : inputText;

        AuditLog log = new AuditLog(preview, status);
        return repository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return repository.findAllByOrderByTimestampDesc();
    }
}

// ── REST Controller ───────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
class SentinelController {

    private final DlpScannerService scannerService;

    SentinelController(DlpScannerService scannerService) {
        this.scannerService = scannerService;
    }

    /**
     * POST /api/scan
     * Body: { "text": "..." }
     * Returns the saved AuditLog entry with scan result.
     */
    @PostMapping("/scan")
    public ResponseEntity<AuditLog> scan(@RequestBody Map<String, String> body) {
        String text = body.getOrDefault("text", "").trim();

        if (text.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        AuditLog result = scannerService.scan(text);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/logs
     * Returns all scan history, newest first.
     */
    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getLogs() {
        return ResponseEntity.ok(scannerService.getAllLogs());
    }

    /**
     * GET /api/health
     * Simple heartbeat endpoint for the dashboard.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "OPERATIONAL",
                "service", "Sentinel-IX DLP Engine",
                "timestamp", LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));
    }
}
