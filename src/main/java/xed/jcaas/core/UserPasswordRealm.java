/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xed.jcaas.core;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.util.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author sonpn
 */
public class UserPasswordRealm extends JdbcRealm {

    protected static final Logger log = LoggerFactory.getLogger(JdbcRealm.class);

	/*--------------------------------------------
   |             C O N S T A N T S             |
	 ============================================*/
    /**
     * The default query used to retrieve account data for the user.
     */
//	protected static final String DEFAULT_AUTHENTICATION_QUERY = "SELECT password FROM user WHERE username=? AND active=1";

    /**
     * The default query used to retrieve the roles that apply to a user.
     */
//	protected static final String DEFAULT_USER_ROLES_QUERY = "SELECT role.role_name FROM role,user_roles,user WHERE role.role_id=user_roles.role_id AND user_roles.username=user.username AND user.username=?";

    /**
     * The default query used to retrieve permissions that apply to a particular
     * role.
     */
//	protected static final String DEFAULT_PERMISSIONS_QUERY = "SELECT permission FROM role_permissions,role WHERE role_permissions.role_id=role.role_id AND role.role_name=?";

    protected static final String DEFAULT_PERMISSIONS_USER_QUERY = "SELECT permission FROM user_permissions WHERE username=?";

//	protected String authenticationQuery = DEFAULT_AUTHENTICATION_QUERY;

//	protected String userRolesQuery = DEFAULT_USER_ROLES_QUERY;

//	protected String permissionsQuery = DEFAULT_PERMISSIONS_QUERY;

    protected String permissionUserQuery = DEFAULT_PERMISSIONS_USER_QUERY;

    public UserPasswordRealm() {
        this.authenticationQuery = "SELECT password FROM user WHERE username=? AND active=1";
        this.userRolesQuery = "SELECT role.role_name FROM role,user_roles,user WHERE role.role_id=user_roles.role_id AND user_roles.username=user.username AND user.username=?";
        this.permissionsQuery = "SELECT permission FROM role_permissions,role WHERE role_permissions.role_id=role.role_id AND role.role_name=?";
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (token instanceof OAuthToken) {
            OAuthToken oauthToken = (OAuthToken) token;
            String username = oauthToken.getUsername();

            // Null username is invalid
            if (username == null) {
                throw new AccountException("Null username are not allowed by this realm.");
            }
            Connection conn = null;
            OAuthAuthenticationInfo info = null;
            try {
                conn = dataSource.getConnection();

                String password = getPasswordForUser(conn, username);
                if (password == null) {
                    throw new UnknownAccountException("No account found for user [" + username + "]");
                }
                info = new OAuthAuthenticationInfo(username, getName());
            } catch (SQLException e) {
                final String message = "There was a SQL error while authenticating user [" + username + "]";
                if (log.isErrorEnabled()) {
                    log.error(message, e);
                }

                // Rethrow any SQL errors as an authentication exception
                throw new AuthenticationException(message, e);
            } finally {
                JdbcUtils.closeConnection(conn);
            }
            return info;
        } else return super.doGetAuthenticationInfo(token);
    }

    private String getPasswordForUser(Connection conn, String username) throws SQLException {
        String result = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(authenticationQuery);
            ps.setString(1, username);

            // Execute query
            rs = ps.executeQuery();

            // Loop over results - although we are only expecting one result, since usernames should be unique
            boolean foundResult = false;
            while (rs.next()) {

                // Check to ensure only one row is processed
                if (foundResult) {
                    throw new AuthenticationException("More than one user row found for user [" + username + "]. Usernames must be unique.");
                }

                result = rs.getString(1);
                foundResult = true;
            }
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
        }
        return result;
    }

    @Override
    protected Set<String> getPermissions(Connection conn, String username, Collection<String> roleNames) throws SQLException {
        PreparedStatement ps = null;
        Set<String> permissions = new LinkedHashSet<String>();
        try {
            ps = conn.prepareStatement(permissionsQuery);
            for (String roleName : roleNames) {

                ps.setString(1, roleName);

                ResultSet rs = null;

                try {
                    // Execute query
                    rs = ps.executeQuery();

                    // Loop over results and add each returned role to a set
                    while (rs.next()) {

                        String permissionString = rs.getString(1);

                        // Add the permission to the set of permissions
                        permissions.add(permissionString);
                    }
                } finally {
                    JdbcUtils.closeResultSet(rs);
                }

            }

            // get permission from user_permissions
            try (PreparedStatement ps_ = conn.prepareStatement(permissionUserQuery)) {
                ps_.setString(1, username);
                try (ResultSet rs = ps_.executeQuery()) {
                    while (rs.next()) {
                        permissions.add(rs.getString(1));
                    }
                }
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            JdbcUtils.closeStatement(ps);
        }
        return permissions;
    }
}
