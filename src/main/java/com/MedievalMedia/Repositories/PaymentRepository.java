package com.MedievalMedia.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.MedievalMedia.Entities.Payment;
import com.MedievalMedia.Enums.Status;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String>{

	@Query("SELECT p FROM Payment p WHERE p.id IN :ids AND p.status = :status")
	List<Payment> findAllByIdAndStatus(@Param("ids") List<String> ids, @Param("status") Status status);

}
