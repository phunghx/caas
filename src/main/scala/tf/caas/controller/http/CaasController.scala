package tf.caas.controller.http

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller


class CaasController extends Controller {
  get("/ping") { request: Request =>
    response.ok("pong")
  }
}
