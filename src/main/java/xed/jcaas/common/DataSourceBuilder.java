/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xed.jcaas.common;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 *
 * @author sonpn
 */
public class DataSourceBuilder {

    protected static String MYSQL_DRIVER_CLASS = "com.mysql.jdbc.Driver";
    protected static String MYSQL_JDBC_URL = "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8";

    public static DataSource buildMySQLDataSource(String host, int port, String dbName, String username, String password) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(MYSQL_DRIVER_CLASS);
        ds.setJdbcUrl(String.format(MYSQL_JDBC_URL, host, port, dbName));
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setAutoCommit(true);
        return ds;
    }
}
