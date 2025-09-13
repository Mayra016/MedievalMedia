package com.MedievalMedia.Entities;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name="users")
@NoArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	private long id;
	private String email;
	private String username;
	private String password;
	private LocalDate birthday;
	private LocalDate enterDate;
	private String country;
	private String[] titles = new String[1];
	
	public User(String email, String username, String password, LocalDate birthday, String country, String title) {
		this.enterDate = LocalDate.now();
		titles[0] = title;
	}

}
