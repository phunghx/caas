package tf.jcaas.model.dal;

import tf.jcaas.model.entity.RoleInfo;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoleDAOImpl implements IRoleDAO {

    protected final DataSource _dataSource;
    protected static final Logger _logger = Logger.getLogger(RoleDAOImpl.class);
    
    // <editor-fold defaultstate="collapsed" desc="SQL Query DB Section">
    private final String SQL_INSERT_ROLE = "INSERT INTO role (role_id, role_name) VALUES (?, ?)";
    private final String SQL_UPDATE_ROLE = "UPDATE role SET role_name=? WHERE role_id=?";
    private final String SQL_ROLE_FIELD = ",(?, ?) ";
    private final int SQL_ROLE_NUMFIELD = 2;
    private final String SQL_DELETE_ROLE = "DELETE FROM role WHERE role_id=?";
    private final String SQL_SELECT_ROLE = "SELECT role_id, role_name FROM role WHERE role_id=?";
    private final String SQL_SELECT_ALL_ROLE = "SELECT role_id, role_name FROM role";

    private final String SQL_INSERT_ROLE_PERM = "INSERT INTO role_permissions (role_id, permission) VALUES (?, ?)";
    private final String SQL_UPDATE_DUPLICATE_ROLE_PERM = " ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), permission=VALUES(permission) ";
    private final String SQL_ROLE_PERM_FIELD = ",(?, ?)";
    private final int SQL_ROLE_PERM_NUMFIELD = 2;
    private final String SQL_DELETE_ROLE_PERM_LIST = "DELETE FROM role_permissions WHERE (role_id, permission) IN ( (?, ?) %s )";

    private final String SQL_SELECT_ROLE_PERM_SET = "SELECT role_id, permission FROM role_permissions WHERE role_id=?";

    // </editor-fold>

    public RoleDAOImpl(DataSource dataSource) {
        this._dataSource = dataSource;
    }

    // <editor-fold defaultstate="collapsed" desc="Insert Section">
    @Override
    public void insertRole(int roleId, String roleName) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_INSERT_ROLE)) {
                prepareStatement.setInt(1, roleId);
                prepareStatement.setString(2, roleName);
                if (prepareStatement.executeUpdate() == 0) {
                    throw new SQLException("Not register role.");
                }
            }

        }
    }
    @Override
    public void insertRole(List<Integer> roleIds, List<String> roleNames) throws Exception {
        if (roleIds == null || roleNames == null) {
            throw new NullPointerException();
        }
        if (roleIds.size() != roleNames.size()) {
            throw new Exception("Size roleIds not equal size roleNames");
        }
        try (Connection connection = _dataSource.getConnection()) {
            StringBuilder query = new StringBuilder(SQL_INSERT_ROLE);
            for (int i = 1; i < roleIds.size(); i++) {
                query.append(SQL_ROLE_FIELD);
            }
            try (PreparedStatement prepareStatement = connection.prepareStatement(query.toString())) {
                for (int i = 0; i < roleIds.size(); i++) {
                    prepareStatement.setInt(i * SQL_ROLE_NUMFIELD + 1, roleIds.get(i));
                    prepareStatement.setString(i * SQL_ROLE_NUMFIELD + 2, roleNames.get(i));
                }
                if (prepareStatement.executeUpdate() == 0) {
                    throw new SQLException("Not register role.");
                }
            }

        }
    }

    @Override
    public void insertRoleWithPermission(int roleId, String roleName, Set<String> permissions) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_INSERT_ROLE)) {
                prepareStatement.setInt(1, roleId);
                prepareStatement.setString(2, roleName);
                if (prepareStatement.executeUpdate() == 0) {
                    connection.rollback();
                    throw new SQLException("Not register role.");
                }
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
            if (permissions == null || permissions.isEmpty()) {
                connection.commit();
            } else {
                StringBuilder query = new StringBuilder(SQL_INSERT_ROLE_PERM);
                for (int i = 1; i < permissions.size(); i++) {
                    query.append(SQL_ROLE_PERM_FIELD);
                }
                query.append(SQL_UPDATE_DUPLICATE_ROLE_PERM);
                try (PreparedStatement prepareStatement = connection.prepareStatement(query.toString())) {
                    int i = 0;
                    for (String permission : permissions) {
                        prepareStatement.setInt(i * SQL_ROLE_PERM_NUMFIELD + 1, roleId);
                        prepareStatement.setString(i * SQL_ROLE_PERM_NUMFIELD + 2, permission);
                        i++;
                    }
                    if (prepareStatement.executeUpdate() != 0) {
                        connection.commit();
                    } else {
                        connection.rollback();
                        throw new SQLException("insert permission fail");
                    }
                } catch (Exception ex) {
                    connection.rollback();
                    throw ex;
                }
            }
        }
    }

    @Override
    public void addRolePermission(int roleId, String permission) throws Exception {
        if (permission == null) {
            return;
        }
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_INSERT_ROLE_PERM + SQL_UPDATE_DUPLICATE_ROLE_PERM)) {
                prepareStatement.setInt(1, roleId);
                prepareStatement.setString(2, permission);
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void addRolePermission(int roleId, Set<String> permissions) throws Exception {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        try (Connection connection = _dataSource.getConnection()) {
            StringBuilder query = new StringBuilder(SQL_INSERT_ROLE_PERM);
            for (int i = 1; i < permissions.size(); i++) {
                query.append(SQL_ROLE_PERM_FIELD);
            }
            query.append(SQL_UPDATE_DUPLICATE_ROLE_PERM);
            try (PreparedStatement prepareStatement = connection.prepareStatement(query.toString())) {
                int i = 0;
                for (String permission : permissions) {
                    prepareStatement.setInt(i * SQL_ROLE_PERM_NUMFIELD + 1, roleId);
                    prepareStatement.setString(i * SQL_ROLE_PERM_NUMFIELD + 2, permission);
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
    public List<RoleInfo> getAllRole() throws Exception {
        List<RoleInfo> roles = null;
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_SELECT_ALL_ROLE)) {
                try (ResultSet rs = prepareStatement.executeQuery()) {
                    roles = new ArrayList<>();
                    while (rs != null && rs.next()) {
                        RoleInfo role = new RoleInfo(rs.getInt("role_id"), rs.getString("role_name"));
                        roles.add(role);
                    }
                }
            }
        }
        return roles;
    }

    @Override
    public RoleInfo getRoleInfo(int roleId) throws Exception {
        RoleInfo role = null;
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_SELECT_ROLE)) {
                prepareStatement.setInt(1, roleId);
                try (ResultSet rs = prepareStatement.executeQuery()) {
                    if (rs.next()) {
                        role = new RoleInfo();
                        role.setId(rs.getInt("role_id"));
                        role.setName(rs.getString("role_name"));
                        try (PreparedStatement _prepareStatement = connection.prepareStatement(SQL_SELECT_ROLE_PERM_SET)) {
                            _prepareStatement.setInt(1, roleId);
                            try (ResultSet _rs = _prepareStatement.executeQuery()) {
                                Set<String> permissions = new HashSet<>();
                                while (_rs.next()) {
                                    permissions.add(_rs.getString("permission"));
                                }
                                role.setPermissions(permissions);
                            }
                        }
                    }
                }
            }

        }
        return role;
    }

    @Override
    public List<RoleInfo> getAllRoleWithPermissions() throws Exception {
        List<RoleInfo> roles = null;
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_SELECT_ALL_ROLE)) {
                try (ResultSet rs = prepareStatement.executeQuery()) {
                    roles = new ArrayList<>();
                    while (rs.next()) {
                        RoleInfo role = new RoleInfo();
                        role.setId(rs.getInt("role_id"));
                        role.setName(rs.getString("role_name"));
                        try (PreparedStatement _prepareStatement = connection.prepareStatement(SQL_SELECT_ROLE_PERM_SET)) {
                            _prepareStatement.setInt(1, role.getId());
                            try (ResultSet _rs = _prepareStatement.executeQuery()) {
                                Set<String> permissions = new HashSet<>();
                                while (_rs.next()) {
                                    permissions.add(_rs.getString("permission"));
                                }
                                role.setPermissions(permissions);
                            }
                        }
                        roles.add(role);
                    }
                }
            }
        }
        return roles;
    }
	// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Delete Section">
    @Override
    public void deleteRole(int roleId) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_DELETE_ROLE)) {
                prepareStatement.setInt(1, roleId);
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void deleteRolePermission(int roleId, String permission) throws Exception {
        if (permission == null) {
            return;
        }
        try (Connection connection = _dataSource.getConnection()) {
            String query = String.format(SQL_DELETE_ROLE_PERM_LIST, "");
            try (PreparedStatement prepareStatement = connection.prepareStatement(query)) {
                prepareStatement.setInt(1, roleId);
                prepareStatement.setString(2, permission);
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void deleteRolePermission(int roleId, Set<String> permissions) throws Exception {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        try (Connection connection = _dataSource.getConnection()) {
            StringBuilder subQuery = new StringBuilder();
            for (int i = 1; i < permissions.size(); i++) {
                subQuery.append(SQL_ROLE_PERM_FIELD);
            }
            String query = String.format(SQL_DELETE_ROLE_PERM_LIST, subQuery.toString());
            try (PreparedStatement prepareStatement = connection.prepareStatement(query)) {
                int i = 0;
                for (String permission : permissions) {
                    prepareStatement.setInt(i * SQL_ROLE_PERM_NUMFIELD + 1, roleId);
                    prepareStatement.setString(i * SQL_ROLE_PERM_NUMFIELD + 2, permission);
                    i++;
                }
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }
	// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Update Section">
    @Override
    public void updateRoleName(int roleId, String newRoleName) throws Exception {
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement prepareStatement = connection.prepareStatement(SQL_UPDATE_ROLE)) {
                prepareStatement.setString(1, newRoleName);
                prepareStatement.setInt(2, roleId);
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
        }
    }

    @Override
    public void replaceRolePermission(int roleId, Set<String> oldPermissions, Set<String> newPermissions) throws Exception {
        if (oldPermissions == null || oldPermissions.isEmpty()) {
            addRolePermission(roleId, newPermissions);
            return;
        }
        if (newPermissions == null || newPermissions.isEmpty()) {
            deleteRolePermission(roleId, oldPermissions);
            return;
        }
        try (Connection connection = _dataSource.getConnection()) {
            // delete
            StringBuilder subQuery = new StringBuilder();
            for (int i = 1; i < oldPermissions.size(); i++) {
                subQuery.append(SQL_ROLE_PERM_FIELD);
            }
            try (PreparedStatement prepareStatement = connection.prepareStatement(String.format(SQL_DELETE_ROLE_PERM_LIST, subQuery.toString()))) {
                int i = 0;
                for (String permission : oldPermissions) {
                    prepareStatement.setInt(i * SQL_ROLE_PERM_NUMFIELD + 1, roleId);
                    prepareStatement.setString(i * SQL_ROLE_PERM_NUMFIELD + 2, permission);
                    i++;
                }
                if (prepareStatement.executeUpdate() == 0) {
                    // fail
                }
            }
            // add
            StringBuilder query = new StringBuilder(SQL_INSERT_ROLE_PERM);
            for (int i = 1; i < newPermissions.size(); i++) {
                query.append(SQL_ROLE_PERM_FIELD);
            }
            query.append(SQL_UPDATE_DUPLICATE_ROLE_PERM);
            try (PreparedStatement prepareStatement = connection.prepareStatement(query.toString())) {
                int i = 0;
                for (String permission : newPermissions) {
                    prepareStatement.setInt(i * SQL_ROLE_PERM_NUMFIELD + 1, roleId);
                    prepareStatement.setString(i * SQL_ROLE_PERM_NUMFIELD + 2, permission);
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
