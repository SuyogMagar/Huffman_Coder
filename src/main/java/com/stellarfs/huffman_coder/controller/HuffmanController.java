package com.stellarfs.huffman_coder.controller;

import com.stellarfs.huffman_coder.service.HuffmanService;
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

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class HuffmanController {

    @Autowired
    private HuffmanService huffmanService;

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
} 