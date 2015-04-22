package de.chrgroth.jsonstore.json.model;

import java.time.LocalDateTime;
import java.util.Date;

public class DateTestEntity {
	
	private Date date;
	private LocalDateTime dateTime;
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public LocalDateTime getDateTime() {
		return dateTime;
	}
	
	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
}
