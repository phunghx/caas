package tf.jcaas.model.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserInfo {

    private String username;

    private String password;

    private List<RoleInfo> roles = new ArrayList<>();

    protected boolean isActive;

    protected long createTime;

    private Set<String> permissions = new HashSet<>();

    public UserInfo() {

    }

    public UserInfo(String username, boolean isActive, long createTime) {
        this.username = username;
        this.isActive = isActive;
        this.createTime = createTime;
    }

    public void addRole(RoleInfo role) {
        synchronized (this) {
            if (roles == null) {
                roles = new ArrayList<>();
            }
        }
        roles.add(role);
    }

    @Override
    public String toString() {
        return String.format("username: %s, roles=%s, isActive=%s, createTime=%s,permissions=%s",
            username, Arrays.toString(roles.toArray(new RoleInfo[0])), isActive, createTime, Arrays.toString(permissions.toArray(new String[0])));
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the roles
     */
    public List<RoleInfo> getRoles() {
        return roles;
    }

    /**
     * @param roles the roles to set
     */
    public void setRoles(List<RoleInfo> roles) {
        this.roles = roles;
    }

    /**
     * @return the permissions
     */
    public Set<String> getPermissions() {
        return permissions;
    }

    /**
     * @param permissions the permissions to set
     */
    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    /**
     * @return the _isActive
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * @param _isActive the _isActive to set
     */
    public void setActive(boolean _isActive) {
        this.isActive = _isActive;
    }

    /**
     * @return the _createDate
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * @param _createTime the _createDate to set
     */
    public void setCreateTime(long _createTime) {
        this.createTime = _createTime;
    }

}
