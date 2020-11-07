package xed.caas.controller.http

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

/**
  * @author sonpn created on 9/19/16.
  */
class CaasController extends Controller {
  get("/ping") { request: Request =>
    response.ok("pong")
  }
}
