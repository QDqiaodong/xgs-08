package com.bonsai.service;

import com.bonsai.entity.Like;
import com.bonsai.entity.Post;
import com.bonsai.repository.LikeRepository;
import com.bonsai.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    public boolean hasLiked(Long userId, String targetType, Long targetId) {
        return likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }

    @Transactional
    public void toggleLike(Long userId, String targetType, Long targetId) {
        if (likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)) {
            likeRepository.deleteByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
            if ("post".equals(targetType)) {
                postRepository.findById(targetId).ifPresent(post -> {
                    post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                    postRepository.save(post);
                });
            }
        } else {
            Like like = new Like();
            like.setUserId(userId);
            like.setTargetType(targetType);
            like.setTargetId(targetId);
            likeRepository.save(like);
            if ("post".equals(targetType)) {
                postRepository.findById(targetId).ifPresent(post -> {
                    post.setLikeCount(post.getLikeCount() + 1);
                    postRepository.save(post);
                });
            }
        }
    }
}
