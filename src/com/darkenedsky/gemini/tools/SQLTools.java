package com.darkenedsky.gemini.tools;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public abstract class SQLTools {

	public static void addParam(PreparedStatement ps, int index, Boolean value) throws SQLException {

		if (value == null)
			ps.setNull(index, Types.BOOLEAN);
		else
			ps.setBoolean(index, value);
	}

	public static void addParam(PreparedStatement ps, int index, Double value) throws SQLException {

		if (value == null)
			ps.setNull(index, Types.DOUBLE);
		else
			ps.setDouble(index, value);
	}

	public static void addParam(PreparedStatement ps, int index, Float value) throws SQLException {

		if (value == null)
			ps.setNull(index, Types.FLOAT);
		else
			ps.setFloat(index, value);
	}

	public static void addParam(PreparedStatement ps, int index, Integer value) throws SQLException {

		if (value == null)
			ps.setNull(index, Types.INTEGER);
		else
			ps.setInt(index, value);
	}

	public static void addParam(PreparedStatement ps, int index, Long value) throws SQLException {

		if (value == null)
			ps.setNull(index, Types.BIGINT);
		else
			ps.setLong(index, value);
	}

	public static void addParam(PreparedStatement ps, int index, Short value) throws SQLException {

		if (value == null)
			ps.setNull(index, Types.SMALLINT);
		else
			ps.setShort(index, value);
	}

	public static void addParam(PreparedStatement ps, int index, String value) throws SQLException {

		if (value == null)
			ps.setNull(index, Types.VARCHAR);
		else
			ps.setString(index, value);
	}

	public static Boolean getBoolean(ResultSet set, String field) throws SQLException {
		Boolean i = set.getBoolean(field);
		if (set.wasNull())
			i = null;
		return i;
	}

	public static Double getDouble(ResultSet set, String field) throws SQLException {
		Double i = set.getDouble(field);
		if (set.wasNull())
			i = null;
		return i;
	}

	public static Float getFloat(ResultSet set, String field) throws SQLException {
		Float i = set.getFloat(field);
		if (set.wasNull())
			i = null;
		return i;
	}

	public static Integer getInteger(ResultSet set, String field) throws SQLException {
		Integer i = set.getInt(field);
		if (set.wasNull())
			i = null;
		return i;
	}

	public static Long getLong(ResultSet set, String field) throws SQLException {
		Long i = set.getLong(field);
		if (set.wasNull())
			i = null;
		return i;
	}

	public static Short getShort(ResultSet set, String field) throws SQLException {
		Short i = set.getShort(field);
		if (set.wasNull())
			i = null;
		return i;
	}

	public static String getString(ResultSet set, String field) throws SQLException {
		String s = set.getString(field);
		if (set.wasNull())
			s = null;
		return s;
	}

}
