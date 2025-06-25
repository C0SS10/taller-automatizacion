package Thinking

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Thinking.Data._

class ContactsTest extends Simulation {

  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val contactFeeder = Iterator.continually(Map(
    "firstName" -> s"User${scala.util.Random.nextInt(10000)}",
    "lastName" -> s"Test${scala.util.Random.nextInt(10000)}",
    "birthdate" -> f"${1970 + scala.util.Random.nextInt(50)}-${1 + scala.util.Random.nextInt(12)}%02d-${1 + scala.util.Random.nextInt(28)}%02d",
    "email" -> s"user${scala.util.Random.nextInt(10000)}@test.com",
    "phone" -> s"${scala.util.Random.nextInt(900000000) + 100000000}",
    "street1" -> s"Street ${scala.util.Random.nextInt(100)}",
    "street2" -> s"Apt ${scala.util.Random.nextInt(100)}",
    "city" -> s"City${scala.util.Random.nextInt(100)}",
    "stateProvince" -> s"State${scala.util.Random.nextInt(50)}",
    "postalCode" -> f"${scala.util.Random.nextInt(90000) + 10000}",
    "country" -> s"Country${scala.util.Random.nextInt(100)}"
  ))

  val loginChain = exec(
  http("Login")
    .post("/users/login")
    .body(StringBody(s"""{"email": "$email", "password": "$password"}""")).asJson
    .check(status.is(200))
    .check(jsonPath("$.token").exists.saveAs("authToken"))
  )


  val scn = scenario("Creación de Contactos")
  .exec(loginChain)
  .pause(1)
  .doIf(session => session.contains("authToken")) {
    repeat(5) {
      feed(contactFeeder)
        .exec(
          http("Create Contact")
            .post("/contacts")
            .header("Authorization", "Bearer ${authToken}")
            .body(StringBody(
              """
              {
                "firstName": "${firstName}",
                "lastName": "${lastName}",
                "birthdate": "${birthdate}",
                "email": "${email}",
                "phone": "${phone}",
                "street1": "${street1}",
                "street2": "${street2}",
                "city": "${city}",
                "stateProvince": "${stateProvince}",
                "postalCode": "${postalCode}",
                "country": "${country}"
              }
              """)).asJson
            .check(status.in(200, 201))
        )
        .pause(1)
    }
  }

  setUp(
    scn.inject(
      atOnceUsers(2),
      rampUsers(10).during(30),
      constantUsersPerSec(1).during(60)
    )
  ).protocols(httpConf)
    .assertions(
      global.responseTime.max.lt(5000),
      global.successfulRequests.percent.gt(90)
    )
}
