package com.stellarfs.huffman_coder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HybridCompressionService {
    
    @Autowired
    private HuffmanService huffmanService;
    
    @Autowired
    private ModernCompressionService modernCompressionService;
    
    public CompressionResult compress(byte[] data, String fileName) throws IOException {
        String fileType = getFileExtension(fileName);
        long originalSize = data.length;
        
        // For PDFs: Use modern compression (better for PDFs)
        if ("pdf".equalsIgnoreCase(fileType)) {
            byte[] compressed = modernCompressionService.compress(data, fileName);
            return new CompressionResult(compressed, "MODERN", originalSize, compressed.length);
        }
        
        // For text files: Try both Huffman and modern compression
        if (isTextFile(fileType)) {
            byte[] huffmanResult = huffmanService.compress(data);
            byte[] modernResult = modernCompressionService.compress(data, fileName);
            
            if (huffmanResult.length < modernResult.length) {
                return new CompressionResult(huffmanResult, "HUFFMAN", originalSize, huffmanResult.length);
            } else {
                return new CompressionResult(modernResult, "MODERN", originalSize, modernResult.length);
            }
        }
        
        // For other files: Try both and pick the best
        byte[] huffmanResult = huffmanService.compress(data);
        byte[] modernResult = modernCompressionService.compress(data, fileName);
        
        if (huffmanResult.length < modernResult.length) {
            return new CompressionResult(huffmanResult, "HUFFMAN", originalSize, huffmanResult.length);
        } else {
            return new CompressionResult(modernResult, "MODERN", originalSize, modernResult.length);
        }
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
    
    private boolean isTextFile(String fileType) {
        return java.util.Arrays.asList("txt", "csv", "log", "json", "xml", "html", "css", "js", "java", "py", "cpp", "c", "h").contains(fileType);
    }
    
    public static class CompressionResult {
        public final byte[] compressedData;
        public final String method;
        public final long originalSize;
        public final long compressedSize;
        public final double compressionRatio;
        public final boolean wasCompressed;
        
        public CompressionResult(byte[] compressedData, String method, long originalSize, long compressedSize) {
            this.compressedData = compressedData;
            this.method = method;
            this.originalSize = originalSize;
            this.compressedSize = compressedSize;
            this.compressionRatio = originalSize > 0 ? (double)(originalSize - compressedSize) / originalSize * 100 : 0;
            this.wasCompressed = compressedSize < originalSize;
        }
    }
}

