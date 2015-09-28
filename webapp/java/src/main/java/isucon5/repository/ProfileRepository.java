package isucon5.repository;

import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import isucon5.model.Profile;

@Repository
public class ProfileRepository {
	@Autowired
	NamedParameterJdbcTemplate jdbcTemplate;

	RowMapper<Profile> rowMapper = (rs, i) -> {
		Profile profile = new Profile();
		profile.setUserId(rs.getInt("user_id"));
		profile.setFirstName(rs.getString("first_name"));
		profile.setLastName(rs.getString("last_name"));
		profile.setSex(rs.getString("sex"));
		profile.setBirthDay(rs.getDate("birthday").toLocalDate());
		profile.setPref(rs.getString("pref"));
		profile.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
		return profile;
	};

	public Profile findByUserId(Integer userId) {
		SqlParameterSource source = new MapSqlParameterSource().addValue("user_id",
				userId);
		try {
			return jdbcTemplate.queryForObject(
					"SELECT * FROM profiles WHERE user_id = :user_id", source, rowMapper);
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Transactional
	public Profile create(Profile profile) {
		SqlParameterSource source = new MapSqlParameterSource()
				.addValue("user_id", profile.getUserId())
				.addValue("first_name", profile.getFirstName())
				.addValue("last_name", profile.getLastName())
				.addValue("sex", profile.getSex())
				.addValue("birthday", Date.valueOf(profile.getBirthDay()))
				.addValue("pref", profile.getPref());
		jdbcTemplate.update(
				"INSERT INTO profiles (user_id, first_name, last_name, sex, birthday, pref)"
						+ " VALUES (:user_id, :first_name, :last_name, :sex, :birthday, :pref)",
				source);
		return profile;
	}

	@Transactional
	public Profile update(Profile profile) {
		SqlParameterSource source = new MapSqlParameterSource()
				.addValue("user_id", profile.getUserId())
				.addValue("first_name", profile.getFirstName())
				.addValue("last_name", profile.getLastName())
				.addValue("sex", profile.getSex())
				.addValue("birthday", Date.valueOf(profile.getBirthDay()))
				.addValue("pref", profile.getPref());
		jdbcTemplate.update("UPDATE profiles"
				+ " SET first_name=:first_name, last_name=:last_name, sex=:sex, birthday=:birthday, pref=:pref, updated_at=CURRENT_TIMESTAMP()"
				+ " WHERE user_id = :user_id", source);
		return profile;
	}
}
