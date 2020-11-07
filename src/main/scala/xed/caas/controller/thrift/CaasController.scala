package xed.caas.controller.thrift

import com.twitter.finagle.Service
import com.twitter.finatra.thrift.Controller
import com.twitter.inject.Logging
import com.twitter.util.Future
import javax.inject.Inject
import xed.caas.domain.thrift.ThriftImplicit._
import xed.caas.domain.thrift._
import xed.caas.service.TCaasService.{GetListUserRole, HasAllRoleUser, InsertUserRole, Ping, RegisterWithOAuth, _}
import xed.caas.service.{CaasService, TCaasService}

import scala.collection.JavaConverters._

/**
 * @author sonpn on 9/19/16.
 */
class CaasController @Inject()(caasService: CaasService) extends Controller with TCaasService.ServicePerEndpoint with Logging {

  override def ping = handle(Ping) {
    args: Ping.Args => Future.value("pong")
  }

  override def renewSession = handle(RenewSession) { args: RenewSession.Args =>
    Future {
      try {
        TUserAuthResult(
          code = 0,
          userAuthInfo = Some(
            caasService.renewSession(args.oldSessionId, args.sessionTimeout)
          )
        )
      } catch {
        case e: Exception =>
          logger.error("Exception when renewSession", e)
          TUserAuthResult(-2, Some(e.getMessage))

        case _ =>
          logger.error("Exception when renewSession")
          TUserAuthResult(-2)

      }
    }
  }

  override def login = handle(Login) { args: Login.Args =>
    Future {
      try TUserAuthResult(
        code = 0,
        userAuthInfo = Some(
          caasService.login(args.username, args.password, args.sessionTimeout)
        )
      )
      catch {
        case e: Exception => {
          logger.error("Exception when login", e)
          TUserAuthResult(-2, Some(e.getMessage))
        }
        case _ => {
          logger.error("Exception when login")
          TUserAuthResult(-2)
        }
      }
    }
  }

  override def loginOAuth = handle(LoginOAuth) { args: LoginOAuth.Args =>
    Future {
      try TUserAuthResult(
        code = 0,
        userAuthInfo =
          Some(caasService.loginOAuth(args.username, args.sessionTimeout))
      )
      catch {
        case e: Exception => {
          logger.error("Exception when loginOAuth", e)
          TUserAuthResult(-2, Some(e.getMessage))
        }
        case _ => {
          logger.error("Exception when loginOAuth")
          TUserAuthResult(-2)
        }
      }
    }
  }

  override def loginWithOAuth = handle(LoginWithOAuth) {
    args: LoginWithOAuth.Args =>
      Future {
        try TUserAuthResult(
          0,
          userAuthInfo = Some(
            caasService.loginWithOAuth(
              args.oauthType,
              args.id,
              args.token,
              args.sessionTimeout,
              args.password
            )
          )
        )
        catch {
          case e: Exception => TUserAuthResult(-2, Some(e.getMessage))
          case _            => TUserAuthResult(-2)
        }
      }
  }

  override def registerWithOAuth = handle(RegisterWithOAuth) {
    args: RegisterWithOAuth.Args =>
      Future {
        try TUserInfoResult(
          0,
          userInfo = Some(
            caasService.registerUserWithOAuth(
              args.oauthType,
              args.id,
              args.token,
              args.password
            )
          )
        )
        catch {
          case e: Exception => TUserInfoResult(-2, Some(e.getMessage))
          case _            => TUserInfoResult(-2)
        }
      }
  }

  override def register = handle(Register) { args: Register.Args =>
    Future {
      try {
        val userInfo = caasService.registerUser(args.username, args.password)
        val resp = TUserInfoResult(0, userInfo = Some(userInfo))
        resp
      } catch {
        case e: Exception => TUserInfoResult(-2, Some(e.getMessage))
        case _            => TUserInfoResult(-2)
      }
    }
  }

  override def deleteUser = handle(DeleteUser) { args: DeleteUser.Args =>
    Future {
      try {
        caasService.deleteUser(args.username)
        TResult(0)
      } catch {
        case e: Exception => TResult(-2, Some(e.getMessage))
        case _            => TResult(-2)
      }
    }
  }

  override def logout = handle(Logout) { args: Logout.Args =>
    Future {
      try {
        caasService.logout(args.sessionId)
        TResult(0)
      } catch {
        case e: Exception => TResult(-2, Some(e.getMessage))
        case _            => TResult(-2)
      }
    }
  }

