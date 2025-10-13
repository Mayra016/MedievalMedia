package com.MedievalMedia.Controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.MedievalMedia.Configurations.CustomUserDetails;
import com.MedievalMedia.Entities.Post;
import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Records.PostDAO;
import com.MedievalMedia.Records.PostsResponse;
import com.MedievalMedia.Records.UpdatePostDAO;
import com.MedievalMedia.Records.UpdatedPostResponse;
import com.MedievalMedia.Repositories.PostRepository;
import com.MedievalMedia.Repositories.UserRepository;
import com.MedievalMedia.Services.JwtTokenService;
import com.MedievalMedia.Services.PostService;
import com.MedievalMedia.Services.UserService;

@RestController
@RequestMapping("/api/v1")
public class PostController {
	
	private UserRepository userRepository;
	private PostRepository postRepository;
	private UserService userService;
	private PostService postService;
	private JwtTokenService jwtService;
	private Logger log = LoggerFactory.getLogger(PostController.class);
	
	@Autowired
	public PostController(UserRepository userRepository, PostRepository postRepository,
			UserService userService, PostService postService) {
		this.userRepository = userRepository;
		this.postRepository = postRepository;
		this.userService = userService;
		this.postService = postService;
	}
	
	/**
	 * Add one post to user's favorite posts
	 *
	 * @param post The post that will be added
	 * @param request Access headers to verify jwt token
	 * @return ResponseEntity with the petition status code
	 * @throws ResponseStatusException if user not found, post is invalid or post not found
	 */

	@PutMapping("/add-to-favorite")
	public ResponseEntity<String> addToFavorite(@RequestBody Post post, HttpRequest request) {
		try {
			String token = request.getHeaders().get("Authorization").toString().replace("Bearer: ", "");
			if (!this.jwtService.validateToken(token)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authorized");
			}
			String email = this.jwtService.extractEmail(token);
			this.postService.addPostToFavorite(post, email);
			
			return ResponseEntity.status(HttpStatus.OK).body("Success adding post to favorite");
			
		} catch (ResponseStatusException e) {
	        this.log.warn("Request failed: " + e.getReason());
	        return ResponseEntity
	                .status(e.getStatusCode())
	                .body(e.getReason());
		} catch (Exception e) {
			e.printStackTrace();
			this.log.error("Error adding post to favorite: " + post.getId());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding post to favorite");
		}
	}
	
	/**
	 * Delete one post
	 *
	 * @param postId The id of the post to delete
	 * @param request The HTTP request containing headers to verify the JWT token
	 * @return ResponseEntity containing a message of the HTTP status code
	 * @throws ResponseStatusException if post not found or if the email doesn't match the post's creator
	 */
	
	@DeleteMapping("/delete-post")
	public ResponseEntity<String> deletePost(@RequestBody long postId, HttpRequest request) {
		try {
			String token = request.getHeaders().get("Authorization").toString().replace("Bearer: ", "");
			if (!this.jwtService.validateToken(token)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
			}
			String email = this.jwtService.extractEmail(token);
			this.postService.deletePost(postId, email);
			
			
			return ResponseEntity.status(HttpStatus.OK).body("Post was successful deleted");
		} catch (ResponseStatusException e) {
			e.printStackTrace();
			this.log.warn("Request failed: " + e.getReason());
			return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error deleting post: " + postId);
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting post");
		}
	}
	
	/**
	 * Update a post
	 *
	 * @param updateInfo A DAO object containing the post, the new interaction type, and whether it is positive or negative
	 * @param request The HTTP request containing headers to verify the JWT token
	 * @return ResponseEntity containing a message of the HTTP status code and the updated post in case of success
	 * @throws ResponseStatusException if post not found or if the email doesn't match the post's creator
	 */
	
	@PutMapping("/update-post")
	public ResponseEntity<Post> updatePost(@RequestBody UpdatePostDAO updateInfo, HttpRequest request) {
		try {
			String token = request.getHeaders().get("Authorization").toString().replace("Bearer: ", "");
			if (!this.jwtService.validateToken(token)) {
				// Invalid credentials
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Post());
			}
			String email = this.jwtService.extractEmail(token);
			
			UpdatedPostResponse updatedPost = this.postService.updateInteractions(updateInfo, email);
			

			return updatedPost.response().getStatusCode() == HttpStatus.OK ? ResponseEntity.status(HttpStatus.OK).body(updatedPost.post()) : 
				ResponseEntity.status(updatedPost.response().getStatusCode()).body(new Post());
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error updating post's interactions");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Post());
		}
	}
	
	@GetMapping("/get-followed-posts")
	public ResponseEntity<List<Post>> getFollowPosts() {
		try {
			
			PostsResponse response = this.postService.getFollowedPosts(this.userService.getCurrentUserId());
			
			return ResponseEntity.status(response.exception().getStatusCode()).body(response.posts());
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error getting posts from followers");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of(new Post()));
		}
	}
	
	// read the answers of a letter
	@PostMapping("/get-post-answers")
	public ResponseEntity<List<Post>> getPostAnswers(@RequestBody Post post) {
		try {
			List<Post> posts = this.postService.getPostsAnswers(post);
			return ResponseEntity.status(HttpStatus.OK).body(posts);
			
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error getting post answers");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of(new Post()));
		}
	}
	
	// get last posts with pagination sorted by latest
	@PostMapping("/global-posts")
	public ResponseEntity<List<Post>> getLastPostsGlobaly(@RequestBody Post post) {
		try {
			List<Post> posts = this.postService.getLastPostsGlobaly(post);
			
			return ResponseEntity.status(HttpStatus.OK).body(posts);
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error getting global posts");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of( new Post()));
		}
	}
	
	
	// get last posts with pagination filtered by reign 
	@GetMapping("/posts")
	public ResponseEntity<List<Post>> getLastPostsByReign(@RequestParam String reign) {
		try {
			List<Post> result = this.postService.getLastPostsByReign(reign);
			
			return ResponseEntity.status(HttpStatus.OK).body(result);
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error getting 50 most recent posts in " + reign);
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of(new Post()));
		}
		
	}
	

	@PostMapping("/create-post")
	public ResponseEntity<String> insertData(@RequestBody PostDAO post) {
		try {
			UUID userId = this.userService.getCurrentUserId();
			
			try {
				Optional<User> searchUser = userRepository.findById(userId);
				
				if (searchUser.isPresent()) {
					User user = searchUser.get();
					Post newPost = new Post(user, post.greetings(), post.content(), post.reign(), post.language());
					this.postRepository.save(newPost);
					
					return ResponseEntity.status(HttpStatus.OK).body("Data was successful inserted");
				} else {
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
				}
			} catch(Exception e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating post");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			this.log.error("Error sending post");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknow error creating post");
		}
	}
	
	@GetMapping("/get-user-posts")
	public ResponseEntity<List<Post>> getPostsData(@RequestBody UUID userId) {
		try {
			Optional<User> searchUser = this.userRepository.findById(userId);
			
			if (searchUser.isPresent()) {
				List<Post> userPosts = this.postRepository.findAllByCreator(searchUser.get());
				return ResponseEntity.status(HttpStatus.OK).body(userPosts);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of(new Post()));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			this.log.error("Error getting posts data");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of(new Post()));
		}
	}
}
