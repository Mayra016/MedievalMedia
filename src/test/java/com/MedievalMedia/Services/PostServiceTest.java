package com.MedievalMedia.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.MedievalMedia.Entities.Post;
import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Enums.Language;
import com.MedievalMedia.Records.PostsResponse;
import com.MedievalMedia.Records.UpdatePostDAO;
import com.MedievalMedia.Records.UpdatedPostResponse;
import com.MedievalMedia.Repositories.PostRepository;
import com.MedievalMedia.Repositories.UserRepository;

@DataJpaTest
public class PostServiceTest {
	private PostRepository postRepository;
	private UserRepository userRepository;
	private PostService postService;
	private User user;

	@Autowired
	public PostServiceTest (PostRepository postRepository, UserRepository userRepository) {
		this.postRepository = postRepository;
		this.userRepository = userRepository;
	}
    
    

    @BeforeEach
    void initialize() {
    	this.postService = new PostService(postRepository, userRepository);
    	user = this.userRepository.save(new User());
    	user.setEmail("user1@test.com");
    	
        for (int i = 0; i < 61; i++) {
        	Post post = new Post(user, "Greeting " + i, "Test content", "Spain", Language.ESPAÑOL);
        	post.setDate(LocalDate.now().minusDays(200-i));
            postRepository.save(post);
        }
    }
    
    @Test
    void getFollowedPosts() {
    	User user2 = new User();
    	user2.setFollow(List.of(this.user));
    	user2 = this.userRepository.save(user2);
    	
    	PostsResponse response = this.postService.getFollowedPosts(user2.getId());
    	
    	assertEquals(HttpStatus.OK, response.exception().getStatusCode());
    	assertTrue(response.posts().size() > 2);
    }
    
    @Test
    void updateInteractionsOK() {
    	Post oldPost = this.postRepository.findByGreetings("Greeting 50");
    	UpdatePostDAO dao = new UpdatePostDAO(oldPost, "heart", true);
    	this.postService.updateInteractions(dao, "user1@test.com");

    	assertTrue(oldPost.getInteractions().getReactions().get("heart") > 0f);
    	
    }
    
    @Test
    void updateInteractionsPostNotFound() {
    	Post invalidPost = new Post();
    	invalidPost.setId(-456L);
    	UpdatePostDAO dao = new UpdatePostDAO(invalidPost, "heart", true);

    	UpdatedPostResponse exception = this.postService.updateInteractions(dao, "user1@test.com");
    
    	
    	assertEquals(HttpStatus.NOT_FOUND, exception.response().getStatusCode());
    	
    }
    
    @Test
    void updateInteractionsUnauthorized() {
    	Post post = this.postRepository.findByGreetings("Greeting 50");
    	UpdatePostDAO dao = new UpdatePostDAO(post, "heart", true);

    	UpdatedPostResponse exception = this.postService.updateInteractions(dao, "unauthorized@test.com");
    	
    	assertEquals(HttpStatus.UNAUTHORIZED, exception.response().getStatusCode());
    	
    }
    
    @Test
    void deletePostOK() {
    	Post post = postRepository.findByGreetings("Greeting 60");
    	this.postService.deletePost(post.getId(), post.getCreator().getEmail());
    	 
    	Optional<Post> search = this.postRepository.findById(post.getId());
    	assertTrue(search.isEmpty());
    }
    