  override def isPermitted = handle(IsPermitted) { args: IsPermitted.Args =>
    Future {
      try TBoolResult(
        code = 0,
        data = Some(caasService.isPermitted(args.sessionId, args.permission))
      )
      catch {
        case e: Exception => TBoolResult(-2, Some(e.getMessage))
        case _            => TBoolResult(-2)
      }
    }
  }

  override def isPermitteds = handle(IsPermitteds) { args: IsPermitteds.Args =>
    Future {
      try TListBoolResult(
        0,
        data = Some(caasService.isPermitteds(args.sessionId, args.permissions))
      )
      catch {
        case e: Exception => TListBoolResult(-2, Some(e.getMessage))
        case _            => TListBoolResult(-2)
      }
    }
  }

  override def isPermittedAll = handle(IsPermittedAll) {
    args: IsPermittedAll.Args =>
      Future {
        try TBoolResult(
          0,
          data =
            Some(caasService.isPermittedAll(args.sessionId, args.permissions))
        )
        catch {
          case e: Exception => TBoolResult(-2, Some(e.getMessage))
          case _            => TBoolResult(-2)
        }
      }
  }

  override def isPermittedUser = handle(IsPermittedUser) {
    args: IsPermittedUser.Args =>
      Future {
        try TBoolResult(
          0,
          data =
            Some(caasService.isPermittedUser(args.username, args.permission))
        )
        catch {
          case e: Exception => TBoolResult(-2, Some(e.getMessage))
          case _            => TBoolResult(-2)
        }
      }
  }

  override def isPermittedsUser = handle(IsPermittedsUser) {
    args: IsPermittedsUser.Args =>
      Future {
        try TListBoolResult(
          0,
          data =
            Some(caasService.isPermittedsUser(args.username, args.permissions))
        )
        catch {
          case e: Exception => TListBoolResult(-2, Some(e.getMessage))
          case _            => TListBoolResult(-2)
        }
      }
  }

  override def isPermittedUserAll = handle(IsPermittedUserAll) {
    args: IsPermittedUserAll.Args =>
      Future {
        try TBoolResult(
          0,
          data = Some(
            caasService.isPermittedUserAll(args.username, args.permissions)
          )
        )
        catch {
          case e: Exception => TBoolResult(-2, Some(e.getMessage))
          case _            => TBoolResult(-2)
        }
      }
  }

  override def getUserWithSessionId = handle(GetUserWithSessionId) {
    args: GetUserWithSessionId.Args =>
      Future {
        try TUserInfoResult(
          0,
          userInfo = Some(caasService.getUserWithSessionId(args.sessionId))
        )
        catch {
          case e: Exception => TUserInfoResult(-2, Some(e.getMessage))
          case _            => TUserInfoResult(-2)
        }
      }
  }

  override def getUserRoles = handle(GetUserRoles) { args: GetUserRoles.Args =>
    Future {
      try TListStringResult(
        0,
        data = Some(caasService.getUserRoles(args.sessionId))
      )
      catch {
        case e: Exception => TListStringResult(-2, Some(e.getMessage))
        case _            => TListStringResult(-2)
      }
    }
  }

  override def hasRole = handle(HasRole) { args: HasRole.Args =>
    Future {
      try TBoolResult(
        0,
        data = Some(caasService.hasRole(args.sessionId, args.role))
      )
      catch {
        case e: Exception => TBoolResult(-2, Some(e.getMessage))
        case _            => TBoolResult(-2)
      }
    }
  }

  override def hasRoles = handle(HasRoles) { args: HasRoles.Args =>
    Future {
      try TListBoolResult(
        0,
        data = Some(caasService.hasRoles(args.sessionId, args.roles))
      )
      catch {
        case e: Exception => TListBoolResult(-2, Some(e.getMessage))
        case _            => TListBoolResult(-2)
      }
    }
  }

  override def getAllUsername = handle(GetAllUsername) {
    args: GetAllUsername.Args =>
      Future {
        try TListStringResult(0, data = Some(caasService.getAllUsername()))
        catch {
          case e: Exception => TListStringResult(-2, Some(e.getMessage))
          case _            => TListStringResult(-2)
        }
      }
  }

  override def getActiveUsername = handle(GetActiveUsername) {
    args: GetActiveUsername.Args =>
      Future {
        try {
          val result = caasService.getActiveUsername(args.from, args.size)
          TListUserResult(
            0,
            total = Some(result.getTotal),
            users = Some(result.getData.asScala)
          )
        } catch {
          case e: Exception =>
            TListUserResult(code = -2, msg = Some(e.getMessage))
          case _ => TListUserResult(code = -2)
        }
      }
  }

