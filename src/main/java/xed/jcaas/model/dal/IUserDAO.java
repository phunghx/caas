package xed.jcaas.model.dal;

import xed.jcaas.model.entity.Pageable;
import xed.jcaas.model.entity.RoleInfo;
import xed.jcaas.model.entity.UserInfo;
//import com.rever.jcaas.model.entity.UserProfile;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IUserDAO {

    public UserInfo insertUser(String username, String password, boolean isActive) throws Exception;

    public void insertUserPermission(String username, String permission) throws Exception;

    public void insertUserPermission(String username, Set<String> permissions) throws Exception;

    public void insertUserRole(String username, int roleId, long expireTime) throws Exception;

    public void insertUserRole(String username, Map<Integer, Long> roleIds) throws Exception;

    public boolean isActiveUser(String username, String password) throws Exception;

    public boolean isExistUser(String username) throws Exception;

    public List getAllPermissionOfUser(String username) throws Exception;

    public List getUserPermision(String username) throws Exception;

    public List getAllRoleOfUser(String username) throws Exception;

    public List<RoleInfo> getAllRoleInfoOfUser(String username) throws Exception;

    public List getAllUsername() throws Exception;

    public Pageable<String> getUsernameActive(int from, int size) throws Exception;

    public List getAllUserRoleInfo(int from, int size) throws Exception;

    public int countAllUserInfo() throws Exception;

    public List<UserInfo> searchUserRoleInfo(String searchUsername, int from, int size) throws Exception;

    public int countUserRoleInfo(String searchUsername) throws Exception;

    public List<UserInfo> getListUserRoleInfoWithHighestRoleFilter(List<Integer> notInRoleIds, List<Integer> inRoleIds, int from, int size) throws Exception;

    public int countUserRoleInfoWithHighestRoleFilter(List<Integer> notInRoleIds, List<Integer> inRoleIds) throws Exception;

    public List<UserInfo> searchUserRoleInfoWithHighestRoleFilter(String searchUsername, List<Integer> notInRoleIds, List<Integer> inRoleIds, int from, int size) throws Exception;

    public int countUserRoleInfoWithHighestRoleFilter(String searchUsername, List<Integer> notInRoleIds, List<Integer> inRoleIds) throws Exception;

    public UserInfo getUserInfo(String username) throws Exception;

    public void activeUser(String username, boolean isActive) throws Exception;

    public void resetPasswordUser(String username, String password) throws Exception;

    public void updatePasswordUser(String username, String oldPass, String newPass) throws Exception;

    public void deleteUser(String username) throws Exception;

    public void deleteUserPermission(String username, String permission) throws Exception;

    public void deleteUserPermission(String username, Set<String> permissions) throws Exception;

    public void deleteUserRole(String username, int roleId) throws Exception;

    public void deleteUserRole(String username, Set<Integer> roleIds) throws Exception;

    public void replaceUserPermission(String username, Set<String> oldPermissions, Set<String> newPermissions) throws Exception;

    public void replaceUserRole(String username, Set<Integer> oldRoleIds, Map<Integer, Long> newRoleIds) throws Exception;

    public boolean isPassword(String username, String password) throws Exception;

}
