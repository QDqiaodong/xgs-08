package com.bonsai.controller;

import com.bonsai.dto.Result;
import com.bonsai.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final ImageUtil imageUtil;

    @PostMapping("/image")
    public Result<String[]> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String[] urls = imageUtil.uploadImage(file);
            return Result.success(urls);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
