/* @flow */

const actions: any = {
  selectTemplate(context: any, prop: string) {
    context.dispatch('plantumlEditor/renderUML', context.state[prop], {
      root: true,
    })
  },
}

const state: any = {
  minimalHello: `SEQUENCE @startuml

PARTICIPANT A
PARTICIPANT B

A -> B : GET "/hello"
activate B
B -> A : 200
deactivate B

@enduml
`,
  complexHello: `SEQUENCE @startuml

PARTICIPANT A
PARTICIPANT B

alt "\${testCondition} == 'SomeValue'"
A -> B : POST "/hello/\${id}" (varA : "A", varB : "B")
activate B
B -> A : 200 - (varA := "itemA", varB := "itemB") ["\${varA} == \${varB}"]
deactivate B
end

@enduml
  `,
  rerouting: `SEQUENCE @startuml

participant "Voicemanager" as VM
participant CRS
participant CCC

== Rerouting preparation ==

'##### CCC checks VoiceManager connection status
CCC -[#5B57FF]> CRS : POST "/ccc/rerouteOptions" (countryCode : "\${countryCode1}", positionCountryCode : "\${positionCountryCode1}", sourceEventType : "\${sourceEventType1}")
activate CCC
activate CRS
CRS -> CCC : 200 - (uiswitch := "/UISWITCH", reroute := "/REROUTE", warmhandover := "/WARMHANDOVER")
    deactivate CRS

    '##### CCC checks VoiceManager connection status
    CCC -[#5B57FF]> VM : GET "/ccc/events/\${eventId}/isconnected"
    activate VM
alt "\${voiceEstablished} == true"
    VM -[#5B57FF]> CCC : 200 - (eventid1 := "/VoiceStatus/eventId1", agent1 := "/VoiceStatus/agent1/connectionStatus", agent2 := "/VoiceStatus/agent2/connectionStatus")
else "\${voiceEstablished} == false"
    VM -[#5B57FF]> CCC : 400,404,500
    deactivate VM
    note over CCC : Error is displayed \\n reroute may not be triggered
end

@enduml
`,
  xcall: `SEQUENCE @startuml

participant "Voicemanager" as VM
participant XCS
participant DataService
participant CRS
participant EventNotifier
participant ELOS
participant CCC


activate XCS

XCS -> DataService : GET  "/vehicle/internal/\${vin}"
activate DataService
DataService --> XCS : 200 - (homeCountry := "/homeCountry", positionCountry := "/positionCountry", brand := "/brand")
deactivate DataService

XCS -> CRS : POST "routingTargets/find" (eventId : "\${eventId}", serviceType : "\${serviceType}", vin : "\${vin}", homeCountry : "\${homeCountry}", positionCountry : "\${positionCountry}", brand : "\${brand}")
activate CRS
CRS --> XCS : 200 - (voiceTargets := "/voiceTargets")
deactivate CRS

alt "\${xcsServiceType} == 'ACall'"
XCS -> EventNotifier : PUT "xcs/notify/\${eventId}" (homeCountry : "\${homeCountry}", positionCountry : "\${positionCountry}", brand : "\${brand}")
activate EventNotifier
EventNotifier --> XCS : 200
deactivate EventNotifier
end

alt "\${xcsServiceType} == 'BCall'"
XCS -> ELOS : PUT "xcs/notify/\${eventId}" (homeCountry : "\${homeCountry}", positionCountry : "\${positionCountry}", brand : "\${brand}")
activate ELOS
ELOS --> XCS : 200
deactivate ELOS
end

XCS -> CCC : POST "xcs/eventReceived" (eventId : "\${eventId}", serviceType : "\${serviceType}", homeCountry : "\${homeCountry}", positionCountry : "\${positionCountry}", brand : "\${brand}")
activate CCC
CCC --> XCS : 200
deactivate CCC

XCS -> VM : POST "/setupCall" (eventId : "\${eventId}", vin : "\${vin}", voiceTargets : "\${voiceTargets}")
activate VM
VM --> XCS : 200
deactivate XCS
deactivate VM

@enduml
  `,
  async: `SEQUENCE @startuml

Participant A
Participant B
Participant C

ACTIVATE A
A -> B : GET "/asynccall"
ACTIVATE B
A <- B : 200
B -> C : GET "/synccall"
ACTIVATE C
C -> B : 200
DEACTIVATE C
B -> A : POST "/asyncreturn"
A -> B : 200
DEACTIVATE B
DEACTIVATE A

@enduml
`,
  demo: `SEQUENCE @startuml

Participant A
Participant B
Participant C
Participant D

ACTIVATE A
A -> B : GET "/asynccall/\${id}"
ACTIVATE B
A <- B : 200 - (a := "a") ["\${a} == 'Hello World!'"]
B -> C : GET "/synccall"
ACTIVATE C
C -> D : POST "/important/info" (name : "\${internalinfo}")
ACTIVATE D
D -> C : 200
DEACTIVATE D
C -> B : 200 - ( result := "result")
DEACTIVATE C
alt "\${result} == 'success'"
B -> A : POST "/asyncreturn"
A -> B : 200
else "\${result} != 'success'"
B -> A : POST "/asyncfailure"
A -> B : 200
end
DEACTIVATE B
DEACTIVATE A

@enduml

  `
}

export default {
  namespaced: true,
  state,
  actions,
}
