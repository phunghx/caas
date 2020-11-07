package xed.caas.service

import java.{lang, util}

import javax.inject.Inject
import xed.caas.domain.Implicits._
import xed.caas.domain.UserInfoPageable
import xed.caas.domain.thrift.ThriftImplicit._
import xed.caas.domain.thrift.{Constants, TUserAuthInfo, TUserInfo}
import xed.caas.repository._
import xed.caas.util.ZConfig
import xed.jcaas.core.CAAS
import xed.jcaas.model.dal.{IRoleDAO, IUserDAO}
import xed.jcaas.model.entity.{Pageable, RoleInfo}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters.{
  iterableAsScalaIterableConverter,
  mapAsJavaMapConverter
}

/**
 * @author sonpn
 */

trait CaasService {

  def renewSession(oldSessionId: String, ssTimeout: Option[Long]): TUserAuthInfo

  def registerUser(username: String, password: String): TUserInfo

  def registerUserWithOAuth(oauthType: String,
                            id: String,
                            token: String,
                            password: Option[String]): TUserInfo

  def loginWithOAuth(oauthType: String,
                     id: String,
                     token: String,
                     ssTimeout: Option[Long],
                     password: Option[String]): TUserAuthInfo

  def login(username: String,
            password: String,
            ssTimeout: Option[Long]): TUserAuthInfo

  def loginOAuth(username: String, ssTimeout: Option[Long]): TUserAuthInfo

  def logout(sessionId: String)

  def getUserWithSessionId(sessionId: String): TUserInfo

  def getUserWithUsername(username: String): TUserInfo

  def isPermittedsUser(username: String, permissions: Seq[String]): Seq[Boolean]

  def isPermittedUserAll(username: String, permissions: Seq[String]): Boolean

  def isPermitted(sessionId: String, permission: String): Boolean

  def isPermittedUser(username: String, permission: String): Boolean

  def isPermittedAll(sessionId: String, permissions: Seq[String]): Boolean

  def isPermitteds(sessionId: String, permissions: Seq[String]): Seq[Boolean]

  def getUserRoles(sessionId: String): Seq[String]

  def hasRole(sessionId: String, role: String): Boolean

  def hasRoles(sessionId: String, roles: Seq[String]): Seq[Boolean]

  def hasRoleUser(username: String, roleName: String): Boolean

  def hasAllRoleUser(username: String, roleName: Seq[String]): Boolean

  def getAllUsername(): Seq[String]

  def getActiveUsername(from: Int, size: Int): Pageable[String]

  def insertUserRoles(username: String, roleIds: Map[Int, Long]): Unit

  def deleteUserRoles(username: String, roleIds: Set[Int]): Unit

  def deleteUser(username: String): Unit

  def getListUserRole(notInRoleIds: Option[Seq[Int]],
                      inRoleIds: Option[Seq[Int]],
                      from: Int,
                      size: Int): UserInfoPageable

  def searchListUserRole(usernameSearchKey: String,
                         notInRoleIds: Option[Seq[Int]],
                         inRoleIds: Option[Seq[Int]],
                         from: Int,
                         size: Int): UserInfoPageable

  def resetPasswordUser(username: String, newPassword: String): Unit

  def updatePasswordUser(username: String,
                         oldPassword: String,
                         newPassword: String): Unit

  def isCredentialDefault(oauthType: String, username: String): Boolean

  def deleteAllExpiredUserRole(defaultRole: Int, maxTime: Long): Boolean

  def getUserRoleInfos(username: String): Seq[RoleInfo]

  def getAllPermission(username: String): Seq[String]

  def insertUserRole(username: String, role: Int, expireTime: Long, force: Boolean): Unit
}

