package xed.caas.controller

import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.finatra.thrift.ThriftClient
import com.twitter.inject.server.FeatureTest
import com.twitter.util.Future
import xed.caas.Server
import xed.caas.domain.thrift.{TRoleInfo, TUserAuthResult}
import xed.caas.service.TCaasService

/**
 * Created by SangDang on 9/18/16.
 */
class CaasControllerTest extends FeatureTest {
  override protected val server = new EmbeddedHttpServer(
    twitterServer = new Server
  ) with ThriftClient

  lazy val client = server.thriftClient[TCaasService[Future]](clientId = "1")
  val username = "meomeo@x.education"
  val password = "123456"
  var ssid: String = _

  def deleteOldData(): Unit = {
    client.deleteUser(username)
  }

  override def beforeAll(): Unit = {
    deleteOldData()
    client.register(username, password)
  }

  test("[Thrift] ping") {
    assertResult("pong")(client.ping().value)
  }

  test("[Thrift] loginOAuth") {
    val result: TUserAuthResult = client.login(username, password).value
    println(result)
    assertResult(true)(result != null)
    assertResult(0)(result.code)
    assertResult(true)(result.userAuthInfo.isDefined)
    ssid = result.userAuthInfo.get.ssid

  }

  test("Get user info with session id") {
    val r = client.getUserWithSessionId(ssid).value
    println(r)
    assertResult(true)(r != null)
    assertResult(true)(r.userInfo.isDefined)
  }

  test("Insert user roles") {
    val r = client
      .insertExpirableUserRoles(
        username,
        Map[Int, Long](10 -> System.currentTimeMillis())
      )
      .value
    assertResult(true)(r != null)
    assertResult(0)(r.code)
    assertResult(true)(r.data.getOrElse(false))
  }

  test("Get user roles") {
    val r = client.getUserRoles(ssid).value
    println(r)
    assertResult(true)(r != null)
    assertResult(0)(r.code)
    assertResult(true)(r.data.getOrElse(Seq.empty).nonEmpty)
  }

  test("Get user permission") {
    val r = client.isPermitted(ssid, "*:*").value
    println(r)
    assertResult(true)(r != null)
    assertResult(0)(r.code)
    assertResult(true)(r.data.getOrElse(false))
  }

  test("Remove roles") {
    val r = client.deleteUserRoles(username, Set(10)).value
    println(r)
    assertResult(true)(r != null)
    assertResult(0)(r.code)
    assertResult(true)(r.data.getOrElse(false))
  }

  test("Get user permission with permission not exists") {
    val r = client.isPermitted(ssid, "write").value
    println(r)
    assertResult(true)(r != null)
    assertResult(0)(r.code)
    assertResult(false)(r.data.getOrElse(false))
  }

  test("Clear expire roles") {
    val r = client
      .insertExpirableUserRoles(
        username,
        Map[Int, Long](
          10 -> System.currentTimeMillis(),
          11 -> (System.currentTimeMillis() + 15 * 1000)
        )
      )
      .value
    println(s"Insert roles: $r")
    assertResult(true)(r.data.getOrElse(false))
    val rs = client.deleteAllExpiredUserRole(11, Long.MaxValue).value
    println(s"Result deleteAllExpiredUserRole:: $rs")
    assertResult(true)(rs != null)
    assertResult(true)(rs.data.getOrElse(false))
    val roleInfos = client.getAllRoleInfo(username).value
    assertResult(true)(roleInfos.roles.getOrElse(Seq.empty).nonEmpty)
    for (roleInfo <- roleInfos.roles.get) {
      println(roleInfo)
      assertResult(TRoleInfo(11, "admin", expireTime = Some(Long.MaxValue)))(roleInfo)
    }
  }
}