  //  override def loginPhonenumber = handle(LoginPhonenumber) {
  //    args: LoginPhonenumber.Args =>
  //      Future {
  //        try TUserAuthResult(0, userAuthInfo = Some(caasService.loginPhonenummber(args.phoneNumber, args.password, args.sessionTimeout)))
  //        catch {
  //          case e: Exception => TUserAuthResult(-2, Some(e.getMessage))
  //          case _ => TUserAuthResult(-2)
  //        }
  //      }
  //  }

  //  override def registerPhonenumber = handle(RegisterPhonenumber) {
  //    args: RegisterPhonenumber.Args =>
  //      Future {
  //        try TUserInfoResult(0, userInfo = Some(caasService.registerPhonenumber(args.username, args.password)))
  //        catch {
  //          case e: Exception => TUserInfoResult(-2, Some(e.getMessage))
  //          case _ => TUserInfoResult(-2)
  //        }
  //      }
  //  }

  //  override def updatePhonenumber = handle(UpdatePhonenumber) {
  //    args: UpdatePhonenumber.Args =>
  //      Future {
  //        try TUserInfoResult(0, userInfo = Some(caasService.updatePhonenumber(args.username, args.password))) catch {
  //          case e: Exception => TUserInfoResult(-2, Some(e.getMessage))
  //          case _ => TUserInfoResult(-2)
  //        }
  //      }
  //  }

  override def insertUserRoles = handle(InsertUserRoles) {
    args: InsertUserRoles.Args =>
      Future {
        try {
          val expirableRoleMap = args.roleIds.map(_ -> Long.MaxValue).toMap
          caasService.insertUserRoles(args.username, expirableRoleMap)
          TBoolResult(code = 0, data = Some(true))
        } catch {
          case e: Exception => TBoolResult(code = -2, msg = Some(e.getMessage))
          case _            => TBoolResult(code = -2)
        }
      }
  }

  override def deleteUserRoles = handle(DeleteUserRoles) {
    args: DeleteUserRoles.Args =>
      Future {
        try {
          caasService.deleteUserRoles(args.username, args.roleIds.toSet)
          TBoolResult(code = 0, data = Some(true))
        } catch {
          case e: Exception => TBoolResult(code = -2, msg = Some(e.getMessage))
          case _            => TBoolResult(code = -2)
        }
      }
  }

  override def getListUserRole = handle(GetListUserRole) {
    args: GetListUserRole.Args =>
      Future {
        try {
          val userRolesResp = caasService.getListUserRole(
            args.notInRoleIds,
            args.inRoleIds,
            args.from,
            args.size
          )
          val userInfos = userRolesResp.userInfos.map(f => UserInfo2T(f))
          TUserInfoPageable(
            code = 0,
            users = Some(userInfos),
            total = Some(userRolesResp.total)
          )
        } catch {
          case e: Exception =>
            TUserInfoPageable(code = -2, msg = Some(e.getMessage))
          case _ => TUserInfoPageable(code = -2)
        }
      }
  }

  override def searchListUserRole = handle(SearchListUserRole) {
    args: SearchListUserRole.Args =>
      Future {
        try {
          val userRolesResp = caasService.searchListUserRole(
            args.usernameSearchKey,
            args.notInRoleIds,
            args.inRoleIds,
            args.from,
            args.size
          )
          TUserInfoPageable(
            code = 0,
            users = Some(userRolesResp.userInfos.map(f => UserInfo2T(f))),
            total = Some(userRolesResp.total)
          )
        } catch {
          case e: Exception =>
            TUserInfoPageable(code = -2, msg = Some(e.getMessage))
          case _ => TUserInfoPageable(code = -2)
        }
      }
  }

  override def getUserWithUsername = handle(GetUserWithUsername) {
    args: GetUserWithUsername.Args =>
      Future {
        try TUserInfoResult(
          0,
          userInfo = Some(caasService.getUserWithUsername(args.username))
        )
        catch {
          case e: Exception => TUserInfoResult(-2, Some(e.getMessage))
          case _            => TUserInfoResult(-2)
        }
      }
  }

