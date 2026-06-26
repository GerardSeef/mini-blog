package com.miniblog.service;

import com.miniblog.entity.Like;
import com.miniblog.entity.Post;
import com.miniblog.entity.User;
import com.miniblog.repository.LikeRepository;
import com.miniblog.repository.PostRepository;
import com.miniblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public void likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        try {
            Like like = Like.builder()
                .post(post)
                .user(user)
                .build();
            likeRepository.save(like);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Already liked");
        }
    }

    public void unlikePost(Long postId, Long userId) {
        Optional<Like> like = likeRepository.findByUserIdAndPostId(userId, postId);
        like.ifPresentOrElse(
            likeRepository::delete,
            () -> {
                throw new IllegalArgumentException("Like not found");
            }
        );
    }

    public Long getLikeCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }
}
