package isucon5.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import isucon5.model.Relation;

@Repository
public class RelationRepository {
	@Autowired
	NamedParameterJdbcTemplate jdbcTemplate;

	RowMapper<Relation> rowMapper = (rs, i) -> {
		Relation relation = new Relation();
		relation.setId(rs.getInt("id"));
		relation.setOne(rs.getInt("one"));
		relation.setAnother(rs.getInt("another"));
		relation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		return relation;
	};

	public List<Relation> findByUserIdOrderByCreatedAtDesc(Integer userId) {
		SqlParameterSource source = new MapSqlParameterSource().addValue("user_id",
				userId);
		return jdbcTemplate.query(
				"SELECT * FROM relations WHERE one = :user_id OR another = :user_id ORDER BY created_at DESC",
				source, rowMapper);
	}

	public long countByOneAndAnother(Integer one, Integer another) {
		SqlParameterSource source = new MapSqlParameterSource().addValue("one", one)
				.addValue("another", another);
		return jdbcTemplate.queryForObject(
				"SELECT COUNT(1) AS cnt FROM relations WHERE (one = :one AND another = :another) OR (one = :one AND another = :another)",
				source, Long.class);
	}

	@Transactional
	public void create(Relation relation) {
		SqlParameterSource source = new MapSqlParameterSource()
				.addValue("one", relation.getOne())
				.addValue("another", relation.getAnother());
		jdbcTemplate.update(
				"INSERT INTO relations (one, another) VALUES (:one, :another), (:another, :one)",
				source);
	}
}
