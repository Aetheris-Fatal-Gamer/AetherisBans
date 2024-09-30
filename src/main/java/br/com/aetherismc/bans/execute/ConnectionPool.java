package br.com.aetherismc.bans.execute;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Executor;

public class ConnectionPool {
	
	private final Vector<ConnectionPool.JDCConnection> connections;
	private final String url;
	private final String user;
	private final String password;

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public ConnectionPool(String url, String user, String password) throws ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		this.url = url;
		this.user = user;
		this.password = password;
		this.connections = new Vector(10);
        ConnectionReaper reaper = new ConnectionReaper();
		reaper.start();
	}

	public synchronized Connection getConnection() throws SQLException {
		ConnectionPool.JDCConnection conn;
		for(int i = 0; i < this.connections.size(); ++i) {
			conn = (ConnectionPool.JDCConnection)this.connections.get(i);
			if (conn.lease()) {
				if (conn.isValid()) {
					return conn;
				}
				this.connections.remove(conn);
				conn.terminate();
			}
		}
		
		conn = new ConnectionPool.JDCConnection(DriverManager.getConnection(this.url, this.user, this.password));
		conn.lease();
		if (!conn.isValid()) {
			conn.terminate();
			throw new SQLException("Failed to validate a brand new connection");
		} else {
			this.connections.add(conn);
			return conn;
		}
	}

	private synchronized void reapConnections() {
		long stale = System.currentTimeMillis() - 60000L;
		
		for ( JDCConnection connREG : connections ) {
            if (((JDCConnection) connREG).inUse() && stale > ((JDCConnection) connREG).getLastUse() && !((JDCConnection) connREG).isValid()) {
				this.connections.remove((JDCConnection) connREG);
			}
		}
	}

	public synchronized void closeConnections() {
		Enumeration<JDCConnection> conns = this.connections.elements();
		
		while( conns.hasMoreElements() ) {
			ConnectionPool.JDCConnection conn = (ConnectionPool.JDCConnection)conns.nextElement();
			this.connections.remove(conn);
			conn.terminate();
		}
	}

	private class JDCConnection implements Connection {
		private final Connection conn;
		private boolean inuse;
		private long timestamp;
		
		public JDCConnection(Connection conn) {
			this.conn = conn;
			this.inuse = false;
			this.timestamp = 0L;
		}

		public void terminate() {
			try {
				this.conn.close();
			} catch (SQLException ignored) {
			}
		}

		public synchronized boolean lease() {
			if (this.inuse) {
				return false;
			} else {
				this.inuse = true;
				this.timestamp = System.currentTimeMillis();
				return true;
			}
		}

		public boolean inUse() {
			return this.inuse;
		}

		public long getLastUse() {	
			return this.timestamp;
		}
		
		public void close() {
			this.inuse = false;

			try {
				if (!this.conn.getAutoCommit()) {
					this.conn.setAutoCommit(true);
				}
			} catch (SQLException var2) {
				ConnectionPool.this.connections.remove(this.conn);
				this.terminate();
			}
		}

		public PreparedStatement prepareStatement(String sql) throws SQLException {
			return this.conn.prepareStatement(sql);
		}

		public CallableStatement prepareCall(String sql) throws SQLException {
			return this.conn.prepareCall(sql);
		}

		public Statement createStatement() throws SQLException {
			return this.conn.createStatement();
		}

		public String nativeSQL(String sql) throws SQLException {
			return this.conn.nativeSQL(sql);
		}

		public void setAutoCommit(boolean autoCommit) throws SQLException {
			this.conn.setAutoCommit(autoCommit);
		}

		public boolean getAutoCommit() throws SQLException {
			return this.conn.getAutoCommit();
		}

		public void commit() throws SQLException {
			this.conn.commit();
		}

		public void rollback() throws SQLException {
			this.conn.rollback();
		}

		public boolean isClosed() throws SQLException {
			return this.conn.isClosed();
		}

		public DatabaseMetaData getMetaData() throws SQLException {
			return this.conn.getMetaData();
		}

		public void setReadOnly(boolean readOnly) throws SQLException {
			this.conn.setReadOnly(readOnly);
		}

		public boolean isReadOnly() throws SQLException {
			return this.conn.isReadOnly();
		}

		public void setCatalog(String catalog) throws SQLException {
			this.conn.setCatalog(catalog);
		}

		public String getCatalog() throws SQLException {
			return this.conn.getCatalog();
		}

		public void setTransactionIsolation(int level) throws SQLException {
			this.conn.setTransactionIsolation(level);
		}

		public int getTransactionIsolation() throws SQLException {
			return this.conn.getTransactionIsolation();
		}

		public SQLWarning getWarnings() throws SQLException {
			return this.conn.getWarnings();
		}

		public void clearWarnings() throws SQLException {
			this.conn.clearWarnings();
		}

		public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
			return this.conn.createArrayOf(typeName, elements);
		}

		public Blob createBlob() throws SQLException {
			return this.conn.createBlob();
		}

		public Clob createClob() throws SQLException {
			return this.conn.createClob();
		}

		public NClob createNClob() throws SQLException {
			return this.conn.createNClob();
		}

		public SQLXML createSQLXML() throws SQLException {
			return this.conn.createSQLXML();
		}

		public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
			return this.conn.createStatement(resultSetType, resultSetConcurrency);
		}

		public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
			return this.conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
		}

		public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
			return this.conn.createStruct(typeName, attributes);
		}

		public Properties getClientInfo() throws SQLException {
			return this.conn.getClientInfo();
		}

		public String getClientInfo(String name) throws SQLException {
	         return this.conn.getClientInfo(name);
		}

		public int getHoldability() throws SQLException {
			return this.conn.getHoldability();
		}

		public Map<String, Class<?>> getTypeMap() throws SQLException {
			return this.conn.getTypeMap();
		}

		public boolean isValid() {
			try {
				return this.conn.isValid(1);
			} catch (SQLException var2) {
				return false;
			}
		}

		public boolean isValid(int timeout) throws SQLException {
			return this.conn.isValid(timeout);	
		}

		public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
			return this.conn.prepareCall(sql, resultSetType, resultSetConcurrency);
		}

		public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
			return this.conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		}

		public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
			return this.conn.prepareStatement(sql, autoGeneratedKeys);
		}

		public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
			return this.conn.prepareStatement(sql, columnIndexes);
		}

		public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
			return this.conn.prepareStatement(sql, columnNames);
		}

		public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
			return this.conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
		}

		public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
			return this.conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		}

		public void releaseSavepoint(Savepoint savepoint) throws SQLException {
			this.conn.releaseSavepoint(savepoint);
		}

		public void rollback(Savepoint savepoint) throws SQLException {
			this.conn.rollback(savepoint);
		}

		public void setClientInfo(Properties properties) throws SQLClientInfoException {
			this.conn.setClientInfo(properties);
		}

		public void setClientInfo(String name, String value) throws SQLClientInfoException {
			this.conn.setClientInfo(name, value);
		}

		public void setHoldability(int holdability) throws SQLException {
			this.conn.setHoldability(holdability);
		}

		public Savepoint setSavepoint() throws SQLException {
			return this.conn.setSavepoint();
		}

		public Savepoint setSavepoint(String name) throws SQLException {
			return this.conn.setSavepoint(name);
		}

		public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
			this.conn.setTypeMap(map);
		}

		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			return this.conn.isWrapperFor(iface);
		}

		public <T> T unwrap(Class<T> iface) throws SQLException {
			return this.conn.unwrap(iface);
		}

		@Override
		public void abort(Executor executor) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getNetworkTimeout() throws SQLException {
			return 0;
		}

		@Override
		public String getSchema() throws SQLException {
			return null;
		}

		@Override
		public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		}

		@Override
		public void setSchema(String schema) throws SQLException {
		}
	}

	private class ConnectionReaper extends Thread {
		private ConnectionReaper() {
		}

		public void run() {
			while (true) {
				try {
					Thread.sleep(300000L);
				} catch (InterruptedException ignored) {
				}
				ConnectionPool.this.reapConnections();
			}
		}
	}
}