case class CaasServiceImpl @Inject()(caas: CAAS,
                                     userDAO: IUserDAO,
                                     roleDAO: IRoleDAO)
    extends CaasService {
  val sessionTimeout: Long =
    ZConfig.getLong("session.timeout", 30L * 86400L * 1000L)

  override def renewSession(oldSessionId: String,
                            ssTimeout: Option[Long]): TUserAuthInfo = {
    val ssId = caas.renewSession(oldSessionId, ssTimeout match {
      case Some(x) => x
      case _       => sessionTimeout
    })
    TUserAuthInfo(ssId, userDAO.getUserInfo(caas.getUser(ssId)))
  }

  override def registerUser(username: String, password: String): TUserInfo = {
    if (userDAO.isExistUser(username)) {
      throw new Exception("User [" + username + "] already exist.");
    }
    userDAO.insertUser(username, password, true)
  }

  override def registerUserWithOAuth(oauthType: String,
                                     id: String,
                                     token: String,
                                     password: Option[String]): TUserInfo = {
    val oauthInfo = getOAuthInfo(oauthType, id, token)
    if (!userDAO.isExistUser(oauthInfo.getId)) {
      userDAO.insertUser(
        oauthInfo.getId,
        password.getOrElse(oauthInfo.getPass),
        true
      )
    }
    userDAO.getUserInfo(oauthInfo.getId)
  }

  override def loginWithOAuth(oauthType: String,
                              id: String,
                              token: String,
                              ssTimeout: Option[Long],
                              password: Option[String]): TUserAuthInfo = {
    val oauthInfo = getOAuthInfo(oauthType, id, token)
    if (!userDAO.isExistUser(oauthInfo.getId)) {
      userDAO.insertUser(
        oauthInfo.getId,
        password.getOrElse(oauthInfo.getPass),
        true
      )
    }
    val ssId: String = caas.loginOAuth(oauthInfo.getId, true, ssTimeout match {
      case Some(x) => x
      case _       => sessionTimeout
    })
    TUserAuthInfo(ssId, userDAO.getUserInfo(oauthInfo.getId))
  }

  private def getOAuthInfo(oauthType: String,
                           id: String,
                           token: String): OAuthRepository = {
    oauthType match {
      case Constants.OAUTH_GOOGLE   => new GoogleOAuthRepository(id, token)
      case Constants.OAUTH_FACEBOOK => new FacebookOAuthRepository(id, token)
      case _                        => throw new UnsupportedOperationException("oauthType invalid")
    }
  }

  override def login(username: String,
                     password: String,
                     ssTimeout: Option[Long]): TUserAuthInfo = {
    if (password == null || password.isEmpty)
      throw new Exception("password is empty")
    val ssId: String = caas.login(username, password, true, ssTimeout match {
      case Some(x) => x
      case _       => sessionTimeout
    })
    TUserAuthInfo(ssId, userDAO.getUserInfo(username))
  }

  def loginOAuth(username: String, ssTimeout: Option[Long]): TUserAuthInfo = {
    val ssId: String = caas.loginOAuth(username, true, ssTimeout match {
      case Some(x) => x
      case _       => sessionTimeout
    })
    TUserAuthInfo(ssId, userDAO.getUserInfo(username))
  }

  override def logout(sessionId: String): Unit = {
    caas.logout(sessionId)
  }

  override def getUserWithSessionId(sessionId: String): TUserInfo = {
    userDAO.getUserInfo(caas.getUser(sessionId))
  }

  override def getUserWithUsername(username: String): TUserInfo = {
    userDAO.getUserInfo(username)
  }

  override def isPermittedsUser(username: String,
                                permissions: Seq[String]): Seq[Boolean] = {
    caas.isPermittedUser(username, permissions.toArray: _*)
  }

  override def isPermitted(sessionId: String, permission: String): Boolean = {
    caas.isPermitted(sessionId, permission)
  }

  override def isPermittedUserAll(username: String,
                                  permissions: Seq[String]): Boolean = {
    caas.isPermittedUserAll(username, permissions.toArray: _*)
  }

  override def isPermittedUser(username: String,
                               permission: String): Boolean = {
    caas.isPermittedUser(username, permission)
  }

  override def isPermittedAll(sessionId: String,
                              permissions: Seq[String]): Boolean = {
    caas.isPermittedAll(sessionId, permissions.toArray: _*)
  }

  override def isPermitteds(sessionId: String,
                            permissions: Seq[String]): Seq[Boolean] = {
    caas.isPermitted(sessionId, permissions.toArray: _*)
  }

  override def getUserRoles(sessionId: String): Seq[String] = {
    val username: String = caas.getUser(sessionId)
    userDAO.getAllRoleOfUser(username).map(_.toString).seq
  }

  override def hasRole(sessionId: String, role: String): Boolean = {
    caas.hasRole(sessionId, role)
  }

  override def hasRoles(sessionId: String, roles: Seq[String]): Seq[Boolean] = {
    caas.hasRoles(sessionId, roles)
  }

  override def hasRoleUser(username: String, roleName: String): Boolean = {
    caas.hasRoleUser(username, roleName)
  }

  override def hasAllRoleUser(username: String, roleNames: Seq[String]): Boolean = {
    caas.hasAllRoleUser(username, roleNames)
  }

  override def getAllUsername(): Seq[String] = {
    userDAO.getAllUsername.asInstanceOf[java.util.List[String]]
  }

  override def insertUserRoles(username: String,
                               roleIds: Map[Int, Long]): Unit = {
    val roles = roleIds.map(tuple => int2Integer(tuple._1) -> long2Long(tuple._2))
    userDAO.insertUserRole(username, roles)
  }

  override def deleteUserRoles(username: String, roleIds: Set[Int]): Unit =
    userDAO.deleteUserRole(username, roleIds)

  override def deleteUser(username: String): Unit =
    userDAO.deleteUser(username)

  override def getListUserRole(notInRoleIds: Option[Seq[Int]] = None,
                               inRoleIds: Option[Seq[Int]] = None,
                               from: Int,
                               size: Int): UserInfoPageable = {
    val cvtNotInRoleIds = notInRoleIds match {
      case Some(x) => x.toList
      case _       => List.empty
    }
    val cvtInRoleIds = inRoleIds match {
      case Some(x) => x.toList
      case _       => List.empty
    }
    val users = userDAO.getListUserRoleInfoWithHighestRoleFilter(
      cvtNotInRoleIds,
      cvtInRoleIds,
      from,
      size
    )
    UserInfoPageable(
      if (users == null) Seq.empty else users.toSeq,
      userDAO
        .countUserRoleInfoWithHighestRoleFilter(cvtNotInRoleIds, cvtInRoleIds)
    )
  }

  override def searchListUserRole(usernameSearchKey: String,
                                  notInRoleIds: Option[Seq[Int]],
                                  inRoleIds: Option[Seq[Int]],
                                  from: Int,
                                  size: Int): UserInfoPageable = {
    val cvtNotInRoleIds = notInRoleIds match {
      case Some(x) => x.toList
      case _       => List.empty
    }
    val cvtInRoleIds = inRoleIds match {
      case Some(x) => x.toList
      case _       => List.empty
    }
    val users = userDAO.searchUserRoleInfoWithHighestRoleFilter(
      usernameSearchKey,
      cvtNotInRoleIds,
      cvtInRoleIds,
      from,
      size
    )
    val total = userDAO.countUserRoleInfoWithHighestRoleFilter(
      usernameSearchKey,
      cvtNotInRoleIds,
      cvtInRoleIds
    )
    UserInfoPageable(if (users == null) Seq.empty else users.toSeq, total)
  }

  override def resetPasswordUser(username: String,
                                 newPassword: String): Unit = {
    userDAO.resetPasswordUser(username, newPassword)
  }

  override def updatePasswordUser(username: String,
                                  oldPassword: String,
                                  newPassword: String): Unit = {
    userDAO.updatePasswordUser(username, oldPassword, newPassword)
  }

  override def isCredentialDefault(oauthType: String,
                                   username: String): Boolean = {
    oauthType match {
      case Constants.OAUTH_GOOGLE =>
        userDAO.isPassword(username, OAuthRepository.generatePassword(username))
      case Constants.OAUTH_FACEBOOK =>
        userDAO.isPassword(username, OAuthRepository.generatePassword(username))
      case _ => false
    }
  }

  override def getActiveUsername(from: Int, size: Int) = {
    userDAO.getUsernameActive(from, size)
  }

  def getExpiredRoles(userRoles: Seq[RoleInfo]): Seq[RoleInfo] = {
    userRoles.filter(_.isExpired)
  }

  override def deleteAllExpiredUserRole(defaultRole: Int, maxTime: Long): Boolean = {
    try {
      val userNames = this.getAllUsername()
      for (username <- userNames) {
        val userRoles = userDAO.getAllRoleInfoOfUser(username)
        val expiredRoles = getExpiredRoles(userRoles.asScala.toSeq)
        if (expiredRoles.nonEmpty) {
          deleteUserRoles(username, expiredRoles.map(_.getId).toSet)
          userDAO.insertUserRole(username, defaultRole, maxTime)
        }
      }
      return true;
    } catch {
      case _: Throwable => return false;
    }
  }

  override def getUserRoleInfos(username: String): Seq[RoleInfo] = {
    userDAO.getAllRoleInfoOfUser(username).filterNot(_.isExpired)
  }

  override def getAllPermission(username: String): Seq[String] = {
    userDAO.getAllPermissionOfUser(username).asInstanceOf[java.util.List[String]].toSet.toSeq
  }

  override def insertUserRole(username: String, role: Int, expireTime: Long, force: Boolean): Unit = {
    getUserRoleInfos(username).find(_.getId.equals(role)) match {
      case Some(userRole) =>
        if (force) {
          userDAO.insertUserRole(username, role, expireTime)
        }
      case None => userDAO.insertUserRole(username, role, expireTime)
    }
  }
}
