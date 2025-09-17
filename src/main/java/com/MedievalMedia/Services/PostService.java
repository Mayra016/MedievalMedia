package com.MedievalMedia.Services;

import java.util.List;
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
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.MedievalMedia.Entities.Post;
import com.MedievalMedia.Records.PostDAO;
import com.MedievalMedia.Records.TestDAO;
import com.MedievalMedia.Records.UpdatePostDAO;
import com.MedievalMedia.Repositories.PostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final KafkaProducer<String, String> producer;
    private final Logger log = LoggerFactory.getLogger(PostService.class);

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;

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


	public Post updateInteractions(UpdatePostDAO updateInfo) {
		try {
			Post post = updateInfo.post();
			post.getInteractions().updateReactions(updateInfo.reaction(), updateInfo.up());
			this.postRepository.save(post);
			
			return post;
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error updating interactions");
			
			return new Post();
		}
	}
}
