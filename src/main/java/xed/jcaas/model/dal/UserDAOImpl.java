package xed.jcaas.model.dal;

import xed.jcaas.model.entity.Pageable;
import xed.jcaas.model.entity.RoleInfo;
import xed.jcaas.model.entity.UserInfo;

import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.BiConsumer;

public class UserDAOImpl implements IUserDAO {

    protected final DataSource _dataSource;

    //     <editor-fold defaultstate="collapsed" desc="SQL Query DB Section">
    private static final String SQL_INSER_USER = "INSERT INTO user (username, password, active, create_time) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_USER = "SELECT username, password, active, create_time FROM user WHERE username=?";
    private static final String SQL_SELECT_USER_PASS = "SELECT active FROM user WHERE username=? AND password=?";
    private static final String SQL_SELECT_ALL_USER = "SELECT username FROM user ORDER BY username ASC";
    private static final String SQL_SELECT_ALL_USER_INFO = "SELECT user.username, user.active, user.create_time, role.role_id, role.role_name, user_roles.expire_time FROM ( SELECT DISTINCT username, active, create_time FROM user %s) AS user LEFT JOIN user_roles ON user.username=user_roles.username LEFT JOIN role ON user_roles.role_id=role.role_id ORDER BY user.username ASC";
    private static final String SQL_COUNT_ALL_USER_INFO = "SELECT COUNT(DISTINCT username) FROM user";
    private static final String SQL_SEARCH_ALL_USER_INFO = "SELECT user.username, user.active, user.create_time, role.role_id, role.role_name, user_roles.expire_time FROM ( SELECT DISTINCT username, active, create_time FROM user WHERE username LIKE ? %s) AS user LEFT JOIN user_roles ON user.username = user_roles.username LEFT JOIN role ON user_roles.role_id = role.role_id ORDER BY user.username ASC";
    private static final String SQL_COUNT_SEARCH_ALL_USER_INFO = "SELECT COUNT(DISTINCT username) FROM user WHERE username LIKE ? ";
    private static final String SQL_SELECT_ALL_USER_INFO_WITH_HIGHEST_ROLE_FILTER = "SELECT user.username, user.active, user.create_time, role.role_id, role.role_name, user_roles.expire_time FROM (SELECT DISTINCT user.username, user.active, user.create_time FROM user %s %s) AS user LEFT JOIN user_roles ON user.username=user_roles.username LEFT JOIN role ON user_roles.role_id=role.role_id ORDER BY user.username ASC";
    private static final String SQL_NOT_IN_ROLE = "user.username NOT IN (SELECT username FROM user_roles WHERE role_id IN (%s))";
    private static final String SQL_IN_ROLE = "user.username IN (SELECT username FROM user_roles WHERE role_id IN (%s))";

    private static final String SQL_COUNT_ALL_USER_INFO_WITH_HIGHEST_ROLE_FILTER = "SELECT COUNT(DISTINCT user.username) FROM user %s ";

    private static final String SQL_SEARCH_ALL_USER_INFO_WITH_HIGHEST_ROLE_FILTER = "SELECT user.username, user.active, user.create_time, role.role_id, role.role_name, user_roles.expire_time FROM ( SELECT DISTINCT user.username, user.active, user.create_time FROM user WHERE user.username LIKE ? %s %s ) AS user LEFT JOIN user_roles ON user.username = user_roles.username LEFT JOIN role ON user_roles.role_id = role.role_id ORDER BY user.username ASC";

    private static final String SQL_COUNT_SEARCH_ALL_USER_INFO_WITH_HIGHEST_ROLE_FILTER = "SELECT COUNT( DISTINCT user.username) FROM user WHERE user.username LIKE ? %s";

    private static final String SQL_IS_PASSWORD = "SELECT username FROM user WHERE username=? and password =?";
    private static final String SQL_RESET_USER_PASS = "UPDATE user SET password=? WHERE username=?";
    private static final String SQL_UPDATE_USER_PASS = "UPDATE user SET password=? WHERE username=? AND password=?";
    private static final String SQL_UPDATE_USER_INFO = "UPDATE user SET active=? WHERE username=?";
    private static final String SQL_ACTIVE_USER = "UPDATE user SET active=? WHERE username=?";
    private static final String SQL_DELETE_USER = "DELETE FROM user WHERE username=?";

