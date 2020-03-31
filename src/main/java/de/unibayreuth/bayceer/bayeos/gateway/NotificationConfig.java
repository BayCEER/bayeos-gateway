package de.unibayreuth.bayceer.bayeos.gateway;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationConfig {
	
	@Value("${NOTIFICATION:false}")
	private Boolean notification;
	
	@Value("${NOTIFICATION_HOST:}")
	private String notification_host;
	
	@Value("${NOTIFICATION_WAIT_SECS:60}")
	private int notification_wait_secs = 60;
	
	@Value("${NOTIFICATION_MAX_SOFT_STATES:4}")
	private int notification_max_soft_states = 4;
	
	@Value("${NOTIFICATION_SENDER:}")
	private String notification_sender;

	
	public String getNotification_host()  {
		if (notification_host == null || notification_host.isEmpty()) {			
			try {
				return InetAddress.getLocalHost().getCanonicalHostName();
			} catch (UnknownHostException e) {
				return "localhost";
			}
		} else {
			return notification_host; 
		}
	}
	public void setNotification_host(String notification_host) {
		this.notification_host = notification_host;
	}
	public int getNotification_wait_secs() {
		return notification_wait_secs;
	}
	public void setNotification_wait_secs(int notification_wait_secs) {
		this.notification_wait_secs = notification_wait_secs;
	}
	public int getNotification_max_soft_states() {
		return notification_max_soft_states;
	}
	public void setNotification_max_soft_states(int notification_max_soft_states) {
		this.notification_max_soft_states = notification_max_soft_states;
	}
	public String getNotification_sender() {
		if (notification_sender == null || notification_sender.isEmpty()) {
			return "admin@" + getNotification_host();
		} else {
			return notification_sender;	
		}
	}
	public void setNotification_sender(String notification_sender) {
		this.notification_sender = notification_sender;
	}
	public Boolean getNotification() {
		return notification;
	}
	public void setNotification(Boolean notification) {
		this.notification = notification;
	}
	
	
}
