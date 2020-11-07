package xed.caas.controller.thrift.filters

import com.twitter.finagle.Service
import com.twitter.finatra.thrift.{ThriftFilter, ThriftRequest}
import com.twitter.inject.Logging
import com.twitter.util.Future

/**
 * @author sonpn
 */
class CommonCustomFilter extends ThriftFilter with Logging {
  override def apply[T, Rep](request: ThriftRequest[T], svc: Service[ThriftRequest[T], Rep]): Future[Rep] = {
    info(s"${request.traceId}\t${request.clientId}\t${request.methodName}\t${request.args}")
    svc(request)
  }
}
