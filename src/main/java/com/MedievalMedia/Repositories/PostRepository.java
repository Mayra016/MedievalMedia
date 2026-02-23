package com.MedievalMedia.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.MedievalMedia.Entities.Post;
import com.MedievalMedia.Entities.User;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>{
	
	Optional<Post> findById(long id);

	List<Post> findAllByCreator(User user);

	@Query("SELECT p FROM Post p WHERE p.reign = :reign ORDER BY p.date DESC")
	List<Post> findLastFifthyByReign(@Param("reign") String reign, Pageable pageable);

	@Query("SELECT p FROM Post p WHERE p.creator.id != :userId ORDER BY p.date DESC")
	List<Post> findLastFifthy(Pageable pageable, @Param("userId") UUID userId);

	@Query("SELECT p FROM Post p WHERE p.creator.id != :userId AND (p.date < :date OR (p.date = :date AND p.id < :lastId)) ORDER BY p.date DESC, p.id DESC")
	List<Post> findTop50ByDateLessThanOrderByCreatedAtDesc(@Param("date") LocalDate date, @Param("lastId") long l, Pageable pageable, @Param("userId") UUID userId);
		
	@Query("SELECT p FROM Post p WHERE p.parent = :post ORDER BY p.interactions.score DESC")
	Page<Post> findAllByParent(@Param("post") Post post);

	@Query("SELECT p FROM Post p WHERE p.greetings = :greetings")
	Post findByGreetings(@Param("greetings") String greetings);


	@Query("SELECT p FROM Post p WHERE p.creator IN :followedUsers ORDER BY p.date DESC")
	List<Post> findAllByCreatorInOrderByDateDesc(@Param("followedUsers") List<User> followedUsers, Pageable pageable);

	@Query("SELECT p.id FROM Post p ORDER BY p.date DESC")
	List<Long> findLastPostId(Pageable pageable);

	@Query("SELECT p FROM Post p WHERE p.creator.id = :userId ORDER BY p.date DESC")
	List<Post> findLastFifthyFromUser(Pageable pageable, @Param("userId") UUID currentUserId);

	@Query("SELECT p FROM Post p WHERE p.creator.id = :userId AND (p.date < :date OR (p.date = :date AND p.id < :lastId)) ORDER BY p.date DESC, p.id DESC")
	List<Post> findTop50ByDateLessThanOrderByCreatedAtDescByUser(@Param("date") LocalDate date, @Param("lastId") long lastPostId, Pageable pageable,
			@Param("userId") UUID currentUserId);

    @Query("SELECT p FROM Post p WHERE p.creatorId = :userId ORDER BY p.date DESC")
    Page<Post> findLastPostsFromUser(Pageable pageable, @Param("userId") UUID userId);
    
    @Query("SELECT p FROM Post p WHERE p.reign = :reign ORDER BY p.date DESC")
    Page<Post> findLastByReign(Pageable pageable, @Param("reign") String reign);
    
    @Query("SELECT p FROM Post p ORDER BY p.date DESC")
    Page<Post> findLastGlobalPosts(Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.creator IN :followed ORDER BY p.date DESC")
    Page<Post> findAllByFollowedInOrderByDateDesc(@Param("followed") List<User> followed, Pageable pageable);
    
    @Query("SELECT p FROM Post p  WHERE p.parent.id = :postId ORDER BY p.date DESC")
    Page<Post> findAllByParent(@Param("postId") UUID postId, Pageable pageable);
}
