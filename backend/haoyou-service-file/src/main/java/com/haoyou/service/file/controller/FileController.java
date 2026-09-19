package com.haoyou.service.file.controller;

import com.haoyou.common.BusinessException;
import com.haoyou.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传与访问（走网关 /api/file/** 路由，直连 59858 亦可）
 * 本地磁盘落盘（haoyou.file.storage=local），预留 MinIO 迁移。
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final long MAX_SIZE = 10 * 1024 * 1024L; // FR-13：≤10MB
    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "jpg", "jpeg", "png", "docx");

    @Value("${haoyou.file.local-path:./data/upload}")
    private String basePath;

    /** 上传：返回可访问 URL */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择文件");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(400, "文件不能超过 10MB");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(400, "仅支持 PDF/JPG/PNG/DOCX");
        }
        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Path dir = Paths.get(basePath, dateDir);
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(storedName).toAbsolutePath());
        } catch (IOException e) {
            throw new BusinessException(500, "文件保存失败");
        }
        return Result.success(Map.of(
                "url", "/api/file/" + dateDir + "/" + storedName,
                "name", original,
                "size", String.valueOf(file.getSize())
        ));
    }

    /** 访问已上传文件 */
    @GetMapping("/{dateDir}/{name:.+}")
    public ResponseEntity<FileSystemResource> serve(@PathVariable String dateDir, @PathVariable String name) {
        Path path = Paths.get(basePath, dateDir, name).toAbsolutePath().normalize();
        if (!path.startsWith(Paths.get(basePath).toAbsolutePath()) || !Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(path));
    }
}
