package com.miniblog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private String body;
    private UserResponse user;

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