  override def resetPasswordUser = handle(ResetPasswordUser) {
    args: ResetPasswordUser.Args =>
      Future {
        try {
          caasService.resetPasswordUser(args.username, args.newPassword)
          TBoolResult(code = 0, data = Some(true))
        } catch {
          case e: Exception => TBoolResult(code = -2, msg = Some(e.getMessage))
          case _            => TBoolResult(code = -2)
        }
      }
  }

  override def updatePasswordUser = handle(UpdatePasswordUser) {
    args: UpdatePasswordUser.Args =>
      Future {
        try {
          caasService.updatePasswordUser(
            args.username,
            args.oldPassword,
            args.newPassword
          )
          TBoolResult(code = 0, data = Some(true))
        } catch {
          case e: Exception => TBoolResult(code = -2, msg = Some(e.getMessage))
          case _            => TBoolResult(code = -2)
        }
      }
  }

  override def isCredentialDefault = handle(IsCredentialDefault) {
    args: IsCredentialDefault.Args =>
      Future {
        try {
          TBoolResult(
            code = 0,
            data = Some(
              caasService.isCredentialDefault(args.oauthType, args.username)
            )
          )
        } catch {
          case e: Exception => TBoolResult(code = -2, msg = Some(e.getMessage))
          case _            => TBoolResult(code = -2)
        }
      }
  }

  override def deleteAllExpiredUserRole = handle(DeleteAllExpiredUserRole) {
    args: DeleteAllExpiredUserRole.Args =>
      Future {
        try {
          val r = caasService
            .deleteAllExpiredUserRole(args.defaultRoleId, args.maxTime)
          TBoolResult(code = 0, data = Some(r))
        } catch {
          case e: Exception => TBoolResult(code = -2, msg = Some(e.getMessage))
          case _            => TBoolResult(code = -2)
        }
      }
  }

  override def getAllRoleInfo = handle(GetAllRoleInfo) {
    args: GetAllRoleInfo.Args =>
      Future {
        try {
          val r = caasService.getUserRoleInfos(args.username).map(RoleInfo2T)
          TListRoleInfoResult(code = 0, args.username, roles = Some(r))
        } catch {
          case ex: Exception =>
            TListRoleInfoResult(
              code = -2,
              msg = Some(ex.getMessage),
              username = args.username
            )
          case _ => TListRoleInfoResult(code = -2, username = args.username)
        }
      }
  }

  override def hasRoleUser = handle(HasRoleUser) { args: HasRoleUser.Args =>
      Future {
        try {
          val r = caasService.hasRoleUser(args.username, args.roleName)
          TBoolResult(code = 0, data = Some(r))
        } catch {
          case e: Exception => TBoolResult(code = -2, msg = Some(e.getMessage))
          case _            => TBoolResult(code = -2)
        }
      }
    }

  override def hasAllRoleUser = handle(HasAllRoleUser) { args: HasAllRoleUser.Args =>
      Future {
        try {
          val r = caasService.hasAllRoleUser(args.username, args.roleNames)
          TBoolResult(code = 0, data = Some(r))
        } catch {
          case e: Exception => TBoolResult(code = -2, msg = Some(e.getMessage))
          case _            => TBoolResult(code = -2)
        }
      }
    }

  override def getAllPermission = handle(GetAllPermission) {
    args: GetAllPermission.Args =>
      Future {
        try {
          val r = caasService.getAllPermission(args.username)
          TListStringResult(code = 0, data = Some(r))
        } catch {
          case e: Exception =>
            TListStringResult(code = -2, msg = Some(e.getMessage))
          case _ => TListStringResult(code = -2)
        }
      }
  }

  override def insertUserRole: Service[InsertUserRole.Args, TBoolResult] = handle(InsertUserRole) {
    args: InsertUserRole.Args => {
      Future {
        try {
          val r = caasService.insertUserRole(args.username, args.role, args.expireTime, args.force)
          TBoolResult(code = 0, data = Some(true))
        } catch {
          case e: Exception => TBoolResult(code = -2, msg = Some(e.getMessage))
          case _ => TBoolResult(code = -2)
        }
      }
    }
  }

  override def insertExpirableUserRoles: Service[InsertExpirableUserRoles.Args, TBoolResult] = handle(InsertExpirableUserRoles) {
    args: InsertExpirableUserRoles.Args =>
      Future {
        try {
          caasService.insertUserRoles(args.username, args.roleIds.toMap)
          TBoolResult(code = 0, data = Some(true))
        } catch {
          case e: Exception => TBoolResult(code = -2, msg = Some(e.getMessage))
          case _            => TBoolResult(code = -2)
        }
      }
  }
}
