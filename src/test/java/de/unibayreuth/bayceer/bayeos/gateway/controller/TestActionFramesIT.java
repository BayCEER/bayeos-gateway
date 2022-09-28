package de.unibayreuth.bayceer.bayeos.gateway.controller;

import static io.restassured.RestAssured.given;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import bayeos.frame.FrameParserException;
import bayeos.frame.types.Command;
import io.restassured.http.ContentType;


public class TestActionFramesIT {

	String serverUrl;
	String serverUser;
	String serverPassword;
	
	@Before	
	public void setUp(){
		this.serverUrl = System.getProperty("server.url");
		this.serverUser = System.getProperty("server.user");
		this.serverPassword = System.getProperty("server.password");
	}

	
	@Test
	public void test() throws ParseException, FrameParserException, UnsupportedEncodingException {
		
		
		//TODO Send a new ActionFrame 
				
//		StringBuffer body = new StringBuffer(URLEncoder.encode("sender=commandTest","UTF-8"));
//		Command cmd = new Command((byte) 3, "Switch ON");				
//		body.append("&bayeosframes[]=");
//		body.append(URLEncoder.encode(Base64.encodeBase64String(cmd.getBytes()),"UTF-8"));
//								
//		given().auth().preemptive().basic(serverUser,serverPassword)
//		.contentType(ContentType.URLENC)
//		.body(body.toString())
//		.post(serverUrl + "/frame/saveFlat")
//		.then().statusCode(200);
		
		//TODO Check that command state is now pending 
				
		//TODO Send a new data frame
		
		//TODO Check that body contains pending command
			
		//TODO Execute command and send command response 
		
		//TODO Check that command state is now executed 

	}

}
