package com.MedievalMedia.Entities;

import java.util.HashMap;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;


@Entity
@Data
@Table(name="interactions")
public class Interactions {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	private float heart = 0f;
    private float sword = 0f;
    private float shield = 0f;
    private float crown = 0f;
    private float coin = 0f;
    private float fav = 0f;
    private float answer = 0f;
	
    @Transient
	private HashMap<String, Float> reactions = new HashMap<>();
    @Transient
    private HashMap<String, Float> points = new HashMap<>();
	
	private float score = 0;
	
	public Interactions() {
		this.initializeReactions();
		this.initializePoints();
	}
	
	private void initializeReactions() {
		this.reactions.put("heart", this.heart);
		this.reactions.put("sword", this.sword);
		this.reactions.put("shield", this.shield);
		this.reactions.put("crown", this.crown);
		this.reactions.put("coin", this.coin);
		this.reactions.put("fav", this.fav);
		this.reactions.put("answer", this.answer);
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
		
