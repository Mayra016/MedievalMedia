package com.MedievalMedia.Records;

import org.springframework.web.server.ResponseStatusException;

import com.MedievalMedia.Entities.Post;

public record UpdatedPostResponse(Post post, ResponseStatusException response) {

}
