package com.github.christiangroth.jsonstore.model;

import java.time.LocalDateTime;
import java.util.Date;

public class TestEntity {
	
	private int id;
	private String data;
	
	private Date date;
	private LocalDateTime dateTime;
	
	public TestEntity() {
		this(0, null);
	}
	
	public TestEntity(int id, String data) {
		this.id = id;
		this.data = data;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestEntity other = (TestEntity) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "TestEntity [id=" + id + ", data=" + data + ", date=" + date + ", localDateTime=" + dateTime + "]";
	}
}
