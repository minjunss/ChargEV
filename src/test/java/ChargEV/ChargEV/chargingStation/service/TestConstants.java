package ChargEV.ChargEV.chargingStation.service;

public class TestConstants {

    public static final String MOCK_JSON_RESPONSE_EMPTY = """
            {
                "items":{
                    "item":[]
                }
            }
            """;

    public static String createMockJsonResponse(String statId, String chgerId, String statUpdDt) {
        return """
                {
                    "items":{
                        "item":[
                            {
                                "statNm":"테스트충전소",
                                "statId":"%s",
                                "chgerId":"%s",
                                "chgerType":"01",
                                "kind":"A0",
                                "kindDetail":"A001",
                                "limitYn":"N",
                                "limitDetail":"",
                                "location":"상세위치",
                                "method":"단독",
                                "note":"",
                                "addr":"테스트주소",
                                "lat":37.5,
                                "lng":127.5,
                                "useTime":"24시간",
                                "stat":"2",
                                "output":"50",
                                "zcode":11,
                                "statUpdDt":"%s",
                                "delYn":"N"
                            }
                        ]
                    }
                }
                """.formatted(statId, chgerId, statUpdDt);
    }
}
