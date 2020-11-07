package xed.caas.repository

import java.net.SocketTimeoutException

import xed.caas.util.ZConfig
import org.apache.commons.codec.digest.HmacUtils

import scala.util.parsing.json.JSON
import scalaj.http.Http

/**
 * @author sonpn
 *         created on 9/19/16.
 */
trait OAuthRepository {
  def getId: String

  def getPass: String

  def getName: String

  def getEmail: String

  def getAvatar: String

  def getPhoneNumber: String
}

object OAuthRepository {
  val PassSecretKey = ZConfig.getString("oauth.pass_secret")

  def generatePassword(id: String) = HmacUtils.hmacSha1Hex(id + OAuthRepository.PassSecretKey, OAuthRepository.PassSecretKey)
}


class GoogleOAuthRepository(googleId: String, token: String) extends OAuthRepository {
  private val appID = ZConfig.getString("oauth.google.app_id", null)

  val response = try {
    JSON.parseFull(Http("https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=" + token).timeout(5000, 10000).asString.body) match {
      case Some(s: Map[String, String]) => s
      case _ => throw new InternalError("Error when getting google info")
    }
  } catch {
    case e: SocketTimeoutException => throw new SocketTimeoutException("Timeout when getting google info")
    case e: Exception => throw new Exception("Error when getting google info")
  }

  response.get("aud") match {
    case Some(s) if s.equals(appID) =>
    case _ => throw new IllegalArgumentException("Illegal token!")
  }

  val idRaw = response.get("sub") match {
    case Some(s) => s.toString
    case _ => throw new IllegalArgumentException("Illegal token!")
  }

  val id = "gg-" + idRaw

  override def getId: String = id

  override def getPass: String = OAuthRepository.generatePassword(id)

  override def getName: String = response.getOrElse("name", id).toString

  override def getEmail: String = response.getOrElse("email", "").toString

  override def getAvatar: String = response.getOrElse("picture", "").toString

  override def getPhoneNumber: String = ""
}

class FacebookOAuthRepository(facebookId: String, token: String) extends OAuthRepository {
  private val appSecret = ZConfig.getString("oauth.facebook.app_secret", null)

  val response = try {
    val appSecretProof = HmacUtils.hmacSha256Hex(appSecret, token)
    JSON.parseFull(
      Http(s"https://graph.facebook.com/me/?access_token=$token&appsecret_proof=$appSecretProof&fields=id,name,first_name,last_name,email")
        .timeout(5000, 10000).asString.body
    ) match {
      case Some(s: Map[String, String]) => s
      case _ => throw new InternalError("Error when getting facebook info")
    }
  } catch {
    case e: SocketTimeoutException => throw new SocketTimeoutException("Timeout when getting facebook info")
    case e: Exception => throw new Exception("Error when getting facebook info")
  }

  val idRaw = response.get("id") match {
    case Some(s) => s.toString
    case _ => throw new IllegalArgumentException("Illegal token!")
  }

  val id = "fb-" + idRaw

  override def getId: String = id

  override def getPass: String = OAuthRepository.generatePassword(id)

  override def getName: String = response.getOrElse("name", id).toString

  override def getEmail: String = response.getOrElse("email", "").toString

  override def getAvatar: String = "https://graph.facebook.com/" + idRaw + "/picture?type=large"

  override def getPhoneNumber: String = ""
}