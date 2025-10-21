package com.MedievalMedia.Entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.MedievalMedia.Enums.Status;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name="Payments")
public class Payment {
	@Id
	private String id;
	private String id_payer;
	private String id_final_user;
	private BigDecimal total;
	private Status status;
	private String date;
}
