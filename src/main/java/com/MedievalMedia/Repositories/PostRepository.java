package com.MedievalMedia.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.MedievalMedia.Entities.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>{

}
