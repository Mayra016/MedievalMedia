package Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MedievalMedia.Entities.Post;

@RestController
@RequestMapping("/api/v1")
public class KafkaRestAPI {

	@PostMapping("insert-data")
	public ResponseEntity insertData(@RequestBody Post post) {
		try {
			
			return ResponseEntity.status(HttpStatus.OK).body("Data was successful inserted");
		} catch (Exception e) {
			
		}
	}
}
