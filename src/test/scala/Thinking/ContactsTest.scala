package Thinking

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Thinking.Data._

class ContactsTest extends Simulation {

  // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    .header("Authorization", s"Bearer $authToken")
    .check(status.is(200))

  // Creando usuarios de prueba
  val contactFeeder = Iterator.continually(Map(
    "firstName" -> s"User${scala.util.Random.nextInt(10000)}",
    "lastName" -> s"Test${scala.util.Random.nextInt(10000)}",
    "birthdate" -> s"${scala.util.Random.nextInt(30) + 1}-${scala.util.Random.nextInt(12) + 1}-${scala.util.Random.nextInt(50) + 1970}",
    "email" -> s"user${scala.util.Random.nextInt(10000)}@test.com",
    "phone" -> s"${scala.util.Random.nextInt(900000000) + 100000000}",
    "street1" -> s"Street ${scala.util.Random.nextInt(100)}",
    "street2" -> s"Apt ${scala.util.Random.nextInt(100)}",
    "city" -> s"City${scala.util.Random.nextInt(100)}",
    "stateProvince" -> s"State${scala.util.Random.nextInt(50)}",
    "postalCode" -> s"${scala.util.Random.nextInt(90000) + 10000}",
    "country" -> s"Country${scala.util.Random.nextInt(100)}"
  ))

  val scn = scenario("Creación de Contactos")
    .exec(
      // Login primero
      http("Login")
        .post("/users/login")
        .body(StringBody(s"""{"email": "$email", "password": "$password"}""")).asJson
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
        .body(contactFeeder).asJson
        .check(status.is(201))
        .check(jsonPath("$._id").saveAs("contactId"))
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