package isucon5.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import isucon5.model.Comment;

@Repository
public class CommentRepository {
	@Autowired
	NamedParameterJdbcTemplate jdbcTemplate;

	RowMapper<Comment> rowMapper = (rs, i) -> {
		Comment comment = new Comment();
		comment.setId(rs.getInt("id"));
		comment.setEntryId(rs.getInt("entry_id"));
		comment.setUserId(rs.getInt("user_id"));
		comment.setComment(rs.getString("comment"));
		comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		return comment;
	};

	public List<Comment> findByEntryId(Integer entryId) {
		SqlParameterSource source = new MapSqlParameterSource().addValue("entry_id",
				entryId);
		return jdbcTemplate.query("SELECT * FROM comments WHERE entry_id = :entry_id",
				source, rowMapper);
	}

	@Transactional(readOnly = true)
	public List<Comment> findOrderByCreatedAtDesc(
			Function<Stream<Comment>, List<Comment>> streamer) {
		SqlParameterSource source = new MapSqlParameterSource();
		return jdbcTemplate.query(
				"SELECT * FROM comments ORDER BY created_at DESC LIMIT 1000", source,
				rs -> {
					return streamer.apply(RowMapperSupport.stream(rowMapper, rs));
				});
	}

	/**
	 * findOrderByCreatedAtDescの地味実装版
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Comment> findByConditionOrderByCreatedAtDesc(Predicate<Comment> condition,
			int limit) {
		SqlParameterSource source = new MapSqlParameterSource();
		return jdbcTemplate.query(
				"SELECT * FROM comments ORDER BY created_at DESC LIMIT 1000", source,
				rs -> {
					List<Comment> comments = new ArrayList<>(limit);
					while (rs.next()) {
						Comment comment = rowMapper.mapRow(rs, 0);
						if (condition.test(comment)) {
							comments.add(comment);
						}
						if (comments.size() >= limit) {
							break;
						}
					}
					return comments;
				});
	}

	public long countByEntryId(Integer entryId) {
		SqlParameterSource source = new MapSqlParameterSource().addValue("entry_id",
				entryId);
		return jdbcTemplate.queryForObject(
				"SELECT COUNT(*) AS c FROM comments WHERE entry_id = :entry_id", source,
				Long.class);
	}

	public List<Comment> findByUserId(Integer userId) {
		SqlParameterSource source = new MapSqlParameterSource().addValue("user_id",
				userId);
		return jdbcTemplate.query(
				"SELECT c.id AS id, c.entry_id AS entry_id, c.user_id AS user_id, c.comment AS comment, c.created_at AS created_at"
						+ " FROM comments c JOIN entries e ON c.entry_id = e.id"
						+ " WHERE e.user_id = :user_id ORDER BY c.created_at DESC LIMIT 10",
				source, rowMapper);
	}

	@Transactional
	public Comment create(Comment comment) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		SqlParameterSource source = new MapSqlParameterSource()
				.addValue("entry_id", comment.getEntryId())
				.addValue("user_id", comment.getUserId())
				.addValue("comment", comment.getComment());
		jdbcTemplate.update(
				"INSERT INTO comments (entry_id, user_id, comment) VALUES (:entry_id, :user_id, :comment)",
				source, keyHolder);
		comment.setId(keyHolder.getKey().intValue());
		return comment;
	}
}
