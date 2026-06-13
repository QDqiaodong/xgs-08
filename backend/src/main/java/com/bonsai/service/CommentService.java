package com.bonsai.service;

import com.bonsai.entity.Comment;
import com.bonsai.repository.CommentRepository;
import com.bonsai.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public Page<Comment> getPostComments(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return commentRepository.findByPostIdAndParentIdIsNullAndStatusOrderByCreatedAtDesc(postId, 1, pageable);
    }

    @Transactional
    public Comment createComment(Comment comment) {
        comment.setStatus(1);
        comment.setLikeCount(0);
        Comment savedComment = commentRepository.save(comment);

        postRepository.findById(comment.getPostId()).ifPresent(post -> {
            post.setCommentCount(post.getCommentCount() + 1);
            postRepository.save(post);
        });

        return savedComment;
    }
}
