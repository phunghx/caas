package xed.caas.domain

import java.{io, util}
import java.util.{Calendar, Date}

import org.apache.shiro.session.Session
import org.apache.shiro.session.mgt.ValidatingSession

import scala.collection.JavaConverters.seqAsJavaListConverter

/**
 * @author andy
 * @since 5/22/20
 **/
case class XSession(id: String,
                    startTime: Long,
                    var lastAccessTime: Long,
                    var stopTime: Option[Long],
                    var timeout: Option[Long],
                    host: Option[String]) extends ValidatingSession {

  override def getId: io.Serializable = id

  override def getStartTimestamp: Date =  {
    val c = Calendar.getInstance()
    c.setTimeInMillis(startTime)
    c.getTime
  }

  override def getLastAccessTime: Date = {
    val c = Calendar.getInstance()
    c.setTimeInMillis(lastAccessTime)
    c.getTime
  }

  override def getTimeout: Long = timeout.getOrElse(-1L)

  override def setTimeout(maxIdleTimeInMillis: Long): Unit = {
    timeout = Option(maxIdleTimeInMillis)
  }

  override def getHost: String = host.getOrElse(null)

  override def touch(): Unit = {
    lastAccessTime = System.currentTimeMillis()
  }

  override def stop(): Unit = {
    if (this.stopTime.isEmpty)
      this.stopTime = Some(System.currentTimeMillis())
  }


  override def isValid: Boolean = ???

  override def validate(): Unit = ???

  override def getAttributeKeys = {
    Seq.empty.asJava
  }

  override def getAttribute(key: Any): AnyRef = null

  override def setAttribute(key: Any, value: Any): Unit = {

  }

  override def removeAttribute(key: Any): AnyRef = {
    null
  }

}
