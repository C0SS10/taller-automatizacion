package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class ContactsSimulation extends Simulation {

  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    //Verificar de forma general para todas las solicitudes
    .check(status.is(200))

  // Feeder para generar datos únicos de contactos
  val contactFeeder = Iterator.continually(Map(
    "firstName" -> s"User${scala.util.Random.nextInt(10000)}",
    "lastName" -> s"Test${scala.util.Random.nextInt(10000)}",
    "email" -> s"user${scala.util.Random.nextInt(10000)}@test.com",
    "phone" -> s"${scala.util.Random.nextInt(900000000) + 100000000}"
  ))

  val scn = scenario("Contact Creation Load Test")
    .exec(
      // Login primero
      http("Login")
        .post("/users/login")
        .body(StringBody("""{"email":"${email}","password":"${password}"}"""))
        .check(status.is(200))
        .check(jsonPath("$.token").saveAs("authToken"))
    )
    .pause(1)
    .feed(contactFeeder)
    .exec(
      // Crear contacto
      http("Create Contact")
        .post("/contacts")
        .header("Authorization", "Bearer ${authToken}")
        .body(StringBody("""{"firstName":"${firstName}","lastName":"${lastName}","email":"${email}","phone":"${phone}"}"""))
        .check(status.is(201))
        .check(jsonPath("$._id").saveAs("contactId"))
    )
    .pause(1)
    .exec(
      // Verificar que el contacto aparece en la lista
      http("Get Contacts")
        .get("/contacts")
        .header("Authorization", "Bearer ${authToken}")
        .check(status.is(200))
        .check(jsonPath("$[*]._id").findAll.exists)
    )

  // Simulación de carga masiva de creación de contactos
  setUp(
    scn.inject(
      atOnceUsers(5),
      rampUsers(25) during (30 seconds),
      constantUsersPerSec(3) during (60 seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(5000),
      global.successfulRequests.percent.gt(90)
    )
}