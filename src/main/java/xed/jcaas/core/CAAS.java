package xed.jcaas.core;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SimpleSessionFactory;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;

import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

/**
 * @since 22/05/2020
 * @author andy
 * Add session manager to CAAS
 */
public class CAAS {

    protected SessionsSecurityManager securityManagerInst;
    protected UserPasswordRealm userPassRealm;
    private SessionDAO _sessionDAO;

    public CAAS(DataSource ds, SessionDAO sessionDAO) {
        userPassRealm = new UserPasswordRealm();
        userPassRealm.setPermissionsLookupEnabled(true);
        userPassRealm.setDataSource(ds);
        userPassRealm.setCredentialsMatcher(new CustomCredentialMatcher());


        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.setSessionValidationSchedulerEnabled(false);
        sessionManager.setSessionFactory(new SimpleSessionFactory());
        sessionManager.setSessionDAO(sessionDAO);
        _sessionDAO = sessionDAO;

        securityManagerInst = new DefaultSecurityManager(userPassRealm);
        securityManagerInst.setSessionManager(sessionManager);
        SecurityUtils.setSecurityManager(securityManagerInst);

        if(securityManagerInst == null)
            throw new NullPointerException("security Manager null");
    }

    public String login(String username, String password, boolean rememberMe, long sessionTimeout) throws Exception {
        Subject currentUser = securityManagerInst.createSubject(new DefaultSubjectContext());
        UsernamePasswordToken myToken = new UsernamePasswordToken(username, password);
        myToken.setRememberMe(rememberMe);
        currentUser.login(myToken);
        if (currentUser.isAuthenticated()) {
            Session session = currentUser.getSession();
            session.setTimeout(sessionTimeout);
            session.touch();
            _sessionDAO.update(session);
            return (String) session.getId();
        } else {
            throw new AuthenticationException("login fail");
        }
    }

    public String loginOAuth(String username, boolean rememberMe, long sessionTimeout) throws Exception {
        Subject currentUser = securityManagerInst.createSubject(new DefaultSubjectContext());
        OAuthToken myToken = new OAuthToken(username);
        myToken.setRememberMe(rememberMe);
        currentUser.login(myToken);
        if (currentUser.isAuthenticated()) {
            Session session = currentUser.getSession();
            session.setTimeout(sessionTimeout);
            session.touch();
            _sessionDAO.update(session);
            return (String) session.getId();
        } else {
            throw new AuthenticationException("login oauth fail");
        }
    }

    public void logout(String sessionId) throws Exception {
        Subject currentUser = getCurrentUser(sessionId);
        currentUser.logout();
    }

    public String renewSession(String oldSessionId, long sessionTimeout) throws UnauthenticatedException {
        Subject currentUser = new Subject.Builder(securityManagerInst).sessionId(oldSessionId).buildSubject();
        if (currentUser == null) {
            throw new UnauthenticatedException("Session  not exist");
        }
        Session session = currentUser.getSession(true);
        if (session != null) {
            session.setTimeout(sessionTimeout);
            session.touch();
            _sessionDAO.update(session);
        }
        return (String) session.getId();
    }

    private Subject getCurrentUser(String sessionId) throws UnauthenticatedException {
        Subject currentUser = new Subject.Builder(securityManagerInst).sessionId(sessionId).buildSubject();
        if (currentUser == null) {
            throw new UnauthenticatedException("Session not exist");
        }
        Session session = currentUser.getSession(false);
        if (session != null) {
            session.touch();
        }
        return currentUser;
    }

    public boolean isPermitted(String sessionId, String permission) throws Exception {
        Subject currentUser = getCurrentUser(sessionId);
        return currentUser.isPermitted(permission);
    }

    public boolean[] isPermitted(String sessionId, String... permissions) throws Exception {
        Subject currentUser = getCurrentUser(sessionId);
        return currentUser.isPermitted(permissions);
    }

    public boolean isPermittedAll(String sessionId, String... permissions) throws Exception {
        Subject currentUser = getCurrentUser(sessionId);
        return currentUser.isPermittedAll(permissions);
    }

    public boolean hasRole(String sessionId, String roleName) throws Exception {
        Subject currentUser = getCurrentUser(sessionId);
        return currentUser.hasRole(roleName);
    }

    public boolean[] hasRoles(String sessionId, List<String> roleName) throws Exception {
        Subject currentUser = getCurrentUser(sessionId);
        return currentUser.hasRoles(roleName);
    }

    public boolean hasAllRole(String sessionId, Collection<String> roleName) throws Exception {
        Subject currentUser = getCurrentUser(sessionId);
        return currentUser.hasAllRoles(roleName);
    }

    public String getUser(String sessionId) throws Exception {
        Subject currentUser = getCurrentUser(sessionId);
        if (currentUser == null || !currentUser.isAuthenticated()) {
            throw new UnauthenticatedException("Session expired or not exist");
        }
        return currentUser.getPrincipal().toString();
    }

    public boolean isPermittedUser(String username, String permissions) throws Exception {
        PrincipalCollection principal = new SimplePrincipalCollection(username, "");
        return userPassRealm.isPermitted(principal, permissions);
    }

    public boolean[] isPermittedUser(String username, String... permissions) throws Exception {
        PrincipalCollection principal = new SimplePrincipalCollection(username, "");
        return userPassRealm.isPermitted(principal, permissions);
    }

    public boolean isPermittedUserAll(String username, String... permissions) throws Exception {
        PrincipalCollection principal = new SimplePrincipalCollection(username, "");
        return userPassRealm.isPermittedAll(principal, permissions);
    }

    public boolean hasRoleUser(String username, String roleName) throws Exception {
        PrincipalCollection principal = new SimplePrincipalCollection(username, "");
        return userPassRealm.hasRole(principal, roleName);
    }

    public boolean[] hasRolesUser(String username, List<String> roleName) throws Exception {
        PrincipalCollection principal = new SimplePrincipalCollection(username, "");
        return userPassRealm.hasRoles(principal, roleName);
    }

    public boolean hasAllRoleUser(String username, Collection<String> roleName) throws Exception {
        PrincipalCollection principal = new SimplePrincipalCollection(username, "");
        return userPassRealm.hasAllRoles(principal, roleName);
    }
}
