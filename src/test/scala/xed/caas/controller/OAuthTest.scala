package xed.caas.controller

import tf.caas.repository.FacebookOAuthRepository
import org.apache.commons.codec.digest.HmacUtils
import tf.caas.repository.{FacebookOAuthRepository, GoogleOAuthRepository}
import tf.caas.util.JsonParser


object GoogleOAuthTest {
  def main(args: Array[String]): Unit = {
    val id = "112433301274568705376"
    val token = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImI4M2M0ZTU5YTllMWZiODA5ZTM4ZjkwMmFhMWE4YzVkZjY1ZTk1MmEifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiaWF0IjoxNDg2NDMyNDA2LCJleHAiOjE0ODY0MzYwMDYsImF0X2hhc2giOiI5ZjcwWmRpenF1UnByOU9ocUg3Z1pnIiwiYXVkIjoiNTM5NDIzNjQyOTI3LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTEyNDMzMzAxMjc0NTY4NzA1Mzc2IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF6cCI6IjUzOTQyMzY0MjkyNy5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImhkIjoicmV2ZXIudm4iLCJlbWFpbCI6InNvbnBuQHJldmVyLnZuIiwibmFtZSI6IlPGoW4gUGjhuqFtIE5n4buNYyIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vLUpaQW5BZVR2ek9zL0FBQUFBQUFBQUFJL0FBQUFBQUFBQURBL2tqNXJIRm5fV2tNL3M5Ni1jL3Bob3RvLmpwZyIsImdpdmVuX25hbWUiOiJTxqFuIiwiZmFtaWx5X25hbWUiOiJQaOG6oW0gTmfhu41jIiwibG9jYWxlIjoiZW4ifQ.ZS_UkDcZ9sI5C4GO6Ql7O-irEsxCPQgx38zDojfbGtpLRfecFC3POEvMODfqM2CaEivruuTsTsM7hWuAFCJTEXfJBWa5AgkCFlyr7amMHFutqN0FjKUkrIUyLTkNlGMOyUESRqRbpPbJ6BODtIzB58AkppF5HAS0mr-iNudWM6Fz2AlSrSUNWPZjHEeRFHQp_ZlGLwQoj66x3st5_s6Cm1st2L7Rnr4KakREKfApmeiB2u1PKQooBh3tA2QvVKgVJfyY9n11NzNHAsMPmcobr7s-Rb0OcOMQsurWwCdHgUB1Q_P6sTLzCYv6B1Fr2XLiYN9HDbWrY2iLrm47NBnvbw"
    val obj = new GoogleOAuthRepository(id, token)
    println(JsonParser.toJson(obj))
  }
}

object GoogleOAuthFailedTest {
  def main(args: Array[String]): Unit = {
    val id = "112433301274568705376"
    val token = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImI4M2M0ZTU5YTllMWZiODA5ZTM4ZjkwMmFhMWE4YzVkZjY1ZTk1MmEifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiaWF0IjoxNDg2NDMyNDUyLCJleHAiOjE0ODY0MzYwNTIsImF0X2hhc2giOiJ2M2ZFblA3YXhKUDFzdDJUUHlBeHlnIiwiYXVkIjoiNzkzNzg4Mzc4MTY1LTl0NDIzbWFkNjliMzExZ2htdXN0aGVzbnVjYTNhZ3JyLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTEyNDMzMzAxMjc0NTY4NzA1Mzc2IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF6cCI6Ijc5Mzc4ODM3ODE2NS05dDQyM21hZDY5YjMxMWdobXVzdGhlc251Y2EzYWdyci5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImhkIjoicmV2ZXIudm4iLCJlbWFpbCI6InNvbnBuQHJldmVyLnZuIiwibmFtZSI6IlPGoW4gUGjhuqFtIE5n4buNYyIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vLUpaQW5BZVR2ek9zL0FBQUFBQUFBQUFJL0FBQUFBQUFBQURBL2tqNXJIRm5fV2tNL3M5Ni1jL3Bob3RvLmpwZyIsImdpdmVuX25hbWUiOiJTxqFuIiwiZmFtaWx5X25hbWUiOiJQaOG6oW0gTmfhu41jIiwibG9jYWxlIjoiZW4ifQ.pGGnZyPXam9pjzW0CahUuUu3UMKzDtKtDrVsh7cA44qGtCj8FNOdKUsxQmqkCmXX9iy5OELHK6xPW38wQ4dVptLZnQxLwreN9Or_EdwPBauCxwz6uCQ7USZUSY7INdkO837j-tyRnoGrSVSiH_pXYkyTcqjQs_lnyp1ajKD-rKxuC6xhT3OjusWdeC9vlU5tsCBBEGC0FPO5a_iWnFEo7tsOl-3wnbWMGTrNN7uEZy0XnJgAl1zwoDlWQr8jboERqaRkaNPiPosxsWYcdkJ7uo9WaLemqlMD9EXos65dKVtG_0V3dDfQQN7qc2c-F3nY4viJDcF0XcfqtM-tpBdz7w"
    val obj = new GoogleOAuthRepository(id, token)
    println(JsonParser.toJson(obj))
  }
}

