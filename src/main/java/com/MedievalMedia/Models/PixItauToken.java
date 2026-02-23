package com.MedievalMedia.Models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.LocalDateTime;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.Data;

@Data
public class PixItauToken {
	private String token = "";
	private LocalDateTime createdAt = LocalDateTime.now();
	
	public PixItauToken() {};
	
	public boolean isValid() {
		return this.createdAt.plusMinutes(4).isAfter(LocalDateTime.now());
	}
	
	public void authenticate() {
		    HttpURLConnection connection = null;
		    try {
		      URL url = new URL("https://sts.itau.com.br/api/oauth/token");
		      connection = (HttpURLConnection) url.openConnection();
		      connection.addRequestProperty(
		          "Content-Type", "application/x-www-form-urlencoded");
		      connection.setRequestMethod("POST");
		      connection.setDoOutput(true);

		      // Add certificate
		      File p12 = new File("{CAMINHO_CERTIFICADO}\\certificado.pfx");
		      String p12password = "{SENHA_CERTIFICADO}";
		      FileInputStream keyInput = new FileInputStream(p12);
		      KeyStore keyStore = KeyStore.getInstance("PKCS12");
		      keyStore.load(keyInput, p12password.toCharArray());
		      keyInput.close();
		      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
		      keyManagerFactory.init(keyStore, p12password.toCharArray());
		      SSLContext context = SSLContext.getInstance("TLS");
		      context.init(
		          keyManagerFactory.getKeyManagers(), null, new SecureRandom());
		      SSLSocketFactory socketFactory = context.getSocketFactory();
		      if (connection instanceof HttpsURLConnection)
		        ((HttpsURLConnection) connection).setSSLSocketFactory(socketFactory);
		      //

		      String body = "grant_type=client_credentials"
		          + "&client_id={CLIENT_ID}"
		          + "&client_secret={CLIENTE_SECRET}";
		      OutputStream outputStream = connection.getOutputStream();
		      outputStream.write(body.toString().getBytes());
		      outputStream.close();
		      BufferedReader bufferedReader = new BufferedReader(
		          new InputStreamReader(connection.getInputStream()));
		      StringBuilder response = new StringBuilder();
		      String line = null;
		      while ((line = bufferedReader.readLine()) != null) {
		        response.append(line);
		      }
		      bufferedReader.close();
		      System.out.println(response.toString());
		      JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
		      this.token = json.get("access_token").getAsString();
		    } catch (Exception e) {
		      e.printStackTrace();
		    } finally {
		      if (connection != null) {
		    	this.createdAt = LocalDateTime.now();
		        connection.disconnect();
		      }
		    }
		  }
	}
