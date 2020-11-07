package xed.jcaas.model.dal;

import xed.jcaas.model.entity.RoleInfo;

import java.util.List;
import java.util.Set;

public interface IRoleDAO {

    public void insertRole(int roleId, String roleName) throws Exception;

    public void insertRole(List<Integer> roleIds, List<String> roleNames) throws Exception;

    public void insertRoleWithPermission(int roleId, String roleName, Set<String> permissions) throws Exception;

    public void addRolePermission(int roleId, String permission) throws Exception;

    public void addRolePermission(int roleId, Set<String> permissions) throws Exception;

    public List getAllRole() throws Exception;

    public List getAllRoleWithPermissions() throws Exception;

    public RoleInfo getRoleInfo(int roleId) throws Exception;

    public void deleteRole(int roleId) throws Exception;

    public void deleteRolePermission(int roleId, String permission) throws Exception;

    public void deleteRolePermission(int roleId, Set<String> permissions) throws Exception;

    public void updateRoleName(int roleId, String newRoleName) throws Exception;

    public void replaceRolePermission(int roleId, Set<String> oldPermissions, Set<String> newPermissions) throws Exception;

}
