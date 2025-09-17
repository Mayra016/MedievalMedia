package com.MedievalMedia.Services;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import com.MedievalMedia.Enums.Language;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailService = new EmailService(mailSender);
        emailService.baseUrl = "http://localhost:8080";
        emailService.from = "info@medievalmedia.myarassoftware.com";
    }

    @Test
    void testSendChangeCredentials_English() throws Exception {
		String[] english = {"Verification email", "Click the button to update your information:", "Change password", "or copy and paste this link into your browser: ", "If you did not request a password change, please ignore this email."};

        emailService.sendChangeCredentials("test@example.com", "12345", Language.ENGLISH);

        System.out.println("GET CONTENT " + emailService.getContent());
        for (byte i = 1; i < english.length; i++) {
            assertTrue(emailService.getContent().contains(english[i]));     	
        }
        
    }
    
    @Test
    void testSendChangeCredentials_German() throws Exception {
		String[] german = {"Bestätigungsmail", "Klicken Sie auf den Button, um Ihre Daten zu aktualisieren: ", "Passwort ändern", "oder kopieren Sie diesen Link in Ihren Browser: ", "Wenn Sie keine Passwortänderung angefordert haben, ignorieren Sie diese E-Mail."};

        emailService.sendChangeCredentials("test@example.com", "12345", Language.DEUTSCH);

        System.out.println("GET CONTENT " + emailService.getContent());
        for (byte i = 1; i < german.length; i++) {
            assertTrue(emailService.getContent().contains(german[i]));     	
        }
        
    }
    
    @Test
    void testSendChangeCredentials_Spanish() throws Exception {
		String[] spanish = {"Correo de verificación", "Haga clic en el botón para actualizar sus datos:", "Cambiar contraseña","o copia y pega este link en tu navegador: ", "Si no has solicitado ningún cambio de contraseña, ignora este email."};

        emailService.sendChangeCredentials("test@example.com", "12345", Language.ESPAÑOL);

        System.out.println("GET CONTENT " + emailService.getContent());
        for (byte i = 1; i < spanish.length; i++) {
            assertTrue(emailService.getContent().contains(spanish[i]));     	
        }
        
    }
    
    @Test
    void testSendChangeCredentials_Portuguese() throws Exception {
		String[] portuguese = {"E-mail de verificação", "Clique no botão para atualizar seus dados:", "Alterar senha", "ou copie e cole este link no seu navegador: ", "Se você não solicitou nenhuma alteração de senha, ignore este e-mail."};

        emailService.sendChangeCredentials("test@example.com", "12345", Language.PORTUGUÊS);

        System.out.println("GET CONTENT " + emailService.getContent());
        for (byte i = 1; i < portuguese.length; i++) {
            assertTrue(emailService.getContent().contains(portuguese[i]));     	
        }
        
    }

}
