package tf.caas.domain.thrift

import tf.jcaas.model.entity.{RoleInfo, UserInfo}
import xed.caas.domain.thrift.{TRoleInfo, TUserInfo}
import tf.jcaas.model.entity.UserInfo

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
 * Created by anhlt
 */
object ThriftImplicit {
  implicit def RoleInfo2T(roleInfo: RoleInfo): TRoleInfo = {
    TRoleInfo(roleInfo.getId, roleInfo.getName,roleInfo.getPermissions.asScala, Option(roleInfo.getExpireTime))
  }

  implicit def T2RoleInfo(troleInfo: TRoleInfo): RoleInfo = {
    val roleInfo = new RoleInfo()
    roleInfo.setId(troleInfo.id)
    roleInfo.setName(troleInfo.name)
    roleInfo.setPermissions(troleInfo.permissions.asJava)
    roleInfo
  }

  implicit def T2UserInfo(tUserInfo: TUserInfo): UserInfo = {
    val userInfo = new UserInfo(tUserInfo.username, tUserInfo.isActive, tUserInfo.createTime)
    userInfo.setRoles(tUserInfo.roles.map(v => T2RoleInfo(v)).asJava)
    userInfo
  }

  implicit def UserInfo2T(userInfo: UserInfo): TUserInfo = {
    val tuserInfo = TUserInfo(
      userInfo.getUsername,
      userInfo.isActive, userInfo.getCreateTime,
      userInfo.getRoles.asScala.map[TRoleInfo, Seq[TRoleInfo]](v => v)
    )
    tuserInfo
  }
}
