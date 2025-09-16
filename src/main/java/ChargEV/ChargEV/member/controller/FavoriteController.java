package ChargEV.ChargEV.member.controller;

import ChargEV.ChargEV.member.domain.UserPrincipal;
import ChargEV.ChargEV.member.dto.FavoriteResDto;
import ChargEV.ChargEV.member.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "즐겨찾기 추가", description = "충전소 ID를 받아 즐겨찾기에 추가")
    @PostMapping("/{statId}")
    public ResponseEntity<Void> addFavorite(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("statId") String statId) {
        favoriteService.addFavorite(userPrincipal.getMember().getId(), statId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "즐겨찾기 삭제", description = "충전소 ID를 받아 즐겨찾기에서 삭제")
    @DeleteMapping("/{statId}")
    public ResponseEntity<Void> removeFavorite(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("statId") String statId) {
        favoriteService.removeFavorite(userPrincipal.getMember().getId(), statId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자 즐겨찾기 조회", description = "현재 사용자의 모든 즐겨찾기 충전소를 조회")
    @GetMapping
    public ResponseEntity<List<FavoriteResDto>> getFavorites(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(favoriteService.getFavorites(userPrincipal.getMember().getId()));
    }

    @Operation(summary = "즐겨찾기 여부 확인", description = "특정 충전소가 현재 사용자의 즐겨찾기에 있는지 확인")
    @GetMapping("/check/{statId}")
    public ResponseEntity<Boolean> checkFavorite(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("statId") String statId) {
        boolean isFavorite = favoriteService.isFavorite(userPrincipal.getMember().getId(), statId);
        return ResponseEntity.ok(isFavorite);
    }
}
