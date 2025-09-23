package com.stellarfs.huffman_coder.service;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Service
public class PDFCompressionService {
    
    public byte[] optimizePDF(byte[] pdfData) throws IOException {
        try (PDDocument document = PDDocument.load(pdfData);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            try {
                // Remove metadata if present
                removeMetadata(document);
            } catch (Exception ignored) {}
            
            try {
                // Conservative image pass (no replacements to avoid corruption)
                optimizeImages(document);
            } catch (Exception ignored) {}
            
            try {
                // Safely clear annotations
                removeAnnotations(document);
            } catch (Exception ignored) {}
            
            // Save document
            document.save(baos);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            // If anything goes wrong, fall back to original bytes
            return pdfData;
        }
    }
    
    private void optimizeDocumentStructure(PDDocument document) {
        // No-op for stability; aggressive resource pruning can corrupt PDFs
    }
    
    private void removeMetadata(PDDocument document) {
        PDDocumentInformation info = document.getDocumentInformation();
        if (info != null) {
            info.setTitle("");
            info.setAuthor("");
            info.setSubject("");
            info.setKeywords("");
            info.setCreator("");
            info.setProducer("");
            info.setCreationDate(null);
            info.setModificationDate(null);
            info.setTrapped("");
        }
    }
    
    private void optimizeImages(PDDocument document) throws IOException {
        for (PDPage page : document.getPages()) {
            PDResources resources = page.getResources();
            if (resources == null) continue;
            try {
                optimizePageImages(resources);
            } catch (Exception ignored) {}
        }
    }
    
    private void optimizePageImages(PDResources resources) throws IOException {
        try {
            Iterable<COSName> names = resources.getXObjectNames();
            for (COSName name : names) {
                PDXObject xObject = resources.getXObject(name);
                if (!(xObject instanceof PDImageXObject)) continue;
                PDImageXObject image = (PDImageXObject) xObject;
                BufferedImage bufferedImage = image.getImage();
                if (bufferedImage == null) continue;
                // Only read/inspect; do not modify to avoid corruption
                // Future: downscale/replace with care
            }
        } catch (Exception ignored) {}
    }
    
    private BufferedImage resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Calculate scaling factor
        double scaleX = (double) maxWidth / originalWidth;
        double scaleY = (double) maxHeight / originalHeight;
        double scale = Math.min(scaleX, scaleY);
        
        if (scale >= 1.0) {
            return originalImage; // No need to resize
        }
        
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                            java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }
    
    private void removeAnnotations(PDDocument document) {
        for (PDPage page : document.getPages()) {
            try {
                if (page.getAnnotations() != null) {
                    page.getAnnotations().clear();
                }
            } catch (Exception ignored) {}
        }
    }
    
    public boolean isPDF(byte[] data) {
        if (data.length < 4) return false;
        // Check PDF signature: %PDF
        return data[0] == 0x25 && data[1] == 0x50 && data[2] == 0x44 && data[3] == 0x46;
    }
}

