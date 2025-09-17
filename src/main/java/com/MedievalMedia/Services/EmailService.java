package com.MedievalMedia.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import com.MedievalMedia.Enums.Language;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {
	@Value("${spring.mail.username}") String from;
	@Value("${app.base-url}") String baseUrl;
	
	private JavaMailSender mailSender;
	private Logger log = LoggerFactory.getLogger(EmailService.class);
	private String content;
	
	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	
	public void sendChangeCredentials(String email, String token, Language language) {
		// translations
		String[] german = {"Bestätigungsmail", "Klicken Sie auf den Button, um Ihre Daten zu aktualisieren: ", "Passwort ändern", "oder kopieren Sie diesen Link in Ihren Browser: ", "Wenn Sie keine Passwortänderung angefordert haben, ignorieren Sie diese E-Mail."};
		String[] english = {"Verification email", "Click the button to update your information:", "Change password", "or copy and paste this link into your browser: ", "If you did not request a password change, please ignore this email."};
		String[] portuguese = {"E-mail de verificação", "Clique no botão para atualizar seus dados:", "Alterar senha", "ou copie e cole este link no seu navegador: ", "Se você não solicitou nenhuma alteração de senha, ignore este e-mail."};
		String[] spanish = {"Correo de verificación", "Haga clic en el botón para actualizar sus datos:", "Cambiar contraseña","o copia y pega este link en tu navegador: ", "Si no has solicitado ningún cambio de contraseña, ignora este email."};
		
		// generate link to password change
		String actionUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
				.path("change-password")
				.queryParam("token", token)
				.toUriString();
		
		// email template
		String subject = "";
		this.content = """
				
				<div style="font-family:'Ubuntu';margin:30px;">
					<div style="text-align:center;font-size:18px;font-weight:bold;">
					  <p>%s</p>
					  <a href="%s" style="background-color:#365b6d;color:#f2f1ec;border-radius:15px;padding:10px;border:none;cursor:pointer;font-weight:bold;text-decoration: none;">%s</a>
					  <p style="font-weight:normal;">%s</p>
					  <p>%s</p>
					</div>
					<div style="text-align:left;margin-top:30px;font-size:12px;">
					  <p>%s</p>
					</div>
				</div>
						""";
		
		// translate template
		if (Language.DEUTSCH == language) {
			subject = german[0];
			this.content = String.format(content, german[1], actionUrl, german[2], german[3], actionUrl, german[4]);
		}
		if (Language.PORTUGUÊS == language) {
			subject = portuguese[0];
			this.content = String.format(content, portuguese[1], actionUrl, portuguese[2], portuguese[3], actionUrl, portuguese[4]);
		}
		if (Language.ESPAÑOL == language) {
			subject = spanish[0];
			this.content = String.format(content, spanish[1], actionUrl, spanish[2], spanish[3], actionUrl, spanish[4]);
		}
		if (Language.ENGLISH == language) {
			subject = english[0];
			this.content = String.format(content, english[1], actionUrl, english[2], english[3], actionUrl, english[4]);
		}
		
		System.out.println("TEST ************" + content.toString());
		
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			
			helper.setTo(email);
			helper.setSubject(subject);
			helper.setFrom(from);
			helper.setText(content, true);
			mailSender.send(mimeMessage);
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error sending change password email: " + email);
		}
	}
	
	public String getContent() {
		return this.content;
	}

}
