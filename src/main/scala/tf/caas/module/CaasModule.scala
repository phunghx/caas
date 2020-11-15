package tf.caas.module

import javax.inject.Singleton
import javax.sql.DataSource
import com.google.inject.Provides
import xed.caas.service.CaasServiceImpl
import com.twitter.inject.TwitterModule
import org.apache.commons.lang3.SerializationUtils
import org.apache.shiro.session.Session
import org.apache.shiro.session.mgt.SimpleSession
import org.apache.shiro.session.mgt.eis.SessionDAO
import redis.clients.jedis.JedisPool
import tf.caas.repository.RedisSimpleSessionDAO
import tf.caas.service.{CaasService, CaasServiceImpl}
import tf.caas.util.ZConfig
import tf.jcaas.common.DataSourceBuilder
import tf.jcaas.core.CAAS
import tf.jcaas.model.dal.{IRoleDAO, IUserDAO, RoleDAOImpl, UserDAOImpl}
import xed.jcaas.model.dal.RoleDAOImpl


object CaasModule extends TwitterModule {


  @Singleton
  @Provides
  def providesRedisPool(): JedisPool = {
    import redis.clients.jedis.JedisPoolConfig

    val host = ZConfig.getString("redis.host")
    val port = ZConfig.getInt("redis.port")
    val authPass = ZConfig.getString("redis.auth_pass", null)
    val timeout = ZConfig.getInt("redis.timeout", 15)
    val maxTimeoutInMillis = ZConfig.getInt("redis.max_timeout_millis", 60000)

    val poolConfig = new JedisPoolConfig
    poolConfig.setMaxWaitMillis(maxTimeoutInMillis)
    poolConfig.setMaxTotal(16)
    poolConfig.setTestWhileIdle(true)
    new JedisPool(
      poolConfig,
      host,
      port,
      timeout,
      authPass
    )
  }

  @Singleton
  @Provides
  def providesDataSource(): DataSource = {
    val dbName: String = ZConfig.getString("authen_mysql.dbname")
    val host: String = ZConfig.getString("authen_mysql.host")
    val port: Int = ZConfig.getInt("authen_mysql.port")
    val username: String = ZConfig.getString("authen_mysql.username")
    val password: String = ZConfig.getString("authen_mysql.password")
    DataSourceBuilder.buildMySQLDataSource(host, port, dbName, username, password)
  }



  @Singleton
  @Provides
  def providesCAAS(dataSource: DataSource, sessionDAO: SessionDAO): CAAS =  {
    new CAAS(dataSource, sessionDAO)
  }

  @Singleton
  @Provides
  def providesSessionDAO(jedisPool: JedisPool): SessionDAO = {
    RedisSimpleSessionDAO(
      jedisPool,
      serializer= (session)  => SerializationUtils.serialize(session.asInstanceOf[SimpleSession]),
      deserializer = (data: Array[Byte]) => SerializationUtils.deserialize[SimpleSession](data)
    )
  }

  @Singleton
  @Provides
  def providesUserDAO(dataSource: DataSource): IUserDAO = new UserDAOImpl(dataSource)

  @Singleton
  @Provides
  def providesRoleDAO(dataSource: DataSource): IRoleDAO = new RoleDAOImpl(dataSource)

  @Singleton
  @Provides
  def providesCaasService(cAAS: CAAS, userDAO: IUserDAO, roleDAO: IRoleDAO): CaasService = new CaasServiceImpl(cAAS, userDAO, roleDAO)

}
