package com.MedievalMedia.Entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.MedievalMedia.Enums.Language;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name="users")
@NoArgsConstructor
public class User {
	@Id
    @GeneratedValue
    private UUID id;
	private String email;
	private String username;
	private String password;
	private List<Language> languages;
	private Language appLanguage;
	private LocalDate birthday;
	private LocalDate enterDate;
	private String country;
	private String reign;
	private String[] titles = new String[1];
	private BigDecimal money;
	@ManyToMany
	@JoinTable(
	    name = "user_follows",
	    joinColumns = @JoinColumn(name = "follower_id"),
	    inverseJoinColumns = @JoinColumn(name = "followed_id")
	)
	private List<User> follow = new ArrayList<>();
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name ="user_favposts",
		joinColumns = @JoinColumn(name = "user_id"),
		inverseJoinColumns = @JoinColumn(name = "post_id")
	)
	private Set<Post> favoritePosts = new HashSet<>();
	
	public User(String email, String password) {
		this.email = email;
		this.password = password;
	}
	
	public User(String email, String username, String password, List<Language> languages, Language appLanguage, LocalDate birthday, String country, String title) {
		this.email = email;
		this.username = username;
		this.password = password;
		this.languages = languages;
		this.appLanguage = appLanguage;
		this.birthday = birthday;
		this.enterDate = LocalDate.now();
		this.country = country;
		titles[0] = title;
	}

	public void addToFavorites(Post post) {
		this.favoritePosts.add(post);
		
	}

}
