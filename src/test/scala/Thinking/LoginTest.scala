package Demo

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Demo.Data._

class LoginTest extends Simulation{

  // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    //Verificar de forma general para todas las solicitudes
    .check(status.is(200))

  // 2 Scenario Definition
  val scn = scenario("Login").
    exec(http("login")
      .post(s"/users/login")
      .body(StringBody("""{"email":"${email}","password":"${password}"}""")).asJson
      .check(status.is(200))
      .check(jsonPath("$.token").exists.saveAs("authToken")))

  // Escenario
  setUp(
    scn.inject(
      atOnceUsers(10), // 10 usuarios simultáneos
      rampUsers(10).during(30), // 10 usuarios en 30 segundos
      constantUsersPerSec(5).during(30) // 5 usuarios por segundo durante 30 segundos
    )
  ).protocols(httpConf)
    .assertions(
      global.responseTime.max.lt(3000), // Tiempo máximo de respuesta < 3s
      global.successfulRequests.percent.gt(95) // 95% de peticiones exitosas
  )
}