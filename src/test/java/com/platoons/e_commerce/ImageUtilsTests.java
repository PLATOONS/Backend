package com.platoons.e_commerce;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import com.platoons.e_commerce.utils.ImageUtils;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class ImageUtilsTests {

    private final ImageUtils imageUtils = new ImageUtils();

    @Mock
    private MultipartFile mockFile;

    // Valid image tests
    @Test
    void fileIsImage_ValidJpeg_ReturnsTrue() throws Exception {
        // JPEG file signature (first 8 bytes)
        byte[] jpegSignature = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};
        when(mockFile.getBytes()).thenReturn(jpegSignature);

        assertTrue(imageUtils.fileIsImage(mockFile));
    }

    @Test
    void fileIsImage_ValidPng_ReturnsTrue() throws Exception {
        // PNG file signature (first 8 bytes)
        byte[] pngSignature = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        when(mockFile.getBytes()).thenReturn(pngSignature);

        assertTrue(imageUtils.fileIsImage(mockFile));
    }

    // Invalid file tests
    @Test
    void fileIsImage_InvalidFileType_ReturnsFalse() throws Exception {
        // Text file content (not an image)
        byte[] textContent = "Hello World".getBytes();
        when(mockFile.getBytes()).thenReturn(textContent);

        assertFalse(imageUtils.fileIsImage(mockFile));
    }

    @Test
    void fileIsImage_EmptyFile_ReturnsFalse() throws Exception {
        byte[] emptyContent = {};
        when(mockFile.getBytes()).thenReturn(emptyContent);

        assertFalse(imageUtils.fileIsImage(mockFile));
    }

    @Test
    void fileIsImage_ExceptionThrown_ReturnsFalse() throws Exception {
        when(mockFile.getBytes()).thenThrow(new RuntimeException("File read error"));

        assertFalse(imageUtils.fileIsImage(mockFile));
    }

    // Edge case tests
    @Test
    void fileIsImage_ShortFile_ReturnsFalse() throws Exception {
        // File with only 4 bytes (shorter than required signature length)
        byte[] shortContent = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        when(mockFile.getBytes()).thenReturn(shortContent);

        assertFalse(imageUtils.fileIsImage(mockFile));
    }

    @Test
    void fileIsImage_RealJpegFile_ReturnsTrue() {
        byte[] jpegSignature = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};
        MultipartFile file = new MockMultipartFile(
            "test.jpg", "test.jpg", "image/jpeg", jpegSignature
        );

        assertTrue(imageUtils.fileIsImage(file));
    }

    @Test
    void fileIsImage_RealTextFile_ReturnsFalse() {
        byte[] textContent = "Not an image".getBytes();
        MultipartFile file = new MockMultipartFile(
            "test.txt", "test.txt", "text/plain", textContent
        );

        assertFalse(imageUtils.fileIsImage(file));
    }
}