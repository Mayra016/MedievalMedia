package com.MedievalMedia.Entities;

import java.util.HashMap;
import java.util.Set;

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
	private HashMap<String, Float> reactions = new HashMap<>();
	private HashMap<String, Float> points = new HashMap<>();
	
	private float score = 0;
	
	public Interactions() {
		this.initializeReactions();
		this.initializePoints();
	}
	
	private void initializeReactions() {
		this.reactions.put("heart", 0f);
		this.reactions.put("sword", 0f);
		this.reactions.put("shield", 0f);
		this.reactions.put("crown", 0f);
		this.reactions.put("coin", 0f);
		this.reactions.put("fav", 0f);
		this.reactions.put("answer", 0f);
	}
	
	private void initializePoints() {
		this.points.put("heart", 0.000002f);
		this.points.put("sword", -0.000002f);
		this.points.put("shield", 0.000002f);
		this.points.put("crown", 0.000004f);
		this.points.put("coin", 0.000003f);
		this.points.put("fav", 0.000001f);
		this.points.put("answer", 0.000003f);
	}

	public void updateReactions(String interaction, boolean up) {
		if (up) {
			this.reactions.replace(interaction, this.reactions.get(interaction) + 1);
			this.score += this.points.get(interaction);
		} else {
			this.reactions.replace(interaction, this.reactions.get(interaction) - 1);
			this.score -= this.points.get(interaction);			
		}
	}
}
		
