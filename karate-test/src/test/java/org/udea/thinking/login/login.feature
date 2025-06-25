Feature: Login de usuario

  Background:
    * url baseUrl
    * def loginUrl = '/users/login'

  Scenario: Login exitoso con credenciales válidas
    Given path loginUrl
    And request { email: 'test@example.com', password: 'mypassword' }
    When method POST
    Then status 200
    And match response.token == '#string'
    And match response.user == '#object'
    * def authToken = response.token

  Scenario: Login fallido con credenciales inválidas
    Given path loginUrl
    And request { email: 'invalid@example.com', password: 'wrongpassword' }
    When method POST
    Then status 401
    And match response.message == 'Incorrect email or password'

  Scenario: Validar token JWT en peticiones subsecuentes
    Given path loginUrl
    And request { email: 'test@example.com', password: 'mypassword' }
    When method POST
    Then status 200
    * def authToken = response.token
    
    Given path '/contacts'
    And header Authorization = 'Bearer ' + authToken
    When method GET
    Then status 200
    And match response == '#array'

  Scenario: Login con email inválido
    Given path loginUrl
    And request { email: 'invalid-email', password: 'mypassword' }
    When method POST
    Then status 400

  Scenario: Login sin campos requeridos
    Given path loginUrl
    And request { email: '', password: '' }
    When method POST
    Then status 400