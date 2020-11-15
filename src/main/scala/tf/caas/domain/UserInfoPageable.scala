package tf.caas.domain

import tf.jcaas.model.entity.UserInfo


case class Page[T](data: Seq[T], total: Long)

case class UserInfoPageable(userInfos: Seq[UserInfo], total: Long)
