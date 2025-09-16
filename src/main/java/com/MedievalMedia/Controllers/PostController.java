package com.MedievalMedia.Controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MedievalMedia.Configurations.CustomUserDetails;
import com.MedievalMedia.Entities.Post;
import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Records.PostDAO;
import com.MedievalMedia.Repositories.PostRepository;
import com.MedievalMedia.Repositories.UserRepository;

@RestController
@RequestMapping("/api/v1")
public class PostController {
	
	private UserRepository userRepository;
	private PostRepository postRepository;
	private Logger log = LoggerFactory.getLogger(PostController.class);
	
	public PostController(UserRepository userRepository, PostRepository postRepository) {
		this.userRepository = userRepository;
		this.postRepository = postRepository;
	}

	@PostMapping("create-post")
	public ResponseEntity<String> insertData(@RequestBody PostDAO post) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
			UUID userId = (UUID) userDetails.getId();
			
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
			List<Post> userPosts = this.postRepository.findAllByUserId(userId);
			return ResponseEntity.status(HttpStatus.OK).body(userPosts);
		} catch (Exception e) {
			e.printStackTrace();
			this.log.error("Error getting posts data");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of(new Post()));
		}
	}
}
