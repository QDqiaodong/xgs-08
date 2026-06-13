package com.bonsai.controller;

import com.bonsai.dto.Result;
import com.bonsai.entity.Comment;
import com.bonsai.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/post/{postId}")
    public Result<Page<Comment>> getPostComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.getPostComments(postId, page, size));
    }

    @PostMapping
    public Result<Comment> createComment(@RequestBody Comment comment) {
        return Result.success(commentService.createComment(comment));
    }
}
