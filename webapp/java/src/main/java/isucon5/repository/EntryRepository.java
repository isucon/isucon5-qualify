package isucon5.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import isucon5.model.Entry;

@Repository
public class EntryRepository {
	@Autowired
	NamedParameterJdbcTemplate jdbcTemplate;

	RowMapper<Entry> rowMapper = (rs, i) -> {
		Entry entry = new Entry();
		entry.setId(rs.getInt("id"));
		entry.setUserId(rs.getInt("user_id"));
		entry.setPrivate(rs.getInt("private") == 1);
		entry.setBody(rs.getString("body"));
		entry.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		return entry;
	};

	public Entry findOne(Integer id) {
		SqlParameterSource source = new MapSqlParameterSource().addValue("id", id);
		try {
			return jdbcTemplate.queryForObject("SELECT * FROM entries WHERE id = :id",
					source, rowMapper);
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<Entry> findOrderByCreatedAtDesc() {
		return jdbcTemplate.query("SELECT * FROM entries ORDER BY created_at DESC",
				rowMapper);
	}

	public List<Entry> findByUserIdOrderByCreatedAtDesc(Integer userId, int limit) {
		SqlParameterSource source = new MapSqlParameterSource()
				.addValue("user_id", userId).addValue("limit", limit);
		return jdbcTemplate.query(
				"SELECT * FROM entries WHERE user_id = :user_id ORDER BY created_at DESC LIMIT :limit",
				source, rowMapper);
	}

	public List<Entry> findByUserIdAndNotPrivateOrderByCreatedAtDesc(Integer userId,
			int limit) {
		SqlParameterSource source = new MapSqlParameterSource()
				.addValue("user_id", userId).addValue("limit", limit);
		return jdbcTemplate.query(
				"SELECT * FROM entries WHERE user_id = :user_id AND private=0 ORDER BY created_at DESC LIMIT :limit",
				source, rowMapper);
	}

	@Transactional(readOnly = true)
	public List<Entry> findOrderByCreatedAtDesc(
			Function<Stream<Entry>, List<Entry>> streamer) {
		SqlParameterSource source = new MapSqlParameterSource();
		return jdbcTemplate.query(
				"SELECT * FROM entries ORDER BY created_at DESC LIMIT 1000", source,
				rs -> {
					return streamer.apply(RowMapperSupport.stream(rowMapper, rs));
				});
	}

	/**
	 * findOrderByCreatedAtDescの地味実装版
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Entry> findByConditionOrderByCreatedAtDesc(Predicate<Entry> condition,
			int limit) {
		SqlParameterSource source = new MapSqlParameterSource();
		return jdbcTemplate.query(
				"SELECT * FROM entries ORDER BY created_at DESC LIMIT 1000", source,
				rs -> {
					List<Entry> entries = new ArrayList<>(limit);
					while (rs.next()) {
						Entry entry = rowMapper.mapRow(rs, 0);
						if (condition.test(entry)) {
							entries.add(entry);
						}
						if (entries.size() >= limit) {
							break;
						}
					}
					return entries;
				});
	}

	@Transactional
	public Entry create(Entry entry) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		SqlParameterSource source = new MapSqlParameterSource()
				.addValue("user_id", entry.getUserId())
				.addValue("private", entry.isPrivate()).addValue("body", entry.getBody());
		jdbcTemplate.update(
				"INSERT INTO entries (user_id, private, body) VALUES (:user_id, :private, :body)",
				source, keyHolder);
		entry.setId(keyHolder.getKey().intValue());
		return entry;
	}
}
