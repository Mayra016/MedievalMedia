package com.MedievalMedia.Controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.kafka.core.KafkaTemplate;

import com.MedievalMedia.Configurations.CustomUserDetails;
import com.MedievalMedia.Entities.Post;
import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Enums.Language;
import com.MedievalMedia.Records.PostDAO;
import com.MedievalMedia.Records.TestDAO;
import com.MedievalMedia.Repositories.PostRepository;
import com.MedievalMedia.Repositories.UserRepository;
import com.MedievalMedia.Services.KafkaService;
import com.MedievalMedia.Services.PostService;
import com.MedievalMedia.Services.UserService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.annotation.PartitionOffset;


import jakarta.annotation.security.PermitAll;

@RestController
@RequestMapping("/api/v1")
public class KafkaRestAPI {
	private PostService productService;
	private UserRepository userRepository;
	private KafkaService kafkaService;
	private UserService userService;
	private List<String> kafkaPosts = new ArrayList<>();
	private Logger log = LoggerFactory.getLogger(KafkaRestAPI.class);
	
    @Autowired
    public KafkaRestAPI(UserRepository userRepository,
                        PostService productService,
                        KafkaService kafkaService,
                        UserService userService) {
        this.userRepository = userRepository;
        this.productService = productService;
        this.kafkaService = kafkaService;
        this.userService = userService;
    }

    
    @KafkaListener(topics = "POSTS", groupId = "medieval_media")
    public void listen(String message) {
        log.info("Received message: " + message);
        this.kafkaPosts.add(message);
    }
    
    @GetMapping("getPostFromKafka")
	public ResponseEntity<List<String>> getTestPost() {
		try {
			return ResponseEntity.status(HttpStatus.OK).body(this.kafkaPosts);
		} catch (Exception e) {
			e.printStackTrace();
			this.log.error("Error getting post from Kafka");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of("Error getting post from Kafka"));
		}
	}
    
	@PostMapping("sendPostToKafka")
	public ResponseEntity<String> sendPostToKafka(@RequestBody PostDAO post) {
		try {
			UUID userId = this.userService.getCurrentUserId();
					
			try {
				Optional<User> searchUser = userRepository.findById(userId);
				
				if (searchUser.isPresent()) {
					User user = searchUser.get();
					Post newPost = new Post(user, post.greetings(), post.content(), post.reign(), post.language());
					String finalPost = this.productService.fromObjectToJSON(newPost);
					this.kafkaService.sendMessage("POSTS", finalPost);
					
					return ResponseEntity.status(HttpStatus.OK).body("Data was successful inserted");
				} else {
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
				}
			} catch(Exception e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending post to Kafka");
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.log.error("Error sending post");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknow error sending post to Kafka");
		}
	}
	

}
