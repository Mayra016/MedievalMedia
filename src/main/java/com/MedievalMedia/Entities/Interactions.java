package com.MedievalMedia.Entities;

import java.util.HashMap;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Data
@Table(name="interactions")
public class Interactions {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	private HashMap<String, Long> reactions = new HashMap<>();
	
	public Interactions() {
		this.initializeReactions();
	}
	
	private void initializeReactions() {
		this.reactions.put("heart", (long) 0);
		this.reactions.put("sword", (long) 0);
		this.reactions.put("shield", (long) 0);
		this.reactions.put("crown", (long) 0);
	}
}
