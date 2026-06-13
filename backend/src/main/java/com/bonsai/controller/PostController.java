package com.bonsai.controller;

import com.bonsai.dto.Result;
import com.bonsai.entity.Post;
import com.bonsai.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/hot")
    public Result<List<Post>> getHotPosts() {
        return Result.success(postService.getHotPosts());
    }

    @GetMapping
    public Result<Page<Post>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long speciesId,
            @RequestParam(required = false) Long styleId) {
        return Result.success(postService.getPosts(page, size, speciesId, styleId));
    }

    @GetMapping("/{id}")
    public Result<Post> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        if (post == null) {
            return Result.error("帖子不存在");
        }
        return Result.success(post);
    }

    @PostMapping
    public Result<Post> createPost(
            @RequestPart Post post,
            @RequestPart(required = false) List<MultipartFile> images) {
        try {
            Post created = postService.createPost(post, images);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("发布失败: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public Result<Page<Post>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(postService.getUserPosts(userId, page, size));
    }
}
