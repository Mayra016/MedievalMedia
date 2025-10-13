package com.MedievalMedia.Services;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.security.cert.X509Certificate;
import java.time.LocalDate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.MedievalMedia.Entities.Post;
import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Records.PostDAO;
import com.MedievalMedia.Records.PostsResponse;
import com.MedievalMedia.Records.TestDAO;
import com.MedievalMedia.Records.UpdatePostDAO;
import com.MedievalMedia.Records.UpdatedPostResponse;
import com.MedievalMedia.Repositories.PostRepository;
import com.MedievalMedia.Repositories.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final KafkaProducer<String, String> producer;
    private final Logger log = LoggerFactory.getLogger(PostService.class);

    @Autowired
    public PostService(PostRepository postRepository,
    		UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;

        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        this.producer = new KafkaProducer<>(props);
    }

	
	public String fromObjectToJSON(Post newPost) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule()); // para LocalDate, LocalDateTime
	    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // ignora campos null
	    mapper.findAndRegisterModules();
	    
		String value = "";
		try {
			value = mapper.writeValueAsString(newPost);
		} catch (JsonProcessingException e) {
			this.log.error("Error converting from object to  json string");
			e.printStackTrace();
		}
		return value;

	}
	
	public void sendPost(String topic, String key, Post post) {
		ObjectMapper mapper = new ObjectMapper();
		String value;
		try {
			this.postRepository.save(post);
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error saving post in data base: " + post.getId());
		}
		
		try {
			value = mapper.writeValueAsString(post);
			ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
	        producer.send(record);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			this.log.error("Error converting post into json");
		}

	}
	
	public Post getPosts(String json) {
		ObjectMapper mapper = new ObjectMapper();
        try {
			Post post = mapper.readValue(json, Post.class);
			return post;
		} catch (JsonMappingException e) {
			this.log.error("Error mapping from json to post object");
			e.printStackTrace();
			return new Post();
		} catch (JsonProcessingException e) {
			this.log.error("Error processing from json to post object");
			e.printStackTrace();
			return new Post();
		}
	}


	public List<Post> getLastPostsByReign(String reign) {
		try {
			return this.postRepository.findLastFifthyByReign(reign, PageRequest.of(0,50));
		} catch (Exception e) {
			e.printStackTrace();
			this.log.error("Error getting 50 most recent posts in " + reign);
			
			return List.of(new Post());			
		}
		
	}


	public List<Post> getLastPostsGlobaly(Post post) {
		try {
			if (post.getDate() != LocalDate.of(1111, 11, 11)) {
				List<Post> posts = this.postRepository.findTop50ByDateLessThanOrderByCreatedAtDesc(post.getDate(), post.getId());
				return posts;
			} else {
				List<Post> posts = this.postRepository.findLastFifthy(PageRequest.of(0, 50));
				return posts;
			}
						
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error getting last posts globaly");
			
			return List.of(new Post());
		}
	}


	public List<Post> getPostsAnswers(Post post) {
		try {
			return this.postRepository.findAllByParent(post);
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error getting post answers");
			
			return List.of(new Post());
		}
	}

	/**
	 * Update a post
	 *
	 * @param updateInfo A DAO object containing the post, the new interaction type, and whether it is positive or negative
	 * @param email Email of the user performing the interaction
	 * @return UpdatedPostResponse containing a message of the HTTP status code and the post in case of success
	 * @throws ResponseStatusException 
	 * 	NOT_FOUND: if post not found
	 * 	UNAUTHORIZED: if the extracted email doesn't match the post's creator
	 */

	public UpdatedPostResponse updateInteractions(UpdatePostDAO updateInfo, String email) throws ResponseStatusException {
		Post post = updateInfo.post();
		Optional<Post> searchPost = postRepository.findById(post.getId());
		
		if (searchPost.isEmpty()) {
			return new UpdatedPostResponse(new Post(), new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
		}
		
		Post originalPost = searchPost.get();
		
		if (post.getCreator().getEmail().equals(email) && originalPost.getCreator().getEmail().equals(email)) {
			originalPost.getInteractions().updateReactions(updateInfo.reaction(), updateInfo.up());
			this.postRepository.save(originalPost);
		} else {
			return new UpdatedPostResponse(new Post(), new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only the post's creator can update its post"));
		}
			
			
		return new UpdatedPostResponse(post, new ResponseStatusException(HttpStatus.OK));
	}


	public List<Post> getPostsFromFollowed(User user) {
		try {
			return this.postRepository.findAllByCreatorInOrderByDateDesc(user.getFollow(), PageRequest.of(0, 50));
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error getting posts from follows");
			
			return List.of(new Post());
		}
	}
	
	/**
	 * Add one post to user's favorite posts
	 *
	 * @param post The post that will be added
	 * @param email Email of the user that marked this post as favorite
	 * @throws ResponseStatusException 
	 * 	NOT_FOUND if user or post not found
	 *  CONFLICT if post content doesn't match database
	 */

	public void addPostToFavorite(Post post, String email) throws ResponseStatusException {

			Optional<User> searchUser = this.userRepository.findByEmail(email);
			Post searchPost = this.postRepository.findById(post.getId())
			        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

			if (searchUser.isPresent()) {
				User user = searchUser.get();
				if (post.getContent().equals(searchPost.getContent())) {
					user.addToFavorites(searchPost);
				} else {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Post doesn't match any know posts");
				}
				
			} else {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
			}
			
	
	}

	/**
	 * Delete one post
	 *
	 * @param postId The id of the post to delete
	 * @param email The email extracted from the JWT token of the requesting user
	 * @throws ResponseStatusException 
	 * 	NOT_FOUND: if post not found
	 *  UNAUTHORIZED: if the email doesn't match the creator of the post
	 */
	public void deletePost(long postId, String email) throws ResponseStatusException  {
		Post post = this.postRepository.findById(postId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
		
		if (post.getCreator().getEmail().equals(email)) {
			this.postRepository.deleteById(postId);
		} else {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only the post's creator can delete its post");
		}		
	}

	/**
	 * Get followed posts
	 *
	 * @param id The id of the user who requested the posts
	 * @return PostsResponse containing a message of the HTTP status code and the followed post in case of success
	 * @throws ResponseStatusException 
	 * 	NOT_FOUND: if post or user not found
	 */

	public PostsResponse getFollowedPosts(UUID id) {
		Optional<User> searchUser = this.userRepository.findById(id);
		
		if (searchUser.isPresent()) {
			User user = searchUser.get();
			try {
				List<Post> posts =  this.postRepository.findAllByCreatorInOrderByDateDesc(user.getFollow(), PageRequest.of(0, 50));
				return new PostsResponse(posts, new ResponseStatusException(HttpStatus.OK, "Success getting followed posts"));
			} catch(Exception e) {
				e.printStackTrace();
				this.log.error("Error getting posts from follows");
				
				return new PostsResponse(List.of(new Post()), new ResponseStatusException(HttpStatus.NOT_FOUND, "Posts not found"));
			}
			
		} else {
			return new PostsResponse(List.of(new Post()), new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		}	
		
		
	}
}
