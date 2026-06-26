package com.miniblog.controller;

import com.miniblog.dto.PostRequest;
import com.miniblog.dto.PostResponse;
import com.miniblog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int per_page) {
        Pageable pageable = PageRequest.of(page, per_page);
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(postService.getPostById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        Long userId = getCurrentUserId();
        PostResponse response = postService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {
        try {
            Long userId = getCurrentUserId();
            PostResponse response = postService.updatePost(id, request, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            postService.deletePost(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        return Long.valueOf(username.split(":")[0]);
    }
}