    private static final String SQL_INSERT_USER_PER = "INSERT INTO user_permissions (permission, username) VALUES (?, ?) ";
    private static final String SQL_UPDATE_DUPLICATE_USER_PER = " ON DUPLICATE KEY UPDATE permission=VALUES(permission), username=VALUES(username) ";
    private static final String SQL_SELECT_USER_PERM = "SELECT permission FROM user_permissions WHERE username=?";
    private static final String SQL_USER_PER_FIELD = " ,(?, ?)";
    private static final int SQL_USER_PER_NUMFIELD = 2;
    private final String SQL_DELETE_USER_PERM_LIST = "DELETE FROM user_permissions WHERE (permission, username) IN ( (?, ?) %s )";

    private static final String SQL_INSER_USER_ROLE = "INSERT INTO user_roles (role_id, username, expire_time) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE_DUPLICATE_USER_ROLE = " ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), username=VALUES(username), expire_time=VALUES(expire_time)";
    private static final String SQL_USER_ROLE_FIELD = " ,(?, ?, ?)";
    private static final int SQL_USER_ROLE_NUMFIELD = 3;
    private static final String SQL_DELETE_USER_ROLE_LIST = "DELETE FROM user_roles WHERE (role_id, username) IN ( (?, ?) %s )";

    private static final String SQL_SELECT_ROLE_BY_USER = "SELECT role_result.role_id, role_result.role_name, role_result.permission, user_roles.expire_time FROM user_roles JOIN ( SELECT role.role_id, role.role_name, role_permissions.permission FROM role LEFT JOIN role_permissions ON role.role_id=role_permissions.role_id) AS role_result ON user_roles.role_id=role_result.role_id WHERE user_roles.username=?";
    private static final String SQL_SELECT_USER_ROLE_BY_USER = "select * from role, user_roles where role.role_id = user_roles.role_id and user_roles.username=?";

    private static final String SQL_SELECT_ROLE_NAME_BY_USER = "SELECT role.role_name FROM user_roles JOIN role ON user_roles.role_id=role.role_id WHERE user_roles.username=?";

    private static final String SQL_SELECT_PERMISSION_FROM_ROLE_PERM_BY_USER = "SELECT role_permissions.permission FROM user_roles JOIN role_permissions ON role_permissions.role_id=user_roles.role_id WHERE user_roles.username=?";
    private static final String SQL_SELECT_PERMISSION_FROM_USER_PERM_BY_USER = "SELECT permission FROM user_permissions WHERE username=?";

    // </editor-fold>

    public UserDAOImpl(DataSource dataSource) {
        this._dataSource = dataSource;
    }

    // <editor-fold defaultstate="collapsed" desc="Insert Section">
    @Override
    public UserInfo insertUser(String username, String password, boolean isActive) throws Exception {
        long createTime = System.currentTimeMillis();
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_INSER_USER)) {
                prepareStatement.setString(1, username);
                prepareStatement.setString(2, password);
                prepareStatement.setBoolean(3, isActive);
                prepareStatement.setLong(4, createTime);

                if (prepareStatement.executeUpdate() == 0) {
                    throw new SQLException("Not register role.");
                }
            }
        }
        UserInfo userInfo = new UserInfo(username, isActive, createTime);
        userInfo.setPassword(password);
        userInfo.setRoles(new ArrayList<>());
        userInfo.setPermissions(new HashSet<>());
        return userInfo;
    }

    @Override
    public void insertUserPermission(String username, String permission) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_INSERT_USER_PER + SQL_UPDATE_DUPLICATE_USER_PER)) {
                prepareStatement.setString(1, permission);
                prepareStatement.setString(2, username);
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void insertUserPermission(String username, Set<String> permissions) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            StringBuilder query = new StringBuilder(SQL_INSERT_USER_PER);
            for (int i = 1; i < permissions.size(); i++) {
                query.append(SQL_USER_PER_FIELD);
            }
            query.append(SQL_UPDATE_DUPLICATE_USER_PER);
            try (PreparedStatement prepareStatement = connection.prepareStatement(query.toString())) {
                int i = 0;
                for (String permission : permissions) {
                    prepareStatement.setString(i * SQL_USER_PER_NUMFIELD + 1, permission);
                    prepareStatement.setString(i * SQL_USER_PER_NUMFIELD + 2, username);
                    i++;
                }
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void insertUserRole(String username, int roleId, long expireTime) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_INSER_USER_ROLE + SQL_UPDATE_DUPLICATE_USER_ROLE)) {
                prepareStatement.setInt(1, roleId);
                prepareStatement.setString(2, username);
                prepareStatement.setLong(3, expireTime);
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void insertUserRole(String username, Map<Integer, Long> roleIds) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            connection.setAutoCommit(true);
            StringBuilder query = new StringBuilder(SQL_INSER_USER_ROLE);
            for (int i = 1; i < roleIds.size(); i++) {
                query.append(SQL_USER_ROLE_FIELD);
            }
            query.append(SQL_UPDATE_DUPLICATE_USER_ROLE);
            try (PreparedStatement prepareStatement = connection.prepareStatement(query.toString())) {
                int i = 0;
                for (Map.Entry<Integer, Long> tuple : roleIds.entrySet()) {
                    prepareStatement.setInt(i * SQL_USER_ROLE_NUMFIELD + 1, tuple.getKey());
                    prepareStatement.setString(i * SQL_USER_ROLE_NUMFIELD + 2, username);
                    prepareStatement.setLong(i * SQL_USER_ROLE_NUMFIELD + 3, tuple.getValue());
                    i++;
                }
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Select Section">
    @Override
    public boolean isActiveUser(String username, String password) throws Exception {
        boolean enable = false;
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_USER_PASS)) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs != null && rs.next()) {
                        try {
                            enable = rs.getBoolean("active");
                        } catch (Exception ex) {

                        }
                    } else {
                        throw new Exception("Username or password is incorrect");
                    }
                }
            }
        }
        return enable;
    }

    /**
     * @return all user permision in table user_permission & role_permission
     */
    @Override
    public List<String> getAllPermissionOfUser(String username) throws Exception {
        List<String> permissions = new ArrayList<>();
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_PERMISSION_FROM_ROLE_PERM_BY_USER)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs != null && rs.next()) {
                        try {
                            permissions.add(rs.getString("permission"));
                        } catch (Exception ex) {
                            // ignore
                        }
                    }
                }
            }
            try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_PERMISSION_FROM_USER_PERM_BY_USER)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs != null && rs.next()) {
                        try {
                            permissions.add(rs.getString("permission"));
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        }
        return permissions;
    }

    /**
     * @return all user permision in table user_permission
     */
    @Override
    public List<String> getUserPermision(String username) throws Exception {
        List<String> permissions = new ArrayList<>();
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_PERMISSION_FROM_USER_PERM_BY_USER)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs != null && rs.next()) {
                        try {
                            permissions.add(rs.getString("permission"));
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        }
        return permissions;
    }

    @Override
    public boolean isExistUser(String username) throws Exception {
        boolean exist = false;
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_USER)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        exist = true;
                    }
                }
            }
        }
        return exist;
    }

    @Override
    public List<String> getAllUsername() throws Exception {
        List<String> user = new ArrayList<>();
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_ALL_USER)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try {
                            user.add(rs.getString("username"));
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        }
        return user;
    }

    @Override
    public Pageable<String> getUsernameActive(int from, int size) throws Exception {
        long total = 0;
        List<String> users = new ArrayList<>();
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(username) FROM user WHERE active=1")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        try {
                            total = rs.getLong(1);
                        } catch (Exception ex) {
                        }
                    }
                }
            }
            try (PreparedStatement ps = connection.prepareStatement("SELECT username FROM user WHERE active=1 ORDER BY create_time DESC LIMIT ?,?")) {
                ps.setInt(1, from);
                ps.setInt(2, size);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try {
                            users.add(rs.getString("username"));
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        }
        return new Pageable<>(total, users);
    }


    @Override
    public UserInfo getUserInfo(String username) throws Exception {
        UserInfo user = null;
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_USER)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        user = new UserInfo();
                        user.setUsername(rs.getString("username"));
                        user.setPassword(rs.getString("password"));
                        user.setActive(rs.getBoolean("active"));
                        user.setCreateTime(rs.getLong("create_time"));
                        try (PreparedStatement _prepareStatement = connection.prepareStatement(SQL_SELECT_USER_PERM)) {
                            _prepareStatement.setString(1, username);
                            try (ResultSet _rs = _prepareStatement.executeQuery()) {
                                Set<String> permissions = new HashSet<>();
                                while (_rs.next()) {
                                    permissions.add(_rs.getString("permission"));
                                }
                                user.setPermissions(permissions);
                            }
                        }
                        try (PreparedStatement pStUserRole = connection.prepareStatement(SQL_SELECT_ROLE_BY_USER)) {
                            pStUserRole.setString(1, username);
                            try (ResultSet _rs = pStUserRole.executeQuery()) {
                                List<RoleInfo> roles = new ArrayList<>();
                                List<Integer> roleIds = new ArrayList<>();
                                while (_rs.next()) {
                                    RoleInfo role = new RoleInfo(_rs.getInt("role_id"), _rs.getString("role_name"), _rs.getLong("expire_time"));

                                    String permission = _rs.getString("permission");

                                    int idx = roleIds.indexOf(role.getId());
                                    if (idx == -1) {
                                        role.setPermissions(new HashSet());
                                        roleIds.add(role.getId());
                                        roles.add(role);
                                        idx = 0;
                                    }
                                    if (permission != null) {
                                        roles.get(idx).getPermissions().add(permission);
                                    }

                                }
                                user.setRoles(roles);
                            }
                        }
                    }
                }
            }
        }
        return user;
    }

    @Override
    public List<String> getAllRoleOfUser(String username) throws Exception {
        List<String> roles = null;
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_ROLE_NAME_BY_USER)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs != null) {
                        roles = new ArrayList<>();
                        while (rs.next()) {
                            roles.add(rs.getString("role_name"));
                        }
                    }
                }
            }
        }
        return roles;
    }

    @Override
    public List<RoleInfo> getAllRoleInfoOfUser(String username) throws Exception {
        List<RoleInfo> roles = new ArrayList<>();
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_USER_ROLE_BY_USER)) {
                ps.setString(1, username);
                try (ResultSet _rs = ps.executeQuery()) {
                    while (_rs.next()) {
                        RoleInfo role = new RoleInfo(_rs.getInt("role_id"), _rs.getString("role_name"), _rs.getLong("expire_time"));
                        roles.add(role);
                    }
                }
            }
        }
        return roles;
    }

    @Override
    public List<UserInfo> getAllUserRoleInfo(int from, int size) throws Exception {
        List<UserInfo> usersInfo = null;
        try (Connection connection = _dataSource.getConnection()) {
            String limit = "";
            if (from >= 0 && size >= 0) {
                limit = " LIMIT " + from + "," + size;
            }
            String query = String.format(SQL_SELECT_ALL_USER_INFO, limit);
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs != null) {
                        usersInfo = new ArrayList<>();
                        Map<String, Integer> storeUserIdx = new HashMap<>();
                        while (rs.next()) {
                            String username = rs.getString("username");
                            if (storeUserIdx.containsKey(username)) {
                                if (rs.getObject("role_id") != null) {
                                    RoleInfo role = new RoleInfo(rs.getInt("role_id"), rs.getString("role_name"), rs.getLong("expire_time"));
                                    usersInfo.get(storeUserIdx.get(username)).addRole(role);
                                }
                            } else {
                                UserInfo userInfo = new UserInfo(username, rs.getBoolean("active"), rs.getLong("create_time"));

                                if (rs.getObject("role_id") != null) {
                                    RoleInfo role = new RoleInfo(rs.getInt("role_id"), rs.getString("role_name"), rs.getLong("expire_time"));
                                    userInfo.addRole(role);
                                }
                                storeUserIdx.put(username, usersInfo.size());
                                usersInfo.add(userInfo);
                            }
                        }
                    }
                }
            }
        }
        return usersInfo;
    }

    @Override
    public int countAllUserInfo() throws Exception {
        int total = -1;
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(SQL_COUNT_ALL_USER_INFO)) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs != null && rs.next()) {
                        total = rs.getInt(1);
                    }
                }
            }
        }
        return total;
    }

    @Override
    public List<UserInfo> searchUserRoleInfo(String searchUsername, int from, int size) throws Exception {
        List<UserInfo> usersInfo = null;
        try (Connection connection = _dataSource.getConnection()) {
            String limit = "";
            if (from >= 0 && size >= 0) {
                limit = " LIMIT " + from + "," + size;
            }
            String query = String.format(SQL_SEARCH_ALL_USER_INFO, limit);
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, "%" + searchUsername + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs != null) {
                        usersInfo = new ArrayList<>();
                        Map<String, Integer> storeUserIdx = new HashMap<>();
                        while (rs.next()) {
                            String username = rs.getString("username");
                            if (storeUserIdx.containsKey(username)) {
                                if (rs.getObject("role_id") != null) {
                                    RoleInfo role = new RoleInfo(rs.getInt("role_id"), rs.getString("role_name"), rs.getLong("expire_time"));
                                    usersInfo.get(storeUserIdx.get(username)).addRole(role);
                                }
                            } else {
                                UserInfo userInfo = new UserInfo(username, rs.getBoolean("active"), rs.getLong("create_time"));

                                if (rs.getObject("role_id") != null) {
                                    RoleInfo role = new RoleInfo(rs.getInt("role_id"), rs.getString("role_name"), rs.getLong("expire_time"));
                                    userInfo.addRole(role);
                                }
                                storeUserIdx.put(username, usersInfo.size());
                                usersInfo.add(userInfo);
                            }
                        }
                    }
                }
            }
        }
        return usersInfo;
    }

    @Override
    public int countUserRoleInfo(String searchUsername) throws Exception {
        int total = -1;
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(SQL_COUNT_SEARCH_ALL_USER_INFO)) {
                ps.setString(1, "%" + searchUsername + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs != null && rs.next()) {
                        total = rs.getInt(1);
                    }
                }
            }
        }
        return total;
    }

    @Override
    public List<UserInfo> getListUserRoleInfoWithHighestRoleFilter(List<Integer> notInRoleIds, List<Integer> inRoleIds, int from, int size) throws Exception {
        String partQuery = null;
        if (notInRoleIds != null && notInRoleIds.isEmpty() == false) {
            partQuery = String.format(SQL_NOT_IN_ROLE, StringUtils.join(notInRoleIds, ","));
        }
        if (inRoleIds != null && inRoleIds.isEmpty() == false) {
            partQuery = partQuery != null ? partQuery + " AND " + String.format(SQL_IN_ROLE, StringUtils.join(inRoleIds, ",")) : String.format(SQL_IN_ROLE, StringUtils.join(inRoleIds, ","));
        }
        List<UserInfo> usersInfo = null;
        try (Connection connection = _dataSource.getConnection()) {
            String limit = "";
            if (from >= 0 && size >= 0) {
                limit = " LIMIT " + from + "," + size;
            }
            String query = String.format(SQL_SELECT_ALL_USER_INFO_WITH_HIGHEST_ROLE_FILTER, partQuery == null ? " WHERE 1 " : " WHERE " + partQuery, limit);
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs != null) {
                        usersInfo = new ArrayList<>();
                        Map<String, Integer> storeUserIdx = new HashMap<>();
                        while (rs.next()) {
                            String username = rs.getString("username");
                            if (storeUserIdx.containsKey(username)) {
                                if (rs.getObject("role_id") != null) {
                                    RoleInfo role = new RoleInfo(rs.getInt("role_id"), rs.getString("role_name"), rs.getLong("expire_time"));
                                    usersInfo.get(storeUserIdx.get(username)).addRole(role);
                                }
                            } else {
                                UserInfo userInfo = new UserInfo(username, rs.getBoolean("active"), rs.getLong("create_time"));

                                if (rs.getObject("role_id") != null) {
                                    RoleInfo role = new RoleInfo(rs.getInt("role_id"), rs.getString("role_name"), rs.getLong("expire_time"));
                                    userInfo.addRole(role);
                                }
                                storeUserIdx.put(username, usersInfo.size());
                                usersInfo.add(userInfo);
                            }
                        }
                    }
                }
            }
        }
        return usersInfo;
    }

    @Override
    public int countUserRoleInfoWithHighestRoleFilter(List<Integer> notInRoleIds, List<Integer> inRoleIds) throws Exception {
        String partQuery = null;
        if (notInRoleIds != null && notInRoleIds.isEmpty() == false) {
            partQuery = String.format(SQL_NOT_IN_ROLE, StringUtils.join(notInRoleIds, ","));
        }
        if (inRoleIds != null && inRoleIds.isEmpty() == false) {
            partQuery = partQuery != null ? partQuery + " AND " + String.format(SQL_IN_ROLE, StringUtils.join(inRoleIds, ",")) : String.format(SQL_IN_ROLE, StringUtils.join(inRoleIds, ","));
        }
        int total = -1;
        try (Connection connection = _dataSource.getConnection()) {
            String query = String.format(SQL_COUNT_ALL_USER_INFO_WITH_HIGHEST_ROLE_FILTER, partQuery == null ? " WHERE 1 " : " WHERE " + partQuery);
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs != null && rs.next()) {
                        total = rs.getInt(1);
                    }
                }
            }
        }
        return total;
    }

    @Override
    public List<UserInfo> searchUserRoleInfoWithHighestRoleFilter(String searchUsername, List<Integer> notInRoleIds, List<Integer> inRoleIds, int from, int size) throws Exception {
        String partQuery = null;
        if (notInRoleIds != null && notInRoleIds.isEmpty() == false) {
            partQuery = String.format(SQL_NOT_IN_ROLE, StringUtils.join(notInRoleIds, ","));
        }
        if (inRoleIds != null && inRoleIds.isEmpty() == false) {
            partQuery = partQuery != null ? partQuery + " AND " + String.format(SQL_IN_ROLE, StringUtils.join(inRoleIds, ",")) : String.format(SQL_IN_ROLE, StringUtils.join(inRoleIds, ","));
        }
        List<UserInfo> usersInfo = null;
        try (Connection connection = _dataSource.getConnection()) {
            String limit = "";
            if (from >= 0 && size >= 0) {
                limit = " LIMIT " + from + "," + size;
            }
            String query = String.format(SQL_SEARCH_ALL_USER_INFO_WITH_HIGHEST_ROLE_FILTER, partQuery == null ? "" : " AND " + partQuery, limit);
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, "%" + searchUsername + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs != null) {
                        usersInfo = new ArrayList<>();
                        Map<String, Integer> storeUserIdx = new HashMap<>();
                        while (rs.next()) {
                            String username = rs.getString("username");
                            if (storeUserIdx.containsKey(username)) {
                                if (rs.getObject("role_id") != null) {
                                    RoleInfo role = new RoleInfo(rs.getInt("role_id"), rs.getString("role_name"), rs.getLong("expire_time"));
                                    usersInfo.get(storeUserIdx.get(username)).addRole(role);
                                }
                            } else {
                                UserInfo userInfo = new UserInfo(username, rs.getBoolean("active"), rs.getLong("create_time"));

                                if (rs.getObject("role_id") != null) {
                                    RoleInfo role = new RoleInfo(rs.getInt("role_id"), rs.getString("role_name"), rs.getLong("expire_time"));
                                    userInfo.addRole(role);
                                }
                                storeUserIdx.put(username, usersInfo.size());
                                usersInfo.add(userInfo);
                            }
                        }
                    }
                }
            }
        }
        return usersInfo;
    }

    @Override
    public int countUserRoleInfoWithHighestRoleFilter(String searchUsername, List<Integer> notInRoleIds, List<Integer> inRoleIds) throws Exception {
        String partQuery = null;
        if (notInRoleIds != null && notInRoleIds.isEmpty() == false) {
            partQuery = String.format(SQL_NOT_IN_ROLE, StringUtils.join(notInRoleIds, ","));
        }
        if (inRoleIds != null && inRoleIds.isEmpty() == false) {
            partQuery = partQuery != null ? partQuery + " AND " + String.format(SQL_IN_ROLE, StringUtils.join(inRoleIds, ",")) : String.format(SQL_IN_ROLE, StringUtils.join(inRoleIds, ","));
        }
        int total = -1;
        try (Connection connection = _dataSource.getConnection()) {
            String query = String.format(SQL_COUNT_SEARCH_ALL_USER_INFO_WITH_HIGHEST_ROLE_FILTER, partQuery == null ? "" : " AND " + partQuery);
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, "%" + searchUsername + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs != null && rs.next()) {
                        total = rs.getInt(1);
                    }
                }
            }
        }
        return total;
    }

    @Override
    public boolean isPassword(String username, String password) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement stm = connection.prepareStatement(SQL_IS_PASSWORD)) {
                stm.setString(1, username);
                stm.setString(2, password);
                try (ResultSet rs = stm.executeQuery()) {
                    return rs != null && rs.next();
                }
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Update Section">

    @Override
    public void resetPasswordUser(String username, String password) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_RESET_USER_PASS)) {
                prepareStatement.setString(1, password);
                prepareStatement.setString(2, username);
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void activeUser(String username, boolean isActive) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_ACTIVE_USER)) {
                prepareStatement.setBoolean(1, isActive);
                prepareStatement.setString(2, username);
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void updatePasswordUser(String username, String oldPass, String newPass) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_UPDATE_USER_PASS)) {
                prepareStatement.setString(1, newPass);
                prepareStatement.setString(2, username);
                prepareStatement.setString(3, oldPass);
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void replaceUserPermission(String username, Set<String> oldPermissions, Set<String> newPermissions) throws Exception {
        if (oldPermissions == null || oldPermissions.isEmpty()) {
            insertUserPermission(username, newPermissions);
            return;
        }
        if (newPermissions == null || newPermissions.isEmpty()) {
            deleteUserPermission(username, oldPermissions);
            return;
        }
        try (Connection connection = _dataSource.getConnection()) {
            // delete
            StringBuilder subQuery = new StringBuilder();
            for (int i = 1; i < oldPermissions.size(); i++) {
                subQuery.append(SQL_USER_PER_FIELD);
            }
            try (PreparedStatement prepareStatement = connection.prepareStatement(String.format(SQL_DELETE_USER_PERM_LIST, subQuery.toString()))) {
                int i = 0;
                for (String permission : oldPermissions) {
                    prepareStatement.setString(i * SQL_USER_PER_NUMFIELD + 1, permission);
                    prepareStatement.setString(i * SQL_USER_PER_NUMFIELD + 2, username);
                    i++;
                }
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
            // add
            StringBuilder query = new StringBuilder(SQL_INSERT_USER_PER);
            for (int i = 1; i < newPermissions.size(); i++) {
                query.append(SQL_USER_PER_FIELD);
            }
            query.append(SQL_UPDATE_DUPLICATE_USER_PER);
            try (PreparedStatement prepareStatement = connection.prepareStatement(query.toString())) {
                int i = 0;
                for (String permission : newPermissions) {
                    prepareStatement.setString(i * SQL_USER_PER_NUMFIELD + 1, permission);
                    prepareStatement.setString(i * SQL_USER_PER_NUMFIELD + 2, username);
                    i++;
                }
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void replaceUserRole(String username, Set<Integer> oldRoleIds, Map<Integer, Long> newRoleIds) throws Exception {
        if (oldRoleIds == null || oldRoleIds.isEmpty()) {
            insertUserRole(username, newRoleIds);
            return;
        }
        if (newRoleIds == null || newRoleIds.isEmpty()) {
            deleteUserRole(username, oldRoleIds);
            return;
        }
        try (Connection connection = _dataSource.getConnection()) {
            // delete
            StringBuilder subQuery = new StringBuilder();
            for (int i = 1; i < oldRoleIds.size(); i++) {
                subQuery.append(SQL_USER_ROLE_FIELD);
            }
            try (PreparedStatement prepareStatement = connection.prepareStatement(String.format(SQL_DELETE_USER_ROLE_LIST, subQuery.toString()))) {
                int i = 0;
                for (Integer roleId : oldRoleIds) {
                    prepareStatement.setInt(i * SQL_USER_ROLE_NUMFIELD + 1, roleId);
                    prepareStatement.setString(i * SQL_USER_ROLE_NUMFIELD + 2, username);
                    i++;
                }
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
            // add
            StringBuilder query = new StringBuilder(SQL_INSER_USER_ROLE);
            for (int i = 1; i < newRoleIds.size(); i++) {
                query.append(SQL_USER_ROLE_FIELD);
            }
            query.append(SQL_UPDATE_DUPLICATE_USER_ROLE);
            try (PreparedStatement prepareStatement = connection.prepareStatement(query.toString())) {
                int i = 0;
                for (Map.Entry<Integer, Long> tuple : newRoleIds.entrySet()) {
                    prepareStatement.setInt(i * SQL_USER_ROLE_NUMFIELD + 1, tuple.getKey());
                    prepareStatement.setString(i * SQL_USER_ROLE_NUMFIELD + 2, username);
                    prepareStatement.setLong(i * SQL_USER_ROLE_NUMFIELD + 3, tuple.getValue());
                    i++;
                }
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Delete Section">
    @Override
    public void deleteUser(String username) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_DELETE_USER)) {
                prepareStatement.setString(1, username);
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void deleteUserPermission(String username, String permission) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            String query = String.format(SQL_DELETE_USER_PERM_LIST, "");
            try (PreparedStatement prepareStatement = connection.prepareStatement(query)) {
                prepareStatement.setString(1, permission);
                prepareStatement.setString(2, username);
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void deleteUserPermission(String username, Set<String> permissions) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            StringBuilder subQuery = new StringBuilder();
            for (int i = 1; i < permissions.size(); i++) {
                subQuery.append(SQL_USER_PER_FIELD);
            }
            String query = String.format(SQL_DELETE_USER_PERM_LIST, subQuery.toString());
            try (PreparedStatement prepareStatement = connection.prepareStatement(query)) {
                int i = 0;
                for (String permission : permissions) {
                    prepareStatement.setString(i * SQL_USER_PER_NUMFIELD + 1, permission);
                    prepareStatement.setString(i * SQL_USER_PER_NUMFIELD + 2, username);
                    i++;
                }
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void deleteUserRole(String username, int roleId) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            String query = String.format(SQL_DELETE_USER_ROLE_LIST, "");
            try (PreparedStatement prepareStatement = connection.prepareStatement(query)) {
                prepareStatement.setInt(1, roleId);
                prepareStatement.setString(2, username);
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void deleteUserRole(String username, Set<Integer> roleIds) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            StringBuilder subQuery = new StringBuilder();
            for (int i = 1; i < roleIds.size(); i++) {
                subQuery.append(SQL_USER_ROLE_FIELD);
            }
            String query = String.format(SQL_DELETE_USER_ROLE_LIST, subQuery.toString());
            try (PreparedStatement prepareStatement = connection.prepareStatement(query)) {
                int i = 0;
                for (Integer roleId : roleIds) {
                    prepareStatement.setInt(i * SQL_USER_ROLE_NUMFIELD + 1, roleId);
                    prepareStatement.setString(i * SQL_USER_ROLE_NUMFIELD + 2, username);
                    i++;
                }
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }
    // </editor-fold>
}
