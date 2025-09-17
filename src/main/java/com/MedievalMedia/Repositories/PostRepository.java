package com.MedievalMedia.Repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.Pageable;
import com.MedievalMedia.Entities.Post;
import com.MedievalMedia.Entities.User;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>{

	List<Post> findAllByCreator(User user);

	@Query("SELECT p FROM Post p WHERE p.reign = :reign ORDER BY p.date ASC")
	List<Post> findLastFifthyByReign(@Param("reign") String reign, Pageable pageable);

}
