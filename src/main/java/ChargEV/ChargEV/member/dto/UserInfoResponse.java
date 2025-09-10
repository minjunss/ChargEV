package ChargEV.ChargEV.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {
    private String email;
    private String carNum;
    private String nickName;
}
