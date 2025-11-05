package com.MedievalMedia.Services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.MedievalMedia.Entities.Payment;
import com.MedievalMedia.Enums.Status;
import com.MedievalMedia.Repositories.PaymentRepository;

@Service
public class PaymentService {
	private PaymentRepository paymentRepository;
	
	public PaymentService(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}

	public String createInternalPayment(String userId, BigDecimal value) {
		Payment payment = new Payment();
		payment.setId(UUID.randomUUID().toString());
		payment.setDate(LocalDateTime.now().toString());
		payment.setId_payer("MEDIEVAL_MEDIA");
		payment.setId_final_user(userId);
		payment.setStatus(Status.WITHDRAW_REQUESTED);
		payment.setTotal(value);
		
		payment = this.paymentRepository.save(payment);
		
		return payment.getId();
	}

	public void updateWithdraw(String paymentId) {
		Payment payment = this.paymentRepository.findById(paymentId).get();
		
		payment.setStatus(Status.WITHDRAW_COMPLETED);
		this.paymentRepository.save(payment);
		
	}

	public void failedWithdraw(String paymentId) {
		Payment payment = this.paymentRepository.findById(paymentId).get();
		
		payment.setStatus(Status.FAILED);
		this.paymentRepository.save(payment);
	}

	public void createPendingPaypalWithdrawPayment(String userPaypalId, BigDecimal value) {
		Payment payment = new Payment();
		payment.setId(UUID.randomUUID().toString());
		payment.setDate(LocalDateTime.now().toString());
		payment.setId_payer("MEDIEVAL_MEDIA");
		payment.setId_final_user(userPaypalId);
		payment.setStatus(Status.INVALID_BALANCE_PAYPAL);
		payment.setTotal(value);
		
		payment = this.paymentRepository.save(payment);
		
	}
}
