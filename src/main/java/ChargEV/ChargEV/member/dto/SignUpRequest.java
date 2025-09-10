package ChargEV.ChargEV.member.dto;

import ChargEV.ChargEV.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SignUpRequest {

    @Schema(description = "이메일 주소", example = "user@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @Schema(description = "닉네임", example = "KoreaGang")
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 15, message = "닉네임은 15자 이내로 입력해야 합니다.")
    private String nickName;

    @Schema(description = "이메일 주소", example = "12가1234")
    @NotBlank(message = "자동차 번호는 필수입니다.")
    @Pattern(regexp = "^[0-9]{2,3}[가-힣]{1}[0-9]{4}$", message = "유효하지 않은 자동차 번호 형식입니다.")
    private String carNum;

    public void setCarNum(String carNum) {
        this.carNum = carNum != null ? carNum.replaceAll("\\s+", "") : null;
    }

    public static Member toEntity(SignUpRequest request) {
        return Member.builder()
                .email(request.getEmail())
                .nickName(request.getNickName())
                .carNum(request.getCarNum())
                .latitude(null)
                .longitude(null)
                .build();
    }
}
