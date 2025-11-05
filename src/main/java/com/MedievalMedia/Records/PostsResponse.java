package com.MedievalMedia.Records;

import org.springframework.web.server.ResponseStatusException;
import com.MedievalMedia.Entities.Post;

import java.util.List;

public record PostsResponse(List<Post> posts, ResponseStatusException exception) {

}
