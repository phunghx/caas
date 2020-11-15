package tf.caas.repository

import org.apache.commons.codec.digest.HmacUtils


case class PhoneAccountRepository(phoneNum: String, password: String) {
  val PN_SECRET_KEY = "17EIS514KMmQ1g1xxTzq9m86lh7728gF"

  val id = "pn-" + phoneNum

  def getId: String = id

  def getPass: String = HmacUtils.hmacSha1Hex((password + PN_SECRET_KEY).getBytes(), PN_SECRET_KEY.getBytes())

  def getName: String = ""

  def getEmail: String = ""

  def getAvatar: String = ""

  def getPhoneNumber: String = phoneNum
}
