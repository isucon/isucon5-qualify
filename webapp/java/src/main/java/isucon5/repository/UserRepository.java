package isucon5.repository;

import isucon5.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
	@Autowired
	NamedParameterJdbcTemplate jdbcTemplate;

	RowMapper<User> rowMapper = (rs, i) -> {
		User user = new User();
		user.setId(rs.getInt("id"));
		user.setAccountName(rs.getString("account_name"));
		user.setNickName(rs.getString("nick_name"));
		user.setEmail(rs.getString("email"));
		return user;
	};

	public User findOne(Integer id) {
		SqlParameterSource source = new MapSqlParameterSource().addValue("id", id);
		try {
			return jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = :id",
					source, rowMapper);
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public User findByEmailAndRawPassword(String email, String rawPassword) {
		SqlParameterSource source = new MapSqlParameterSource().addValue("email", email)
				.addValue("password", rawPassword);
		try {
			return jdbcTemplate.queryForObject(
					"SELECT u.id AS id, u.account_name AS account_name, u.nick_name AS nick_name, u.email AS email"
							+ " FROM users u JOIN salts s ON u.id = s.user_id"
							+ " WHERE u.email = :email AND u.passhash = SHA2(CONCAT(:password, s.salt), 512)",
					source, rowMapper);
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public User findByAccountName(String accountName) {
		SqlParameterSource source = new MapSqlParameterSource().addValue("account_name",
				accountName);
		try {
			return jdbcTemplate.queryForObject(
					"SELECT * FROM users WHERE account_name = :account_name", source,
					rowMapper);
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
}
