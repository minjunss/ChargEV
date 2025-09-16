package ChargEV.ChargEV.member.service;

import ChargEV.ChargEV.chargingStation.domain.ChargingStation;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationResDto;
import ChargEV.ChargEV.chargingStation.repository.ChargingStationRepository;
import ChargEV.ChargEV.member.domain.Favorite;
import ChargEV.ChargEV.member.domain.Member;
import ChargEV.ChargEV.member.dto.FavoriteResDto;
import ChargEV.ChargEV.member.repository.FavoriteRepository;
import ChargEV.ChargEV.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;
    private final ChargingStationRepository chargingStationRepository;

    @Transactional
    public void addFavorite(Long memberId, String chargingStationStatId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        List<ChargingStation> chargers = chargingStationRepository.findByStatId(chargingStationStatId);
        if (chargers.isEmpty()) {
            throw new IllegalArgumentException("ChargingStation not found");
        }
        ChargingStation chargingStation = chargers.get(0);

        if (favoriteRepository.findByMemberIdAndChargingStationStatId(memberId, chargingStationStatId).isPresent()) {
            throw new IllegalArgumentException("Already favorited");
        }

        Favorite favorite = Favorite.builder()
                .member(member)
                .chargingStation(chargingStation)
                .build();
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long memberId, String chargingStationStatId) {
        favoriteRepository.deleteByMemberIdAndChargingStationStatId(memberId, chargingStationStatId);
    }

    public List<FavoriteResDto> getFavorites(Long memberId) {
        List<Favorite> favorites = favoriteRepository.findByMemberId(memberId);
        return favorites.stream()
                .map(favorite -> FavoriteResDto.builder()
                        .id(favorite.getId())
                        .chargingStation(ChargingStationResDto.builder()
                                .statId(favorite.getChargingStation().getStatId())
                                .name(favorite.getChargingStation().getName())
                                .address(favorite.getChargingStation().getAddress())
                                .latitude(favorite.getChargingStation().getLatitude())
                                .longitude(favorite.getChargingStation().getLongitude())
                                .useTime(favorite.getChargingStation().getUseTime())
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    public boolean isFavorite(Long memberId, String chargingStationStatId) {
        return favoriteRepository.findByMemberIdAndChargingStationStatId(memberId, chargingStationStatId).isPresent();
    }
}
