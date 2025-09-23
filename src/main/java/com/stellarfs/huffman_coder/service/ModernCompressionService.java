package com.stellarfs.huffman_coder.service;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.encoder.Encoder;
import com.github.luben.zstd.Zstd;
import net.jpountz.lz4.LZ4Factory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

@Service
public class ModernCompressionService {
    
    @Autowired
    private PDFCompressionService pdfCompressionService;
    
    static {
        // Initialize Brotli
        try {
            Brotli4jLoader.ensureAvailability();
        } catch (Exception e) {
            System.err.println("Failed to initialize Brotli: " + e.getMessage());
        }
    }
    
    public byte[] compress(byte[] data, String fileName) throws IOException {
        String fileType = getFileExtension(fileName);
        
        // For PDFs: Use PDF-specific optimization + modern compression
        if ("pdf".equalsIgnoreCase(fileType) || pdfCompressionService.isPDF(data)) {
            try {
                return compressPDF(data);
            } catch (Exception e) {
                // Fallback: return original data on any PDF pipeline error
                return data;
            }
        }
        
        // For text files: Try both Huffman and modern compression
        if (isTextFile(fileType)) {
            return compressText(data);
        }
        
        // For other files: Use modern compression
        return compressBinary(data);
    }
    
    private byte[] compressPDF(byte[] data) throws IOException {
        // Step 1: Optimize PDF structure
        byte[] optimized = pdfCompressionService.optimizePDF(data);
        
        // Step 2: Try different compression algorithms with optimized parameters
        CompressionResult[] results = {
            new CompressionResult("ZSTD", zstdCompressPDF(optimized)),
            new CompressionResult("BROTLI", brotliCompressPDF(optimized)),
            new CompressionResult("DEFLATE", deflateCompressPDF(optimized)),
            new CompressionResult("LZ4", lz4Compress(optimized)),
            new CompressionResult("MULTI_STAGE", multiStageCompress(optimized))
        };
        
        // Step 3: Choose the best compression
        CompressionResult best = Arrays.stream(results)
            .min(Comparator.comparing(r -> r.compressedData.length))
            .orElse(new CompressionResult("ORIGINAL", optimized));
        
        return best.compressedData;
    }
    
    private byte[] multiStageCompress(byte[] data) {
        try {
            // Multi-stage compression: First ZSTD, then Brotli
            byte[] stage1 = Zstd.compress(data, 19);
            if (stage1.length < data.length) {
                Encoder.Parameters params = new Encoder.Parameters()
                    .setQuality(11)
                    .setWindow(24);
                return Encoder.compress(stage1, params);
            }
            return data;
        } catch (Exception e) {
            return data;
        }
    }
    
    private byte[] zstdCompressPDF(byte[] data) {
        try {
            // Use maximum compression level for PDFs
            return Zstd.compress(data, 22); // Level 22 for maximum compression
        } catch (Exception e) {
            return data; // Return original if compression fails
        }
    }
    
    private byte[] brotliCompressPDF(byte[] data) {
        try {
            // Use maximum quality for PDFs
            Encoder.Parameters params = new Encoder.Parameters()
                .setQuality(11)
                .setWindow(24); // Larger window for better compression
            return Encoder.compress(data, params);
        } catch (Exception e) {
            return data; // Return original if compression fails
        }
    }
    
    private byte[] deflateCompressPDF(byte[] data) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DeflaterOutputStream dos = new DeflaterOutputStream(baos, 
                 new Deflater(Deflater.BEST_COMPRESSION, true))) { // Use best compression
            dos.write(data);
            dos.finish();
            return baos.toByteArray();
        }
    }
    
    private byte[] compressText(byte[] data) throws IOException {
        // For text files, try both Huffman and modern compression
        CompressionResult[] results = {
            new CompressionResult("ZSTD", zstdCompress(data)),
            new CompressionResult("LZ4", lz4Compress(data)),
            new CompressionResult("BROTLI", brotliCompress(data)),
            new CompressionResult("DEFLATE", deflateCompress(data))
        };
        
        CompressionResult best = Arrays.stream(results)
            .min(Comparator.comparing(r -> r.compressedData.length))
            .orElse(new CompressionResult("ORIGINAL", data));
        
        return best.compressedData;
    }
    
    private byte[] compressBinary(byte[] data) throws IOException {
        // For binary files, use modern compression
        CompressionResult[] results = {
            new CompressionResult("ZSTD", zstdCompress(data)),
            new CompressionResult("LZ4", lz4Compress(data)),
            new CompressionResult("BROTLI", brotliCompress(data))
        };
        
        CompressionResult best = Arrays.stream(results)
            .min(Comparator.comparing(r -> r.compressedData.length))
            .orElse(new CompressionResult("ORIGINAL", data));
        
        return best.compressedData;
    }
    
    private byte[] zstdCompress(byte[] data) {
        try {
            return Zstd.compress(data, 19); // Level 19 for maximum compression
        } catch (Exception e) {
            return data; // Return original if compression fails
        }
    }
    
    private byte[] lz4Compress(byte[] data) {
        try {
            return LZ4Factory.fastestInstance().fastCompressor().compress(data);
        } catch (Exception e) {
            return data; // Return original if compression fails
        }
    }
    
    private byte[] brotliCompress(byte[] data) {
        try {
            Encoder.Parameters params = new Encoder.Parameters().setQuality(11); // Maximum quality
            return Encoder.compress(data, params);
        } catch (Exception e) {
            return data; // Return original if compression fails
        }
    }
    
    private byte[] deflateCompress(byte[] data) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DeflaterOutputStream dos = new DeflaterOutputStream(baos, new Deflater(Deflater.BEST_COMPRESSION))) {
            dos.write(data);
            dos.finish();
            return baos.toByteArray();
        }
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
    
    private boolean isTextFile(String fileType) {
        return Arrays.asList("txt", "csv", "log", "json", "xml", "html", "css", "js", "java", "py", "cpp", "c", "h").contains(fileType);
    }
    
    private static class CompressionResult {
        byte[] compressedData;
        
        CompressionResult(String algorithm, byte[] compressedData) {
            this.compressedData = compressedData;
        }
    }
}
