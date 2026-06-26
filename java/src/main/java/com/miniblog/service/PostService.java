package com.miniblog.service;

import com.miniblog.dto.PostRequest;
import com.miniblog.dto.PostResponse;
import com.miniblog.dto.UserResponse;
import com.miniblog.entity.Post;
import com.miniblog.entity.User;
import com.miniblog.repository.CommentRepository;
import com.miniblog.repository.LikeRepository;
import com.miniblog.repository.PostRepository;
import com.miniblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public Page<PostResponse> getAllPosts(Pageable pageable) {
        return postRepository.findAllWithDetails(pageable)
            .map(this::mapToPostResponse);
    }

    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return mapToPostResponse(post);
    }

    public PostResponse createPost(PostRequest request, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Post post = Post.builder()
            .title(request.getTitle())
            .body(request.getBody())
            .user(user)
            .build();

        Post savedPost = postRepository.save(post);
        return mapToPostResponse(savedPost);
    }

    public PostResponse updatePost(Long id, PostRequest request, Long userId) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        post.setTitle(request.getTitle());
        post.setBody(request.getBody());

        Post updatedPost = postRepository.save(post);
        return mapToPostResponse(updatedPost);
    }

    public void deletePost(Long id, Long userId) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        postRepository.delete(post);
    }

    private PostResponse mapToPostResponse(Post post) {
        return PostResponse.builder()
            .id(post.getId())
            .title(post.getTitle())
            .body(post.getBody())
            .user(mapToUserResponse(post.getUser()))
            .commentsCount((long) post.getComments().size())
            .likesCount((long) post.getLikes().size())
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
