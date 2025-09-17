package com.MedievalMedia.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import com.MedievalMedia.Entities.Post;
import com.MedievalMedia.Entities.User;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>{

	List<Post> findAllByCreator(User user);

	@Query("SELECT p FROM Post p WHERE p.reign = :reign ORDER BY p.date DESC")
	List<Post> findLastFifthyByReign(@Param("reign") String reign, Pageable pageable);

	@Query("SELECT p FROM Post p ORDER BY p.date DESC")
	List<Post> findLastFifthy(Pageable pageable);

	@Query("SELECT p FROM Post p WHERE (p.date < :date OR (p.date = :date AND p.id < :lastId)) ORDER BY p.date DESC, p.id DESC")
	List<Post> findTop50ByDateLessThanOrderByCreatedAtDesc(@Param("date") LocalDate date, @Param("lastId") long lastId);

	@Query("SELECT p FROM Post p WHERE p.parent = :post ORDER BY p.interactions.score DESC")
	List<Post> findAllByParent(@Param("post") Post post);

	@Query("SELECT p FROM Post p WHERE p.greetings = :greetings")
	Post findByGreetings(@Param("greetings") String greetings);

}
