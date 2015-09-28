package isucon5.repository;

import java.util.List;

import isucon5.model.Footprint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class FootprintRepository {
	@Autowired
	NamedParameterJdbcTemplate jdbcTemplate;

	RowMapper<Footprint> rowMapper = (rs, i) -> {
		Footprint footprint = new Footprint();
		footprint.setUserId(rs.getInt("user_id"));
		footprint.setOwnerId(rs.getInt("owner_id"));
		footprint.setDate(rs.getDate("date").toLocalDate());
		footprint.setUpdated(rs.getDate("updated").toLocalDate());
		return footprint;
	};

	public List<Footprint> findByUserId(Integer userId) {
		SqlParameterSource source = new MapSqlParameterSource()
				.addValue("user_id", userId).addValue("limit", 50);
		return jdbcTemplate
				.query("SELECT user_id, owner_id, DATE(created_at) AS date, MAX(created_at) AS updated"
						+ " FROM footprints WHERE user_id = :user_id"
						+ " GROUP BY user_id, owner_id, DATE(created_at)"
						+ " ORDER BY updated DESC LIMIT :limit", source, rowMapper);
	}

	@Transactional
	public Footprint create(Footprint footprint) {
		SqlParameterSource source = new MapSqlParameterSource()
				.addValue("user_id", footprint.getUserId())
				.addValue("owner_id", footprint.getOwnerId());
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(
				"INSERT INTO footprints (user_id,owner_id) VALUES (:user_id,:owner_id)",
				source, keyHolder);
		footprint.setId(keyHolder.getKey().intValue());
		return footprint;
	}

}
