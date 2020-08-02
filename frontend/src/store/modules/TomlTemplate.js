/* @flow */

const actions: any = {
  selectTemplate(context: any, prop: string) {
    context.dispatch('plantumlEditor/syncConfigText', context.state[prop], {
      root: true,
    })
  },
}

const state: any = {
  minimalHello: `id = 123
testCondition = '"SomeValue"'

[A]
    username = 'Test'
    password = 'password'
    path = '/testA'
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'

[B]
    username = 'Test'
    password = 'password'
    path = '/testB'
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'
  `,
  complexHello: `id = 123
testCondition = 'SomeValue'

[B]
    username = 'Test'
    password = 'password'
    path = '/testB'
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'
  `,
  rerouting: `id = 123
countryCode1 = "Ger"
positionCountryCode1 = "Muc"
sourceEventType1 = "Hallo"
eventId1 = 123
eventId = 321
voiceEstablished = true

[VM]
    username = "Test"
    password = "password"
    path = "/Voicemanager"
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'

[CRS]
    username = "Test"
    password = "password"
    path = "/CRS"
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'

[CCC]
    username = "Test"
    password = "password"
    path = "/CCC"
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'
`,
  xcall: `id = 123
eventId = 645
vin = 987
serviceType = "ACall"
xcsServiceType = "ACall"
homeCountry = "Ger"
positionCountry = "Jp"
brand = "brandName"
voiceTargets = "tar1"

[VM]
    username = "Test"
    password = "123test"
    path = "/Voicemanager"
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'

[CRS]
    username = "Test"
    password = "123test"
    path = "/CRS"
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'

[CCC]
    username = "Test"
    password = "123test"
    path = "/CCC"
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'

[XCS]
    username = "Test"
    password = "123test"
    path = "/XCS"
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'

[DataService]
    username = "Test"
    password = "123test"
    path = "/DataService"
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'

[EventNotifier]
    username = "Test"
    password = "123test"
    path = "/EventNotifier"
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'

[ELOS]
    username = "Test"
    password = "123test"
    path = "/ELOS"
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'
  `,
  async:`[A]
    username = 'Test'
    password = 'password'
    path = '/testA'
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'

[B]
    username = 'Test'
    password = 'password'
    path = '/testB'
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'

[C]
    username = 'Test'
    password = 'password'
    path = '/testC'
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'
  `,
  demo: `id = 123
internalinfo = 'secret'
result = 'success'

[A]
    username = 'Test'
    password = 'password'
    path = '/testA'
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'

[B]
    username = 'Test'
    password = 'password'
    path = '/testB'
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'

[C]
    username = 'Test'
    password = 'password'
    path = '/testC'
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'

[D]
    username = 'Test'
    password = 'password'
    path = '/testD'
    swagger_json_path = 'http://localhost:8080/swagger/tests.yaml'
  `
}

export default {
  namespaced: true,
  state,
  actions,
}
