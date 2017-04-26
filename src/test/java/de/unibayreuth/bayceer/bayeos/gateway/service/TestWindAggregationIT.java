package de.unibayreuth.bayceer.bayeos.gateway.service;

import static io.restassured.RestAssured.given;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import bayeos.frame.FrameParserException;
import bayeos.frame.types.LabeledFrame;
import bayeos.frame.types.NumberType;
import bayeos.frame.types.TimestampFrame;
import io.restassured.http.ContentType;


public class TestWindAggregationIT {

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
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = dateFormatter.parse("2017-01-01 10:00:00");
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(date);

		String[] frameValues = new String[] { "{'windDirection':340,'windSpeed':1}",
				"{'windDirection':355,'windSpeed':0.5}", "{'windDirection':3,'windSpeed':0.3}",
				"{'windDirection':14,'windSpeed':0.8}", "{'windDirection':335,'windSpeed':1.3}",
				"{'windDirection':340,'windSpeed':1}", "{'windDirection':355,'windSpeed':0.5}",
				"{'windDirection':6,'windSpeed':0.3}", "{'windDirection':14,'windSpeed':0.8}",
				"{'windDirection':335,'windSpeed':1.3}", "{'windDirection':23,'windSpeed':0.9}",
				"{'windDirection':44,'windSpeed':1}", "{'windDirection':23,'windSpeed':0.3}",
				"{'windDirection':45,'windSpeed':2.2}", "{'windDirection':0,'windSpeed':0.5}",
				"{'windDirection':234,'windSpeed':0.7}", "{'windDirection':34,'windSpeed':0.9}",
				"{'windDirection':43,'windSpeed':1.3}", "{'windDirection':4,'windSpeed':2}",
				"{'windDirection':34,'windSpeed':1}" };

		
		StringBuffer body = new StringBuffer(URLEncoder.encode("sender=wetterstation","UTF-8"));
		for (int i = 0; i < 20; i++) {
			TimestampFrame tf = new TimestampFrame(gc.getTime(), new LabeledFrame(NumberType.Float32, frameValues[i]));
			gc.add(Calendar.MINUTE, 1);			
			body.append("&bayeosframes[]=");
			body.append(URLEncoder.encode(Base64.encodeBase64String(tf.getBytes()),"UTF-8"));								
		}
			
		given().auth().preemptive().basic(serverUser,serverPassword)
		.contentType(ContentType.URLENC)
		.body(body.toString())
		.post(serverUrl + "/frame/saveFlat")
		.then().statusCode(200);

	}

}
