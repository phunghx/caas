package tf.jcaas.core;

import org.apache.shiro.authc.UsernamePasswordToken;


public class OAuthToken extends UsernamePasswordToken {

    public OAuthToken(String username) {
        super();
        setUsername(username);
    }

    @Override
    public Object getCredentials() {
        return null;
    }
}
