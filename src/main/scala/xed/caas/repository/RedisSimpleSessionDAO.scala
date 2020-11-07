package xed.caas.repository

import java.io.Serializable
import java.util
import java.util.Collections

import org.apache.commons.lang.SerializationUtils
import org.apache.shiro.session.mgt.SimpleSession
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO
import org.apache.shiro.session.{ProxiedSession, Session, UnknownSessionException}
import redis.clients.jedis.JedisPool
import xed.caas.util.Implicits

/**
 * @author andy
 * @since 5/22/20
 **/
case class RedisSimpleSessionDAO(clientPool: JedisPool,
                                 deserializer : Array[Byte] => Session,
                                 serializer: (Session) => Array[Byte]) extends  AbstractSessionDAO {

  private def dbKey(id: Serializable) = SerializationUtils.serialize(s"xed.user_sessions.$id")

  override protected def doCreate(session: Session): Serializable = {
    val sessionId = generateSessionId(session)
    assignSessionId(session, sessionId)
    storeSession(sessionId, session)
    sessionId
  }

  protected def storeSession(id: Serializable, session: Session): Session = {
    if (id == null) throw new NullPointerException("id argument cannot be null.")

    Implicits.tryWith(clientPool.getResource) { client => {
      client.set(dbKey(id), serializer(session))
      if (session.getTimeout != null && session.getTimeout >= 0) {
        client.pexpire(dbKey(id), session.getTimeout)
      }

      session
    }
    }
  }

  override protected def doReadSession(sessionId: Serializable): Session = {
    Implicits.tryWith(clientPool.getResource) { client => {
      val data = client.get(dbKey(sessionId))
      if (data != null) {
        deserializer(data)
      } else {
        throw new UnknownSessionException(s"$sessionId was not found or expired.")
      }

    }
    }
  }

  @throws[UnknownSessionException]
  override def update(session: Session): Unit = {
    import scala.collection.JavaConversions._
    Implicits.tryWith(clientPool.getResource) { client => {
      val ss = new SimpleSession()
      ss.setId(session.getId)
      ss.setHost(session.getHost)
      ss.setStartTimestamp(session.getStartTimestamp)
      ss.setLastAccessTime(session.getLastAccessTime)
      ss.setTimeout(session.getTimeout)
      session.getAttributeKeys.map(key => {
        ss.setAttribute(key, session.getAttribute(key))
      })
      client.set(dbKey(ss.getId), serializer(ss))
      if (ss.getTimeout != null && ss.getTimeout >= 0) {
        client.pexpire(dbKey(ss.getId), ss.getTimeout)
      }
    }
    }

  }

  override def delete(session: Session): Unit = {
    if (session == null) throw new NullPointerException("session argument cannot be null.")
    val id = session.getId
    if (id != null) {
      Implicits.tryWith(clientPool.getResource) { client => {
        client.del(dbKey(id))
      }
      }
    }
  }

  override def getActiveSessions: util.Collection[Session] = {
    Collections.emptySet[Session]
  }

}
