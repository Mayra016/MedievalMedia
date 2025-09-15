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

import com.MedievalMedia.Enums.Language;

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
	private Language language;
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "interactions_id")
	private Interactions interactions;
	@ManyToOne
    @JoinColumn(name = "parent_id")
    private Post parent;
	
	public Post(User creator, String greetings, String content, String reign, Language language) {
		this.creator = creator;
		this.greetings = greetings;
		this.content = content;
		this.reign = reign;
		this.date = LocalDate.now();
		this.interactions = new Interactions();
		this.language = language;
	}
	
}
