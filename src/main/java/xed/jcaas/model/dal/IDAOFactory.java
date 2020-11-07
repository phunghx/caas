package xed.jcaas.model.dal;

import xed.jcaas.core.CAAS;

public interface IDAOFactory {

    public IUserDAO getUserDAO();

    public IRoleDAO getRoleDAO();

    public CAAS getCaas();
}
