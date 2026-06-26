package com.miniblog.service;

import com.miniblog.dto.CommentRequest;
import com.miniblog.dto.CommentResponse;
import com.miniblog.dto.UserResponse;
import com.miniblog.entity.Comment;
import com.miniblog.entity.Post;
import com.miniblog.entity.User;
import com.miniblog.repository.CommentRepository;
import com.miniblog.repository.PostRepository;
import com.miniblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<CommentResponse> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId)
            .stream()
            .map(this::mapToCommentResponse)
            .toList();
    }

    public CommentResponse createComment(Long postId, CommentRequest request, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = Comment.builder()
            .body(request.getBody())
            .post(post)
            .user(user)
            .build();

        Comment savedComment = commentRepository.save(comment);
        return mapToCommentResponse(savedComment);
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
            .id(comment.getId())
            .body(comment.getBody())
            .user(mapToUserResponse(comment.getUser()))
            .postId(comment.getPost().getId())
            .createdAt(comment.getCreatedAt())
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
