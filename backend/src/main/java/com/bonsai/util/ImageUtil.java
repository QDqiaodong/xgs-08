package com.bonsai.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class ImageUtil {

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

    public String[] uploadImage(MultipartFile file) throws IOException {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;

        Path originalDir = Paths.get(uploadPath, "original", datePath);
        Path thumbnailDir = Paths.get(uploadPath, "thumbnail", datePath);

        Files.createDirectories(originalDir);
        Files.createDirectories(thumbnailDir);

        Path originalPath = originalDir.resolve(fileName);
        Path thumbnailPath = thumbnailDir.resolve(fileName);

        file.transferTo(originalPath.toFile());

        Thumbnails.of(originalPath.toFile())
                .size(thumbnailWidth, thumbnailHeight)
                .outputQuality(quality)
                .toFile(thumbnailPath.toFile());

        String originalUrl = urlPrefix + "/original/" + datePath + "/" + fileName;
        String thumbnailUrl = urlPrefix + "/thumbnail/" + datePath + "/" + fileName;

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
        } catch (Exception e) {
        }
    }
}
