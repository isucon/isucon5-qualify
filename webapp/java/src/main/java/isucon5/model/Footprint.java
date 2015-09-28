package isucon5.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Footprint implements Serializable {
	private Integer id;
	private Integer userId;
	private Integer ownerId;
	private LocalDateTime createdAt;
	private LocalDate date;
	private LocalDate updated;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalDate getUpdated() {
		return updated;
	}

	public void setUpdated(LocalDate updated) {
		this.updated = updated;
	}
}
