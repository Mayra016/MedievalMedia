package com.MedievalMedia.Entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import com.MedievalMedia.Enums.Language;
import com.MedievalMedia.Repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class InteractionsTest {
	User user;
	Post post;
	
	@BeforeEach
	public void init() {
		this.post = new Post(user, "Test Post", "Content", "Deutschland", Language.DEUTSCH);
		
	}

	@Test
	public void updateReactionsTestPlus() {
		this.post.updateInteractions("heart", true);
		System.out.println(this.post.getInteractions().getScore());
		assertEquals(0.000002f, this.post.getInteractions().getScore());
	}
	
	@Test
	public void updateReactionsTestMinus() {
		this.post.updateInteractions("heart", false);
		System.out.println(this.post.getInteractions().getScore());
		assertEquals(-0.000002f, this.post.getInteractions().getScore());
	}
}
