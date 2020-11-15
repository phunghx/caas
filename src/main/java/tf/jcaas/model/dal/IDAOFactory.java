package tf.jcaas.model.dal;

import tf.jcaas.core.CAAS;

public interface IDAOFactory {

    public IUserDAO getUserDAO();

    public IRoleDAO getRoleDAO();

    public CAAS getCaas();
}
