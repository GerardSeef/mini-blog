package com.miniblog.controller;

import com.miniblog.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/posts/{postId}/like")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<?> likePost(@PathVariable Long postId) {
        try {
            Long userId = getCurrentUserId();
            likeService.likePost(postId, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Post liked successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Already liked")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Already liked"));
            }
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<?> unlikePost(@PathVariable Long postId) {
        try {
            Long userId = getCurrentUserId();
            likeService.unlikePost(postId, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Post unliked successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
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
