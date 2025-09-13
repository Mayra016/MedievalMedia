package com.MedievalMedia.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.MedievalMedia.Entities.Interactions;

@Repository
public interface InteractionsRepository extends JpaRepository<Interactions, Long>{

}
