package com.MedievalMedia.Services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.MedievalMedia.Entities.Payment;
import com.MedievalMedia.Enums.Status;
import com.MedievalMedia.Repositories.PaymentRepository;

@Service
public class PaymentService {
	private PaymentRepository paymentRepository;
	
	public PaymentService(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}

	/**
	 *  Create an internatl payment
	 * 
	 * @param userId The user id of the user who is buying in Medieval Media
	 * @param value The total value of the transaction
	 * @return String It returns the payment id
	 */
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
	
	/**
	 *  Create an internal payment using paypal id
	 * 
	 * @param userId The user id of the user who is buying in Medieval Media
	 * @param value The total value of the transaction
	 * @param paymentId The payment id created by Paypal 
	 */
	public void createInternalPayment(String userId, BigDecimal value, String paymentId) {
		Payment payment = new Payment();
		payment.setId(paymentId);
		payment.setDate(LocalDateTime.now().toString());
		payment.setId_payer("MEDIEVAL_MEDIA");
		payment.setId_final_user(userId);
		payment.setStatus(Status.PENDANT);
		payment.setTotal(value);
		
		payment = this.paymentRepository.save(payment);
	}

	/**
	 * Update payment status
	 * 
	 * @param paymentId The id of the payment that must be updated
	 */
	public void updateWithdraw(String paymentId) {
		Payment payment = this.paymentRepository.findById(paymentId).get();
		
		payment.setStatus(Status.WITHDRAW_COMPLETED);
		this.paymentRepository.save(payment);
		
	}

	/**
	 *  Update payment status to failed
	 *  
	 * @param paymentId The id of the payment that must be updated
	 */
	public void failedWithdraw(String paymentId) {
		Payment payment = this.paymentRepository.findById(paymentId).get();
		
		payment.setStatus(Status.FAILED);
		this.paymentRepository.save(payment);
	}

	/**
	 *  Create payment with invalid balance in Paypal as status
	 *  
	 * @param userPaypalId The user paypal id (email)
	 * @param value The total value of the request
	 */
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

	/**
	 * Mark a payment as completed
	 * 
	 * @param paymentId The payment id
	 */
	public void updatePaymentStatus(String paymentId) {
		Optional<Payment> searchPayment = this.paymentRepository.findById(paymentId);
		
		if (searchPayment.isPresent()) {
			Payment payment = searchPayment.get();
			
			payment.setStatus(Status.COMPLETED);
			
			this.paymentRepository.save(payment);
		}
	}

	/**
	 *  Cancel payment
	 * 
	 * @param paymentId The id of the payment
	 * @throws ResponseStatusException 
	 * 	FORBIDDEN: if user doesn't match the payer
	 * 	NOT_FOUND: if the payment was not found
	 * 
	 */
	public void cancelPayment(String paymentId, UUID userId) {
		Optional<Payment> searchPayment = this.paymentRepository.findById(paymentId);
		
		if (searchPayment.isPresent()) {
			Payment payment = searchPayment.get();
			
			if (userId.toString().equals(payment.getId_payer())) {
				payment.setStatus(Status.CANCELED);
				
				this.paymentRepository.save(payment);
			} else {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not the payer");
			}
			
		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment was not found");
		}
	}
}
