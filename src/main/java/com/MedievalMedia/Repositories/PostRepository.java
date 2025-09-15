package com.MedievalMedia.Repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.MedievalMedia.Entities.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>{

	List<Post> findAllByUserId(UUID userId);

}
