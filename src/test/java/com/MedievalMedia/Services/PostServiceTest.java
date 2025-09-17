package com.MedievalMedia.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.MedievalMedia.Entities.Post;
import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Enums.Language;
import com.MedievalMedia.Repositories.PostRepository;
import com.MedievalMedia.Repositories.UserRepository;

@DataJpaTest
public class PostServiceTest {
	private PostRepository postRepository;
	private UserRepository userRepository;

	@Autowired
	public PostServiceTest (PostRepository postRepository, UserRepository userRepository) {
		this.postRepository = postRepository;
		this.userRepository = userRepository;
	}
    
    

    @BeforeEach
    void initialize() {
    	User user = this.userRepository.save(new User());
        for (int i = 0; i < 61; i++) {
            postRepository.save(new Post(user, "Greeting " + i, "Test content", "Spain", Language.ESPAÑOL));
        }
    }

    @Test
    void testFindLastFifthyByReign() {
        List<Post> posts = postRepository.findLastFifthyByReign("Spain", PageRequest.of(0, 50));
        assertEquals(50, posts.size()); // verify if the method retrieve just the last 50      
        assertEquals("Greeting 59", posts.get(1).getGreetings()); // check if posts are order form latest to oldest
    }
}

