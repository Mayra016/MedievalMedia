package com.MedievalMedia.Records;

import com.MedievalMedia.Entities.Post;

public record UpdatePostDAO(Post post, String reaction, boolean up) {

}
