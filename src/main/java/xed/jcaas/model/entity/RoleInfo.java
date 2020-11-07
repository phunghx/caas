package xed.jcaas.model.entity;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author duydb
 */
public class RoleInfo {
    private int id;
    private String name;
    private long expireTime = Long.MAX_VALUE;
    private Set<String> permissions = new HashSet<>();

    public RoleInfo(int id, String name, long expireTime) {
        this.id = id;
        this.name = name;
        this.expireTime = expireTime;
    }

    public RoleInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public RoleInfo() {
    }

    @Override
    public String toString() {
        return String.format("id: %s, name=%s, permissions=%s", id, name, Arrays.toString(permissions.toArray(new String[0])));
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
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
     * @return expire time
     */
    public long getExpireTime() { return expireTime; }

    /**
     * @param expireTime for this role
     */
    public void setExpireTime(long expireTime) { this.expireTime = expireTime; }

    public boolean isExpired() {
        long currentTime = System.currentTimeMillis();
        return currentTime > expireTime;
    }
}