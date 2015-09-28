package isucon5.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Relation implements Serializable {
	private Integer id;
	private Integer one;
	private Integer another;
	private LocalDateTime createdAt;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getOne() {
		return one;
	}

	public void setOne(Integer one) {
		this.one = one;
	}

	public Integer getAnother() {
		return another;
	}

	public void setAnother(Integer another) {
		this.another = another;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "Relation{" + "id=" + id + ", one=" + one + ", another=" + another
				+ ", createdAt=" + createdAt + '}';
	}
}
