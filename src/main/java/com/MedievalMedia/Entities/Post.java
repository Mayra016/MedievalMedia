package com.MedievalMedia.Entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Table(name="posts")
@NoArgsConstructor
public class Post {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	@ManyToOne
	@JoinColumn(name = "creator_id")
	private User creator;
	private String greetings;
	private String content;
	private String reign;
	private LocalDate date;
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "interactions_id")
	private Interactions interactions;
	
	public Post(User creator, String greetings, String content, String reign) {
		this.date = LocalDate.now();
		this.interactions = new Interactions();
	}
	
}
