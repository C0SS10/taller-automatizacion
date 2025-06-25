package Thinking

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Thinking.Data._

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
      .body(StringBody(s"""{"email": "$email", "password": "$password"}""")).asJson
      .check(status.is(200))
      .check(jsonPath("$.token").exists.saveAs("authToken")))

  // Escenario
  setUp(
    scn.inject(
      atOnceUsers(10), // 10 usuarios simultáneos
      rampUsers(10).during(10), // 10 usuarios en 10 segundos
      constantUsersPerSec(5).during(5) // 5 usuarios por segundo durante 5 segundos
    )
  ).protocols(httpConf)
    .assertions(
      global.responseTime.max.lt(8000), // Tiempo máximo de respuesta < 8s
      global.successfulRequests.percent.gt(80) // 80% de peticiones exitosas
  )
}