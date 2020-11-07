package xed.caas.domain

import xed.jcaas.model.entity.UserInfo

/**
 * @author sonpn
 */
case class Page[T](data: Seq[T], total: Long)

case class UserInfoPageable(userInfos: Seq[UserInfo], total: Long)
