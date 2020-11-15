package tf.jcaas.core;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

import java.util.ArrayList;
import java.util.Collection;


public class OAuthAuthenticationInfo implements AuthenticationInfo {

    private PrincipalCollection principalCollection;

    public OAuthAuthenticationInfo(String userId, String realmName) {
        Collection<String> principals = new ArrayList<>();
        principals.add(userId);
        this.principalCollection = new SimplePrincipalCollection(principals, realmName);
    }

    @Override
    public PrincipalCollection getPrincipals() {
        return principalCollection;
    }

    @Override
    public Object getCredentials() {
        return null;
    }
}
