package isucon5.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;

/**
 * See <a href=
 * "https://blog.apnic.net/2015/08/05/using-the-java-8-stream-api-with-springs-jdbctemplate/">
 * this article</a>.
 */
class RowMapperSupport {

	public static <T> Stream<T> stream(RowMapper<T> rowMapper, ResultSet rs) {
		ResultSetWrappingSqlRowSet rowSet = new ResultSetWrappingSqlRowSet(rs);
		Spliterator<T> spliterator = Spliterators
				.spliteratorUnknownSize(new Iterator<T>() {
					@Override
					public boolean hasNext() {
						return !rowSet.isLast();
					}

					@Override
					public T next() {
						if (!rowSet.next()) {
							throw new NoSuchElementException();
						}
						try {
							return rowMapper.mapRow(rowSet.getResultSet(), 0);
						}
						catch (SQLException e) {
							throw new InvalidResultSetAccessException(e);
						}
					}
				}, Spliterator.IMMUTABLE);
		return StreamSupport.stream(spliterator, false);
	}
}
