package com.bonsai.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class ImageUtil {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    ));

    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"
    ));

    private static final Set<String> THUMBNAIL_NATIVE_FORMATS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png"
    ));

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Value("${bonsai.upload.path}")
    private String uploadPath;

    @Value("${bonsai.upload.url-prefix}")
    private String urlPrefix;

    @Value("${bonsai.image.thumbnail.width}")
    private int thumbnailWidth;

    @Value("${bonsai.image.thumbnail.height}")
    private int thumbnailHeight;

    @Value("${bonsai.image.quality}")
    private double quality;

    private void validateImageFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new IllegalArgumentException("文件名包含非法字符");
        }

        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == originalFilename.length() - 1) {
            throw new IllegalArgumentException("文件缺少合法扩展名");
        }

        String extension = originalFilename.substring(lastDotIndex).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件扩展名: " + extension + "，仅支持 " + ALLOWED_EXTENSIONS);
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小超过限制，最大支持 10MB");
        }

        validateMagicNumber(file, extension);
    }

    private void validateMagicNumber(MultipartFile file, String extension) throws IOException {
        byte[] header = new byte[12];
        try (InputStream is = file.getInputStream()) {
            int read = is.read(header);
            if (read < 4) {
                throw new IllegalArgumentException("文件内容异常，无法识别文件类型");
            }
        }

        boolean valid = false;
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                valid = (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF;
                break;
            case ".png":
                valid = (header[0] & 0xFF) == 0x89 && (header[1] & 0xFF) == 0x50
                        && (header[2] & 0xFF) == 0x4E && (header[3] & 0xFF) == 0x47;
                break;
            case ".gif":
                valid = (header[0] & 0xFF) == 0x47 && (header[1] & 0xFF) == 0x49
                        && (header[2] & 0xFF) == 0x46 && (header[3] & 0xFF) == 0x38;
                break;
            case ".bmp":
                valid = (header[0] & 0xFF) == 0x42 && (header[1] & 0xFF) == 0x4D;
                break;
            case ".webp":
                valid = (header[0] & 0xFF) == 0x52 && (header[1] & 0xFF) == 0x49
                        && (header[2] & 0xFF) == 0x46 && (header[3] & 0xFF) == 0x46
                        && (header[8] & 0xFF) == 0x57 && (header[9] & 0xFF) == 0x45
                        && (header[10] & 0xFF) == 0x42 && (header[11] & 0xFF) == 0x50;
                break;
            default:
                valid = false;
        }

        if (!valid) {
            throw new IllegalArgumentException("文件真实内容与扩展名不匹配，请上传合法的图片文件");
        }
    }

    public String[] uploadImage(MultipartFile file) throws IOException {
        validateImageFile(file);

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        String fileBaseName = UUID.randomUUID().toString();
        String fileName = fileBaseName + extension;

        Path originalDir = Paths.get(uploadPath, "original", datePath);
        Path thumbnailDir = Paths.get(uploadPath, "thumbnail", datePath);

        Files.createDirectories(originalDir);
        Files.createDirectories(thumbnailDir);

        Path originalPath = originalDir.resolve(fileName);
        file.transferTo(originalPath.toFile());

        String thumbnailFileName;
        Path thumbnailPath;
        boolean thumbnailGenerated = false;

        if (THUMBNAIL_NATIVE_FORMATS.contains(extension)) {
            thumbnailFileName = fileBaseName + extension;
            thumbnailPath = thumbnailDir.resolve(thumbnailFileName);
            try {
                Thumbnails.of(originalPath.toFile())
                        .size(thumbnailWidth, thumbnailHeight)
                        .outputQuality(quality)
                        .toFile(thumbnailPath.toFile());
                thumbnailGenerated = true;
            } catch (Exception e) {
                thumbnailGenerated = false;
            }
        }

        if (!thumbnailGenerated) {
            thumbnailFileName = fileBaseName + ".jpg";
            thumbnailPath = thumbnailDir.resolve(thumbnailFileName);
            try {
                BufferedImage originalImage = ImageIO.read(originalPath.toFile());
                if (originalImage != null) {
                    int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_RGB : originalImage.getType();
                    BufferedImage rgbImage = new BufferedImage(
                            originalImage.getWidth(),
                            originalImage.getHeight(),
                            type
                    );
                    rgbImage.createGraphics().drawImage(originalImage, 0, 0, null);

                    Thumbnails.of(rgbImage)
                            .size(thumbnailWidth, thumbnailHeight)
                            .outputFormat("jpg")
                            .outputQuality(quality)
                            .toFile(thumbnailPath.toFile());
                    thumbnailGenerated = true;
                }
            } catch (Exception e) {
                thumbnailGenerated = false;
            }
        }

        if (!thumbnailGenerated) {
            thumbnailFileName = fileBaseName + extension;
            thumbnailPath = thumbnailDir.resolve(thumbnailFileName);
            try {
                Files.copy(originalPath, thumbnailPath, StandardCopyOption.REPLACE_EXISTING);
                thumbnailGenerated = true;
            } catch (Exception e) {
                throw new IOException("缩略图生成失败，请尝试使用 JPG 或 PNG 格式的图片");
            }
        }

        String originalUrl = urlPrefix + "/original/" + datePath + "/" + fileName;
        String thumbnailUrl = urlPrefix + "/thumbnail/" + datePath + "/" + thumbnailFileName;

        return new String[]{originalUrl, thumbnailUrl};
    }

    public void deleteImage(String imageUrl) {
        try {
            String relativePath = imageUrl.replace(urlPrefix, "");
            Path filePath = Paths.get(uploadPath, relativePath);
            Files.deleteIfExists(filePath);

            String thumbPath = relativePath.replace("/original/", "/thumbnail/");
            Path thumbFilePath = Paths.get(uploadPath, thumbPath);
            Files.deleteIfExists(thumbFilePath);

            if (relativePath.contains("/original/")) {
                String basePath = thumbPath.substring(0, thumbPath.lastIndexOf("."));
                Path jpgThumbPath = Paths.get(uploadPath, basePath + ".jpg");
                Files.deleteIfExists(jpgThumbPath);
                Path pngThumbPath = Paths.get(uploadPath, basePath + ".png");
                Files.deleteIfExists(pngThumbPath);
            }
        } catch (Exception e) {
        }
    }

    public void deleteImages(String imagesStr) {
        if (imagesStr == null || imagesStr.trim().isEmpty()) {
            return;
        }
        for (String imageUrl : parseImages(imagesStr)) {
            deleteImage(imageUrl);
        }
    }

    public String[] parseImages(String imagesStr) {
        if (imagesStr == null || imagesStr.trim().isEmpty()) {
            return new String[0];
        }
        try {
            String trimmed = imagesStr.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                return new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(trimmed, String[].class);
            }
            if (trimmed.contains(",")) {
                return trimmed.split(",");
            }
            return new String[]{trimmed};
        } catch (Exception e) {
            return new String[]{imagesStr};
        }
    }
}
