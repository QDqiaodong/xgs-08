package com.bonsai.service;

import com.bonsai.entity.Post;
import com.bonsai.entity.PostImage;
import com.bonsai.repository.PostImageRepository;
import com.bonsai.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.bonsai.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final ImageUtil imageUtil;

    public List<Post> getHotPosts() {
        Pageable pageable = PageRequest.of(0, 8);
        return postRepository.findHotPosts(pageable);
    }

    public Page<Post> getPosts(int page, int size, Long speciesId, Long styleId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (speciesId != null && styleId != null) {
            return postRepository.findBySpeciesIdAndStyleIdAndStatusOrderByCreatedAtDesc(speciesId, styleId, 1, pageable);
        } else if (speciesId != null) {
            return postRepository.findBySpeciesIdAndStatusOrderByCreatedAtDesc(speciesId, 1, pageable);
        } else if (styleId != null) {
            return postRepository.findByStyleIdAndStatusOrderByCreatedAtDesc(styleId, 1, pageable);
        }
        return postRepository.findByStatusOrderByCreatedAtDesc(1, pageable);
    }

    public Post getPostById(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);
            post.setImages(postImageRepository.findByPostIdOrderBySortOrderAsc(id));
        }
        return post;
    }

    @Transactional
    public Post createPost(Post post, List<MultipartFile> images) throws Exception {
        post.setStatus(1);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        Post savedPost = postRepository.save(post);

        if (images != null && !images.isEmpty()) {
            List<PostImage> postImages = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                String[] urls = imageUtil.uploadImage(file);
                PostImage postImage = new PostImage();
                postImage.setPostId(savedPost.getId());
                postImage.setImageUrl(urls[0]);
                postImage.setThumbnailUrl(urls[1]);
                postImage.setSortOrder(i);
                postImage.setIsCover(i == 0 ? 1 : 0);
                postImages.add(postImage);
            }
            postImageRepository.saveAll(postImages);
            savedPost.setImages(postImages);
        }

        return savedPost;
    }

    public Page<Post> getUserPosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByUserId(userId, pageable);
    }
}
