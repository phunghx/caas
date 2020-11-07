package xed.caas.services

import java.util

import com.twitter.inject.Test
import org.apache.commons.lang3.SerializationUtils
import org.apache.shiro.session.mgt.SimpleSession
import redis.clients.jedis.JedisPool
import xed.caas.controller.Tools.providesRedisPool
import xed.caas.repository.RedisSimpleSessionDAO
import xed.caas.util.ZConfig
import xed.jcaas.common.DataSourceBuilder
import xed.jcaas.core.CAAS
import xed.jcaas.model.dal.{RoleDAOImpl, UserDAOImpl}
import xed.jcaas.model.entity.RoleInfo

import scala.collection.JavaConverters._

/**
 * @author sonpn
 */
class CaasTest extends Test {

  val ds = DataSourceBuilder.buildMySQLDataSource(
    ZConfig.getString("authen_mysql.host"),
    ZConfig.getInt("authen_mysql.port"),
    ZConfig.getString("authen_mysql.dbname"),
    ZConfig.getString("authen_mysql.username"),
    ZConfig.getString("authen_mysql.password"))


  val jedisPool = {
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
  val sessionDAO =  RedisSimpleSessionDAO(
    providesRedisPool(),
    serializer= (session)  => SerializationUtils.serialize(session.asInstanceOf[SimpleSession]),
    deserializer = (data: Array[Byte]) => SerializationUtils.deserialize[SimpleSession](data)
  )


  val caas = new CAAS(ds, sessionDAO)
  val userDAO = new UserDAOImpl(ds)
  val roleDAO = new RoleDAOImpl(ds)

  val username: String = "tvc12"
  val pass: String = "123456"
  var ssid: String = _
  var ssidOAuth: String = _

  val rolesDefault =
    Map(
      12122018 -> "vip",
      12122019 -> "super_vip",
      12122020 -> "super_super_vip"
    )

  val rolesPermissionDefault = Seq(
    12122018 -> "upload:photo",
    12122018 -> "upload:audio",
    12122019 -> "upload:video",
    12122019 -> "upload:photo",
    12122020 -> "upload:*"
  )

  val rolesAndPermissions = rolesPermissionDefault
    .groupBy(_._1)
    .map(tuple => tuple._1 -> tuple._2.map(_._2))

  val userPermissionDefault = Seq(username -> "read", username -> "write")

  var currentTime: Long = _

  override def beforeEach(): Unit = {
    super.beforeEach()
    clearData()
    println("Run init script")
    val roles: util.List[Integer] = rolesDefault.keys.map(int2Integer).toList.asJava
    val roleNames: util.List[String] = rolesDefault.values.toList.asJava

    userDAO.insertUser(username, pass, true)
    roleDAO.insertRole(roles, roleNames)

    for (tuple <- rolesAndPermissions) {
      val permissions: util.Set[String] = tuple._2.toSet.asJava
      roleDAO.addRolePermission(int2Integer(tuple._1), permissions)
    }
    for (tuple <- userPermissionDefault) {
      userDAO.insertUserPermission(username, tuple._2)
    }
    currentTime = System.currentTimeMillis()
    userDAO.insertUserRole(username, 12122018, currentTime + 15 * 1000)
    userDAO.insertUserRole(username, 12122019, Long.MaxValue)

    println("Completed init data")

    ssid = caas.login(username, pass, true, 100000)
    println(s"ssid: $ssid")
  }

  def clearData(): Unit = {
    for (tuple <- userPermissionDefault) {
      userDAO.deleteUserPermission(username, tuple._2)
    }

    userDAO.deleteUserRole(username, 12122018)
    userDAO.deleteUserRole(username, 12122019)

    for (tuple <- rolesAndPermissions) {
      val permissions: util.Set[String] = tuple._2.toSet.asJava
      roleDAO.deleteRolePermission(int2Integer(tuple._1), permissions)
    }

    for (elem <- rolesDefault) {
      roleDAO.deleteRole(elem._1)
    }

    userDAO.deleteUser(username)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    caas.logout(ssid)

    clearData()
  }

  test("get username") {
    val user = caas.getUser(ssid)
    println(s"Username: $user")
    assertResult(user)(username)
  }
//
  test("check role") {
    assertResult(true)(caas.hasRole(ssid, "vip"))
    assertResult(true)(caas.hasRole(ssid, "super_vip"))

    assertResult(false)(caas.hasRole(ssid, "super_super_vip"))

    userDAO.deleteUserRole(username, 12122018) // delete user vip
    assertResult(false)(caas.hasRole(ssid, "vip"))
  }

  test("check permission") {
    assertResult(true)(caas.isPermitted(ssid, "upload:photo"))
    assertResult(true)(caas.isPermitted(ssid, "upload:video"))
    assertResult(true)(caas.isPermitted(ssid, "upload:audio"))

    assertResult(true)(caas.isPermitted(ssid, "read"))
    assertResult(true)(caas.isPermitted(ssid, "write"))
    assertResult(false)(caas.isPermitted(ssid, "upload:*"))

    userDAO.deleteUserPermission(username, "write")
    userDAO.deleteUserRole(username, 12122018) // delete user vip

    assertResult(false)(caas.isPermitted(ssid, "write"))
    assertResult(false)(caas.isPermitted(ssid, "upload:audio"))
  }

  test("Get all user") {
    val users = userDAO.getAllUsername().asScala
    println(s"Users: ${users.size}")
    assertResult(true)(users.nonEmpty)
  }

  test("Get user info") {
    val user = userDAO.getUserInfo(username)
    println(s"User: $user")
    assertResult(true)(user != null)
    val permissions = user.getPermissions.asScala
    val roleInfos = user.getRoles.asScala
    assertResult(true)(permissions.nonEmpty)
    assertResult(true)(roleInfos.nonEmpty)
    for (roleInfo <- roleInfos) {
      val expectRole = rolesDefault.get(roleInfo.getId)

      assertResult(true)(expectRole.isDefined)
      assertResult(expectRole.get)(roleInfo.getName)
      println(s"Expire Time: ${roleInfo.getExpireTime}")
    }
  }

}