object FacebookOAuthTest {
  def main(args: Array[String]): Unit = {
    val id = "1226173844117726"
    val token = "EAAIwm1qe1OsBAAy7LYTaYWR2XGnBWZC53luEXbq1rDo1Rc0qbsHG6gxzGZC1P5qZCVTYAE0YGbX7eG7YUCBBh3CX0tD9zaMTcZB6LwAZAnwlfhpvJusSJ120LxudDjZAptupFjmwK2x4co0sbZBoK5qzY6s1Y4r1hRVFZBjkXFcfhuDPDiUB5ZAJ7Mb1B3PUKR8gZD"
    val obj = new FacebookOAuthRepository(id, token)
    println(JsonParser.toJson(obj))
  }
}

object FacebookOAuthFailedTest {
  def main(args: Array[String]): Unit = {
    val id = "1218235844911526"
    val token = "EAAQZBZCin8ZBkQBADuoMwe9aZBU3AantLxCrVVow3k8l1C9nOHlVKbciQJFREWy9ZAr6ynqSH8qNJouFq4Qb3k7mW3AW537qPav3NhP7ZArRvjtrcc3kINqan53iQvtwd3EFuTcd9lZC3nHxwORxDdxYETh6hDuTj4ZCZCziOTd6Vzn4R3YJr8rRfPhujkApg4V0ZD"
    val obj = new FacebookOAuthRepository(id, token)
    println(JsonParser.toJson(obj))
  }
}

object FacebookOAuthFailed2Test {
  def main(args: Array[String]): Unit = {
    val id = "1226173844117726"
    val token = "EAAQZBZCin8ZBkQBADuoMwe9aZBU3AantLxCrVVow3k8l1C9nOHlVKbciQJFREWy9ZAr6ynqSH8qNJouFq4Qb3k7mW3AW537qPav3NhP7ZArRvjtrcc3kINqan53iQvtwd3EFuTcd9lZC3nHxwORxDdxYETh6hDuTj4ZCZCziOTd6Vzn4R3YJr8rRfPhujkApg4V0ZD"
    val obj = new FacebookOAuthRepository(id, token)
    println(JsonParser.toJson(obj))
  }
}

object FacebookOtherTest{
  def main (args: Array[String] ): Unit = {
    val appSecretProof = HmacUtils.hmacSha256Hex("5d175238643677c3d7c1218ab3b85647", "EAAYfX1rmlLgBALRoZBAm422sYTSxz790jCE47kKZAuSaILc5uo8DSC3mL977ZAn02nlHF97ZBjRoe9CZBfgCChYUFwdvlvzy0mXkZC1m5wf0NICTeFr5fAZAc26cpNrTCBCAocSfNC5YgQ1BxNZCBLX12qRjcXDFxUSmmoSZAfgSmcQZDZD")
    println(appSecretProof)
}
}