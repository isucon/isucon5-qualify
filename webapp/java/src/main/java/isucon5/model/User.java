package isucon5.model;

import java.io.Serializable;

public class User implements Serializable {
	private Integer id;
	private String accountName;
	private String nickName;
	private String email;
	private String password;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", accountName='" + accountName + '\'' +
				", nickName='" + nickName + '\'' +
				", email='" + email + '\'' +
				'}';
	}
}
