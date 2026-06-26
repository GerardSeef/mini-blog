package com.miniblog.repository;

import com.miniblog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.comments LEFT JOIN FETCH p.likes")
    Page<Post> findAllWithDetails(Pageable pageable);
}
