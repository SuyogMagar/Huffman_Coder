package com.stellarfs.huffman_coder.controller;

import com.stellarfs.huffman_coder.service.HuffmanService;
import com.stellarfs.huffman_coder.service.HybridCompressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api")
public class HuffmanController {

    @Autowired
    private HuffmanService huffmanService;
    
    @Autowired
    private HybridCompressionService hybridCompressionService;

    @PostMapping("/compress")
    public ResponseEntity<byte[]> compressFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        byte[] compressedData = huffmanService.compress(file.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", file.getOriginalFilename() + ".huf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(compressedData);
    }

    @PostMapping("/decompress")
    public ResponseEntity<byte[]> decompressFile(@RequestParam("file") MultipartFile file) throws IOException, ClassNotFoundException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        byte[] decompressedData = huffmanService.decompress(file.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", file.getOriginalFilename().replace(".huf", ""));

        return ResponseEntity.ok()
                .headers(headers)
                .body(decompressedData);
    }
    
    @PostMapping("/smart-compress")
    public ResponseEntity<byte[]> smartCompressFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        HybridCompressionService.CompressionResult result = hybridCompressionService.compress(file.getBytes(), file.getOriginalFilename());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        
        if (result.wasCompressed) {
            headers.setContentDispositionFormData("attachment", file.getOriginalFilename() + ".compressed");
            headers.set("X-Compression-Method", result.method);
            headers.set("X-Compression-Ratio", String.format("%.2f%%", result.compressionRatio));
            headers.set("X-Original-Size", String.valueOf(result.originalSize));
            headers.set("X-Compressed-Size", String.valueOf(result.compressedSize));
            headers.set("X-Space-Saved", String.valueOf(result.originalSize - result.compressedSize));
        } else {
            headers.setContentDispositionFormData("attachment", file.getOriginalFilename());
            headers.set("X-Compression-Status", "No compression achieved - returning original");
            headers.set("X-Compression-Method", "NONE");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(result.compressedData);
    }
} 