    @Test
    void deletePostUnauthorizedUser() {
    	Post post = postRepository.findByGreetings("Greeting 60");

    	ResponseStatusException exception = assertThrows(
    			ResponseStatusException.class,
    	        () -> postService.deletePost(post.getId(), "invalidEmail.com")
    	);
    	 
    	assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
    
    @Test
    void deletePostNotFound() {
    	Post post = new Post();
    	post.setId(-456L);

    	ResponseStatusException exception = assertThrows(
    			ResponseStatusException.class,
    	        () -> postService.deletePost(post.getId(), "email.com")
    	);
    	 
    	assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
    
    @Test
    void addToFavoriteOK() {
    	User user2 = new User();
    	user2.setEmail("user2@test.com");
    	this.userRepository.save(user2);
    	Post post = postRepository.findByGreetings("Greeting 4");
    	this.postService.addPostToFavorite(post, "user2@test.com");

    	user2 = this.userRepository.findById(user2.getId()).get();
    	assertTrue(user2.getFavoritePosts().contains(post));
    }
    
    @Test
    void addToFavoritePostNotFoundError() {
    	User user2 = new User();
    	user2.setEmail("user2@test.com");
    	this.userRepository.save(user2);
    	Post nonExisting = new Post();
    	nonExisting.setId((long)-499);
    	ResponseStatusException exception = assertThrows(
    	        ResponseStatusException.class,
    	        () -> postService.addPostToFavorite(nonExisting, "user@test2.com")
    	);
    	
    	assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
    
    @Test
    void addToFavoriteUserNotFoundError() {
    	User user2 = new User();
    	user2.setEmail("user2@test.com");
    	this.userRepository.save(user2);
    	Post post = postRepository.findByGreetings("Greeting 4");
    	ResponseStatusException exception = assertThrows(
    	        ResponseStatusException.class,
    	        () -> postService.addPostToFavorite(post, "user@test2.com")
    	    );
    	assertEquals(exception.getStatusCode(), HttpStatus.NOT_FOUND);
    }
    
    @Test
    void addToFavoriteCorruptedPostError() {
    	User user2 = new User();
    	user2.setEmail("user2@test.com");
    	this.userRepository.save(user2);
    	Post post = postRepository.findByGreetings("Greeting 4");
    	Post fakePost = new Post();
    	fakePost.setGreetings("Greeting 4");
    	fakePost.setId(post.getId());
    	fakePost.setContent("changed content");
    	ResponseStatusException exception = assertThrows(
    	        ResponseStatusException.class,
    	        () -> postService.addPostToFavorite(fakePost, "user2@test.com")
    	    );
    	assertEquals(exception.getStatusCode(), HttpStatus.CONFLICT);
    }

    @Test
    void testFindLastFifthyByReign() {
        List<Post> posts = postRepository.findLastFifthyByReign("Spain", PageRequest.of(0, 50));
        assertEquals(50, posts.size()); // verify if the method retrieve just the last 50      
        assertEquals("Greeting 59", posts.get(1).getGreetings()); // check if posts are order form latest to oldest
    }
    
    @Test
    void getLastPostsGlobalyTest() {
    	List<Post> posts = new ArrayList<>();
    
    	User user = this.userRepository.save(new User());
        for (int i = 62; i < 100; i++) {
        	Post post = new Post(user, "Greeting " + i, "Test content", "Spain", Language.ESPAÑOL);
        	post.setDate(LocalDate.now().minusDays(100-i));
            postRepository.save(post);
        }
        
        posts = this.postRepository.findLastFifthy(PageRequest.of(0,50));
        
        System.out.println("Most recent post from last call" + posts.get(0).getGreetings());
        
        assertEquals("Greeting 99", posts.get(0).getGreetings());
        
        System.out.println("Oldest post from last call " + posts.get(posts.size() - 2).getGreetings());
        posts = this.postRepository.findTop50ByDateLessThanOrderByCreatedAtDesc(posts.get(posts.size() - 2).getDate(), posts.get(posts.size() - 2).getId());
        
        System.out.println("Most recent post from second call" + posts.get(0).getGreetings());
        
        assertEquals("Greeting 49", posts.get(0).getGreetings());
              
    }
    
    
    @Test
    void getPostsAnswersTest() {
    	Post parentPost = this.postRepository.findByGreetings("Greeting 2");
    	Post answer1 = new Post(this.user, "Answer 1", "Nice letter", "England", Language.ENGLISH, parentPost);
    	Post answer2 = new Post(this.user, "Answer 2", "Nice letter", "Spain", Language.ENGLISH, parentPost);
    	Post answer3 = new Post(this.user, "Answer 3", "Nice letter", "Spain", Language.ENGLISH, parentPost);
    	    	
    	answer2.updateInteractions("crown", true);
    	answer3.updateInteractions("heart", true);
    	
    	answer1 = this.postRepository.save(answer1);
    	answer2 = this.postRepository.save(answer2);
    	answer3 = this.postRepository.save(answer3);
    	
    	
    	
    	List<Post> posts = this.postRepository.findAllByParent(parentPost);
    	System.out.println(" Interactions: " + answer2.getInteractions().getScore());
    	System.out.println(" Size: " + posts.size());
    	System.out.println(" First: " + posts.get(0));
    	
    	assertEquals(List.of(answer2, answer3, answer1), posts);
    }
    
    @Test
    void getPostsFromFollowsTest() {
    	List<Post> posts = new ArrayList<>();
    	User user1 = new User();
    	User user2 = new User();
    	User user3 = new User();

    	  	
    	user1.setUsername("Test 1");
    	user2.setUsername("Test 2");
    	user3.setUsername("Test 3");
    	
    	
    	userRepository.save(user1);
    	userRepository.save(user2);
    	userRepository.save(user3);
    	
    	for ( byte i = 0; i < 9; i++) {
    		Post post = postRepository.findByGreetings("Greeting "+i);
    		post.setDate(LocalDate.now().minusDays(300 - i));
    		
    		if (i < 3 && i > 0) {
    			post.setCreator(user1);
    			post = postRepository.save(post);
    		}
    		
    		if (i >= 3 && i < 6) {
    			post.setCreator(user2);
    			post = postRepository.save(post);
    			posts.add(post);
    		}
    		
    		if (i >= 6 && i < 9) {
    			post.setCreator(user3);
    			post = postRepository.save(post);
    			posts.add(post);
    		}
    		
    		
    	}
    	
    	user1.setFollow(new ArrayList<>(Arrays.asList(user2, user3)));
    	
    	userRepository.save(user1);
    	
    	List<Post> followPosts = postRepository.findAllByCreatorInOrderByDateDesc(user1.getFollow(), PageRequest.of(0, 50));
    	Collections.reverse(posts); // sort from newest to older
    	System.out.println("Follow : " + followPosts.get(0));
    	System.out.println(posts.get(0));
    	assertEquals(followPosts, posts);
    }
}

