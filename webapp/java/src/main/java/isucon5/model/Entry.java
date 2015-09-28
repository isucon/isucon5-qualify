package isucon5.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class Entry implements Serializable {
	private Integer id;
	private Integer userId;
	private boolean _private;
	private String body;
	private LocalDateTime createdAt;

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

	public boolean isPrivate() {
		return _private;
	}

	public void setPrivate(boolean _private) {
		this._private = _private;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getTitle() {
		return body.split(Pattern.quote("\n"), 2)[0];
	}

	public String getContent() {
		return body.split(Pattern.quote("\n"), 2)[1];
	}

	@Override
	public String toString() {
		return "Entry{" + "id=" + id + ", userId=" + userId + ", _private=" + _private
				+ ", body='" + body + '\'' + ", createdAt=" + createdAt + '}';
	}
}
