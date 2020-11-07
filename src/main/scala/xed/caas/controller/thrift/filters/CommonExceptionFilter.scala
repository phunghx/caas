package xed.caas.controller.thrift.filters

import xed.caas.domain.Implicits._
import xed.caas.domain.thrift.TUserInfoResult
import com.twitter.finagle.Service
import com.twitter.finatra.thrift.{ThriftFilter, ThriftRequest}
import com.twitter.inject.Logging
import com.twitter.util.{Future, NonFatal}

/**
 * @author sonpn
 */
//class CommonExceptionFilter extends ThriftFilter with Logging {
//
//  override def apply[T, Req](request: ThriftRequest[T], service: Service[ThriftRequest[T], Req]): Future[Req] = {
//    service(request).rescue()
//
//    service(request).rescue {
//      case e: Exception => futurePool {
//        error(e.getMessage, e)
//        e
//      }
//      case e => e
//    }
//  }
//}
