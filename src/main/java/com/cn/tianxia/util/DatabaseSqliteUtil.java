package com.cn.tianxia.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.stereotype.Service;

/**
 * 连接和使用数据库资源的工具类
 *
 * @author yifangyou
 * @version search 2012-03-12
 */
public class DatabaseSqliteUtil {
	/**
	 * 数据源
	 */
	private BasicDataSource dataSource;
	/**
	 * 数据库连接
	 */
	public Connection conn;

	/**
	 * 获取数据源
	 * 
	 * @return 数据源
	 */
	public BasicDataSource getDataSource() {
		return dataSource;
	}

	/**
	 * 设置数据源
	 * 
	 * @param dataSource
	 *            数据源
	 */
	public void setDataSource(BasicDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 获取数据库连接
	 * 
	 * @return conn
	 */
	public Connection getConnection() {
		try {
			conn = dataSource.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return conn;
	}

	/**
	 * 关闭数据库连接
	 * 
	 * @param conn
	 */
	public static void closeConnection(Connection conn) {
		if (null != conn) {
			try {
				conn.close();
				conn = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取执行SQL的工具
	 * 
	 * @param conn
	 *            数据库连接
	 * @return stmt
	 */
	public static int getFoundRows(Connection conn) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = getStatement(conn);
			rs = stmt.executeQuery("SELECT FOUND_ROWS()");
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeStatement(stmt);
			closeResultSet(rs);
		}
		return 0;
	}

	/**
	 * 获取执行SQL的工具
	 * 
	 * @param conn
	 *            数据库连接
	 * @return stmt
	 */
	public static Statement getStatement(Connection conn) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return stmt;
	}

	/**
	 * 获取执行SQL的工具
	 * 
	 * @param conn
	 *            数据库连接
	 * @param sql
	 *            SQL语句
	 * @return prepStmt
	 */
	public static PreparedStatement getPrepStatement(Connection conn, String sql) {
		PreparedStatement prepStmt = null;
		try {
			prepStmt = conn.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return prepStmt;
	}

	/**
	 * 关闭数据库资源
	 * 
	 * @param stmt
	 */
	public static void closeStatement(Statement stmt) {
		if (null != stmt) {
			try {
				stmt.close();
				stmt = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 关闭数据库资源
	 * 
	 * @param prepStmt
	 */
	public static void closePrepStatement(PreparedStatement prepStmt) {
		if (null != prepStmt) {
			try {
				prepStmt.close();
				prepStmt = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取结果集
	 * 
	 * @param stmt
	 *            执行SQL的工具
	 * @param sql
	 *            SQL语句
	 * @return 结果集
	 */
	public static ResultSet getResultSet(Statement stmt, String sql) {
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

	/**
	 * 关闭数据库资源
	 * 
	 * @param rs
	 */
	public static void closeResultSet(ResultSet rs) {
		if (null != rs) {
			try {
				rs.close();
				rs = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static Boolean setAutoCommit(Connection conn, boolean commitStatus) {
		if (conn == null) {
			return true;
		}
		try {
			boolean commit = conn.getAutoCommit();
			conn.setAutoCommit(commitStatus);
			return commit;
		} catch (SQLException e1) {
			e1.printStackTrace();
			return true;
		}
	}

	public static boolean rollback(Connection conn, boolean oldCommitStatus) {
		if (conn == null) {
			return true;
		}
		try {
			conn.rollback(); // 事物回滚
			conn.setAutoCommit(oldCommitStatus);
			return true;
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}
	}

	public static boolean commit(Connection conn, boolean oldCommitStatus) {
		if (conn == null) {
			return true;
		}
		try {
			conn.commit(); // 事物回滚
			conn.setAutoCommit(oldCommitStatus);
			return true;
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}
	}

	public static int getLastId(PreparedStatement ps) {
		ResultSet rs = null;
		try {
			rs = ps.getGeneratedKeys();
			if (rs != null && rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeResultSet(rs);
		}
		return -1;
	}

	/**
	 * 判断是否是管理员
	 * 
	 * @param conn
	 *            mysql连接
	 * @param ip
	 *            请求ip
	 * @param password
	 *            管理员密码
	 * @author yaofuyuan
	 * @since 2011-08-02 12:58:00
	 * @return void 0：不是,1:是,-1:数据库出错
	 */
	public int isSuperAdmin(Connection conn, String ip, String password) {
		if (conn == null) {
			return -1;
		}
		PreparedStatement ps = getPrepStatement(conn,
				"select count(*) as count from auth_admin_server where ip=? and password=?");
		ResultSet rs = null;
		try {
			// 查询帐号，用户名和密码
			ps.setString(1, ip);
			ps.setString(2, password);
			rs = ps.executeQuery();
			if (rs.next()) {
				if (rs.getInt("count") == 0) {
					// 用户名密码错误
					return 0;
				} else {
					return 1;
				}
			}
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			closeResultSet(rs);
			closePrepStatement(ps);
		}
	}

	public int test(Connection conn) {
		PreparedStatement pst = getPrepStatement(conn, "select 123");
		// 获取结果集
		ResultSet rs = null;
		try {
			rs = pst.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// 关闭数据库资源
			closeResultSet(rs);
			closePrepStatement(pst);
		}
		return -1;
	}
}