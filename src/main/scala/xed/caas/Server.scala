package xed.caas

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.thrift.ThriftServer
import com.twitter.finatra.thrift.routing.ThriftRouter
import xed.caas.controller._
import xed.caas.controller.thrift.filters.CommonCustomFilter
import xed.caas.module.CaasModule
import xed.caas.util.ZConfig

/**
 * Created by SangDang on 9/8/
 **/
object MainApp extends Server


class Server extends HttpServer with ThriftServer {

  override protected def defaultFinatraHttpPort: String = ZConfig.getString("server.http.port", ":8080")

  override protected def defaultFinatraThriftPort: String = ZConfig.getString("server.thrift.port", ":8082")

  override def modules = Seq(CaasModule)

  override protected def disableAdminHttpServer: Boolean = ZConfig.getBoolean("server.admin.disable", true)

  override protected def configureHttp(router: HttpRouter): Unit = {
    router.filter[CommonFilters]
      .add[http.CaasController]
  }

  override protected def configureThrift(router: ThriftRouter): Unit = {
    router
      .filter[com.twitter.finatra.thrift.filters.AccessLoggingFilter]
      .filter[CommonCustomFilter]
      .add[thrift.CaasController]
  }
}
