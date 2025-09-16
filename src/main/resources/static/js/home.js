
    let map;
    let lastBounds = null;
    let debounceTimer = null;
    let openInfoWindow = null;
    let markers = []; // 마커들을 저장할 배열
    let stationMarkers = new Map(); // statId를 키로 마커와 infoWindow를 저장할 Map

    function onSuccessGeolocation(position) {
        const userLocation = new naver.maps.LatLng(position.coords.latitude, position.coords.longitude);
        map = new naver.maps.Map('map', {
            center: userLocation,
            zoom: 16,
            minZoom: 14
        });
        // 현재 위치 마커는 그대로 둠 (필터링과 무관)
        new naver.maps.Marker({
            position: userLocation,
            map: map,
            title: '현재 위치',
            icon: {
                url: 'https://maps.google.com/mapfiles/ms/icons/red-dot.png',
                size: new naver.maps.Size(32, 32),
                origin: new naver.maps.Point(0, 0),
                anchor: new naver.maps.Point(16, 32)
            }
        });
        setupIdleListener();
        setupMapClickToCloseInfoWindow();
        fetchChargingStationsIfNeeded();
    }

    function onErrorGeolocation(error) {
        alert('위치 정보를 가져올 수 없습니다: ' + error.message);
        map = new naver.maps.Map('map', {
            center: new naver.maps.LatLng(37.3595704, 127.105399),
            zoom: 10
        });
        setupIdleListener();
        setupMapClickToCloseInfoWindow();
        fetchChargingStationsIfNeeded();
    }

    // CSRF 토큰과 헤더 이름을 메타 태그에서 읽어오는 함수
    function getCsrfTokenAndHeader() {
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
        return { token: csrfToken, header: csrfHeader };
    }

    // 마커 아이콘 URL 정의
    const ICONS = {
        GREEN: 'http://maps.google.com/mapfiles/ms/icons/green-dot.png',
        BLUE: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png',
        GRAY: 'http://maps.google.com/mapfiles/ms/icons/grey-dot.png'
    };

    // 기존 마커를 지우는 함수
    function clearMarkers() {
        markers.forEach(marker => marker.setMap(null));
        markers = [];
        stationMarkers.clear(); // stationMarkers 맵도 초기화
    }

    function getMarkerIcon(station) {
        if (station.hasAvailableCharger) {
            return ICONS.GREEN;
        }
        switch (station.stat) {
            case '2': // 충전대기
                return ICONS.GREEN;
            case '3': // 충전중
                return ICONS.BLUE;
            default: // 통신이상, 운영중지, 점검중 등
                return ICONS.GRAY;
        }
    }

    function boundsChangedEnough(newBounds, oldBounds) {
        if (!oldBounds) return true;
        const nSW = newBounds.getSW();
        const nNE = newBounds.getNE();
        const oSW = oldBounds.getSW();
        const oNE = oldBounds.getNE();
        const threshold = 0.002;
        return (
            Math.abs(nSW.y - oSW.y) > threshold ||
            Math.abs(nSW.x - oSW.x) > threshold ||
            Math.abs(nNE.y - oNE.y) > threshold ||
            Math.abs(nNE.x - oNE.x) > threshold
        );
    }

    function fetchChargingStationsIfNeeded() {
        const bounds = map.getBounds();
        const sw = bounds.getSW();
        const ne = bounds.getNE();

        // 필터링된 충전기 타입 가져오기
        const checkedTypes = Array.from(document.querySelectorAll('.charger-type-filter:checked')).map(cb => cb.value);

        const requestData = {
            minLatitude: sw.y,
            maxLatitude: ne.y,
            minLongitude: sw.x,
            maxLongitude: ne.x,
            chargerTypes: checkedTypes
        };

        // CSRF 토큰 가져오기
        const csrf = getCsrfTokenAndHeader();

        fetch('/api/chargingStation/range', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrf.header]: csrf.token // CSRF 토큰 추가
            },
            body: JSON.stringify(requestData)
        })
            .then(response => response.json())
            .then(data => {
                clearMarkers(); // 새 데이터를 받으면 기존 마커 지우기

                data.forEach(station => {
                    const marker = new naver.maps.Marker({
                        position: new naver.maps.LatLng(station.latitude, station.longitude),
                        map: map,
                        title: station.name,
                        icon: {
                            url: getMarkerIcon(station),
                            size: new naver.maps.Size(32, 32),
                            origin: new naver.maps.Point(0, 0),
                            anchor: new naver.maps.Point(16, 32)
                        }
                    });

                    markers.push(marker); // 새로 생성된 마커를 배열에 추가

                    const content = `
                    <div style="padding:8px; font-size:13px; line-height:1.5; white-space:nowrap;">
                        <strong>${station.name}</strong><br>
                        📍 ${station.address}<br>
                        🕒 ${station.useTime || '이용 시간 정보 없음'}<br>
                        🚫 ${station.limitYn === 'Y' ? (station.limitDetail || '제한 있음') : '제한 없음'}<br>
                        <button class="btn btn-primary btn-sm mt-2" onclick="loadDetail('${station.statId}')">상세보기</button>
                    </div>
                `;
                    const infoWindow = new naver.maps.InfoWindow({ content: content });
                    naver.maps.Event.addListener(marker, 'click', () => {
                        if (openInfoWindow) openInfoWindow.close();
                        infoWindow.open(map, marker);
                        openInfoWindow = infoWindow;
                    });

                    // stationMarkers 맵에 저장
                    stationMarkers.set(station.statId, { marker: marker, infoWindow: infoWindow });
                });
            })
            .catch(console.error);
    }

    function loadDetail(statId, latitude, longitude) {
        console.log(`loadDetail called with statId: ${statId}, lat: ${latitude}, lng: ${longitude}`); // Debug log
        const csrf = getCsrfTokenAndHeader();
        const favoriteButton = document.getElementById('favorite-button');
        const chargerDetailsContainer = document.getElementById('charger-details-container');
        
        // 즐겨찾기 버튼 초기화
        favoriteButton.style.display = 'none'; // 기본적으로 숨김
        favoriteButton.onclick = null; // 기존 이벤트 리스너 제거

        // 현재 로그인 상태 확인 (body의 data-logged-in 속성 사용)
        const isLoggedIn = document.body.dataset.loggedIn === 'true';
        console.log("loadDetail: isLoggedIn =", isLoggedIn);

        // 지도 이동 (새로 추가된 부분)
        if (latitude && longitude) {
            const pos = new naver.maps.LatLng(latitude, longitude);
            map.setCenter(pos);
            map.setZoom(16); // 적절한 줌 레벨 설정

            // 해당 마커의 infoWindow 열기
            const stationData = stationMarkers.get(statId);
            if (stationData) {
                if (openInfoWindow) openInfoWindow.close();
                stationData.infoWindow.open(map, stationData.marker);
                openInfoWindow = stationData.infoWindow;
            }
        }

        // 상세 정보 로드
        fetch(`/api/chargingStation/detail?statId=${statId}`, {
            method: 'GET',
            headers: {
                [csrf.header]: csrf.token
            }
        })
        .then(res => res.json())
        .then(data => {
            const html = data.map(d => `
                <div class="card mb-2">
                    <div class="card-header">
                        충전기 ID: ${d.chargerId}
                    </div>
                    <div class="card-body">
                        <p class="card-text"><strong>충전기 타입:</strong> ${d.chargerType}</p>
                        <p class="card-text"><strong>상태:</strong> ${d.stat}</p>
                        <p class="card-text"><strong>출력:</strong> ${d.output}</p>
                        <p class="card-text"><strong>이용 시간:</strong> ${d.useTime}</p>
                        <p class="card-text"><strong>제한:</strong> ${d.limitYn === 'Y' ? '있음' : '없음'} ${d.limitDetail ? `(${d.limitDetail})` : ''}</p>
                    </div>
                </div>
            `).join("");
            
            chargerDetailsContainer.innerHTML = html;
            document.getElementById("station-detail").style.display = "block";
            document.getElementById("station-detail").scrollIntoView({ behavior: 'smooth', block: 'start' });

            // 로그인된 경우에만 즐겨찾기 버튼 표시 및 상태 확인
            if (isLoggedIn) {
                checkFavoriteStatus(statId).then(isFavorited => {
                    console.log(`Station ${statId} is favorited: ${isFavorited}`); // Debug log
                    updateFavoriteButton(isFavorited);
                    favoriteButton.style.display = 'block';
                    favoriteButton.onclick = () => toggleFavorite(statId, isFavorited);
                });
            }

        })
        .catch(err => alert("상세정보를 불러오는 데 실패했습니다."));
    }

    // 즐겨찾기 상태 확인 함수
    async function checkFavoriteStatus(statId) {
        const csrf = getCsrfTokenAndHeader();
        try {
            const response = await fetch(`/api/favorites/check/${statId}`, {
                method: 'GET',
                headers: {
                    [csrf.header]: csrf.token
                }
            });
            if (!response.ok) {
                // 인증되지 않은 경우 401이 올 수 있음. 이 경우 즐겨찾기 버튼 숨김
                if (response.status === 401) {
                    document.getElementById('favorite-button').style.display = 'none';
                    return false;
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json(); // true 또는 false 반환
        } catch (error) {
            console.error("즐겨찾기 상태 확인 중 오류 발생:", error);
            return false;
        }
    }

    // 즐겨찾기 추가/삭제 토글 함수
    async function toggleFavorite(statId, isFavorited) {
        const csrf = getCsrfTokenAndHeader();
        const method = isFavorited ? 'DELETE' : 'POST';
        const url = `/api/favorites/${statId}`;

        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                    [csrf.header]: csrf.token
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            // 상태 업데이트 후 버튼 UI 변경
            const newIsFavorited = !isFavorited;
            updateFavoriteButton(newIsFavorited);
            document.getElementById('favorite-button').onclick = () => toggleFavorite(statId, newIsFavorited); // 이벤트 리스너 업데이트
            alert(`즐겨찾기 ${newIsFavorited ? '추가' : '삭제'} 완료!`);

        } catch (error) {
            console.error("즐겨찾기 토글 중 오류 발생:", error);
            alert(`즐겨찾기 ${isFavorited ? '삭제' : '추가'} 실패.`);
        }
    }

    // 즐겨찾기 버튼 UI 업데이트 함수
    function updateFavoriteButton(isFavorited) {
        const favoriteButton = document.getElementById('favorite-button');
        if (isFavorited) {
            favoriteButton.innerHTML = '<i class="bi bi-star-fill"></i> 즐겨찾기 삭제';
            favoriteButton.classList.remove('btn-outline-warning');
            favoriteButton.classList.add('btn-warning');
        } else {
            favoriteButton.innerHTML = '<i class="bi bi-star"></i> 즐겨찾기 추가';
            favoriteButton.classList.remove('btn-warning');
            favoriteButton.classList.add('btn-outline-warning');
        }
    }

    function setupIdleListener() {
        naver.maps.Event.addListener(map, 'idle', () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(fetchChargingStationsIfNeeded, 300);
        });
    }

    function setupMapClickToCloseInfoWindow() {
        naver.maps.Event.addListener(map, 'click', () => {
            if (openInfoWindow) {
                openInfoWindow.close();
                openInfoWindow = null;
            }
        });
    }

    // 즐겨찾기 목록을 로드하고 표시하는 함수
    async function loadFavorites() {
        const csrf = getCsrfTokenAndHeader();
        const favoritesListContainer = document.getElementById('favorites-list-container');
        const favoritesSection = document.getElementById('favorites-section');

        try {
            const response = await fetch('/api/favorites', {
                method: 'GET',
                headers: {
                    [csrf.header]: csrf.token
                }
            });

            if (!response.ok) {
                if (response.status === 401) {
                    alert('로그인이 필요합니다.');
                    window.location.href = '/login'; // 로그인 페이지로 리다이렉트
                    return;
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const favorites = await response.json();

            if (favorites.length === 0) {
                favoritesListContainer.innerHTML = '<p>아직 즐겨찾기한 충전소가 없습니다.</p>';
            } else {
                const html = favorites.map(fav => `
                    <div class="card mb-2">
                        <div class="card-body" onclick="loadDetail('${fav.chargingStation.statId}', ${fav.chargingStation.latitude}, ${fav.chargingStation.longitude})">
                            <h5 class="card-title">${fav.chargingStation.name}</h5>
                            <p class="card-text">📍 ${fav.chargingStation.address}</p>
                            <button class="btn btn-sm btn-outline-danger ms-2" onclick="event.stopPropagation(); toggleFavorite('${fav.chargingStation.statId}', true)">즐겨찾기 삭제</button>
                        </div>
                    </div>
                `).join('');
                favoritesListContainer.innerHTML = html;
            }
            favoritesSection.style.display = 'block'; // 즐겨찾기 섹션 표시
            favoritesSection.scrollIntoView({ behavior: 'smooth', block: 'start' }); // 스크롤 이동
        } catch (error) {
            console.error("즐겨찾기 로드 중 오류 발생:", error);
            favoritesListContainer.innerHTML = '<p>즐겨찾기 목록을 불러오는 데 실패했습니다.</p>';
            favoritesSection.style.display = 'block'; // 오류 메시지를 보여주기 위해 섹션 표시
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        const isLoggedIn = document.body.dataset.loggedIn === 'true';

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(onSuccessGeolocation, onErrorGeolocation);
        } else {
            alert("이 브라우저는 Geolocation을 지원하지 않습니다.");
            onErrorGeolocation({ message: "Geolocation 미지원" });
        }

        // 필터 체크박스에 이벤트 리스너 추가
        document.querySelectorAll('.charger-type-filter').forEach(checkbox => {
            checkbox.addEventListener('change', fetchChargingStationsIfNeeded);
        });

        // 주소 자동완성
        document.getElementById("address-input").addEventListener("input", function () {
            const query = this.value;
            const resultBox = document.getElementById("autocomplete-results");
            if (!query.trim()) {
                resultBox.style.display = "none";
                return;
            }

            fetch(`/api/localSearch/search?query=${encodeURIComponent(query)}`)
                .then(res => res.json())
                .then(data => {
                    if (!data.items || data.items.length === 0) {
                        resultBox.style.display = "none";
                        return;
                    }
                    resultBox.innerHTML = data.items.map(item => `
                        <a href="#" class="list-group-item list-group-item-action" onclick="selectPlace('${item.mapy}', '${item.mapx}', '${item.title.replace(/<[^>]+>/g, '')}'); return false;">
                            <div class="d-flex w-100 justify-content-between">
                                <h6 class="mb-1">${item.title.replace(/<[^>]+>/g, '')}</h6>
                            </div>
                            <small class="text-muted">${item.roadAddress || item.address || ''}</small>
                        </a>
                    `).join('');
                    resultBox.style.display = "block";
                })
                .catch(err => {
                    console.error("자동완성 오류", err);
                    resultBox.style.display = "none";
                });
        });

        // 로그인 상태에 따라 즐겨찾기 버튼 표시 및 로드
        if (isLoggedIn) {
            const sidebarHeader = document.querySelector('.sidebar-header');
            const favoritesButton = document.createElement('button');
            favoritesButton.classList.add('btn', 'btn-sm', 'btn-outline-info', 'ms-2');
            favoritesButton.innerHTML = '<i class="bi bi-star"></i> 내 즐겨찾기';
            favoritesButton.onclick = () => {
                const favoritesSection = document.getElementById('favorites-section');
                if (favoritesSection.style.display === 'none') {
                    loadFavorites();
                    favoritesButton.classList.remove('btn-outline-info');
                    favoritesButton.classList.add('btn-info');
                } else {
                    favoritesSection.style.display = 'none';
                    favoritesButton.classList.remove('btn-info');
                    favoritesButton.classList.add('btn-outline-info');
                }
            };
            sidebarHeader.querySelector('div').appendChild(favoritesButton);
        }

        // 상세 정보 닫기 버튼 이벤트 리스너
        document.getElementById('close-detail-button').addEventListener('click', () => {
            document.getElementById('station-detail').style.display = 'none';
        });
    });

    function fixCoordinate(coordStr) {
        if (!coordStr) return 0;
        const s = coordStr.toString();
        if (s.length >= 8) {  // 8자리 이상이면 소수점 삽입 (한국 위도 경도는 2자리 + 소수점 이하 6자리)
            return parseFloat(s.slice(0, 2) + '.' + s.slice(2));
        }
        return parseFloat(coordStr);
    }

    let searchMarker = null;

    function selectPlace(mapy, mapx, title) {
        const lat = parseFloat(mapy) * 0.0000001;
        const lng = parseFloat(mapx) * 0.0000001;
        console.log('selectPlace 호출 (소수점 변환):', lat, lng, title);

        const pos = new naver.maps.LatLng(lat, lng);
        map.setCenter(pos);
        map.setZoom(16);
        fetchChargingStationsIfNeeded();

        if (searchMarker) {
            searchMarker.setMap(null);
        }

        searchMarker = new naver.maps.Marker({
            position: pos,
            map: map,
            title: title,
            icon: {
                url: 'https://maps.google.com/mapfiles/ms/icons/yellow-dot.png',
                size: new naver.maps.Size(32, 32),
                origin: new naver.maps.Point(0, 0),
                anchor: new naver.maps.Point(16, 32)
            }
        });

        document.getElementById("autocomplete-results").style.display = "none";
        document.getElementById("address-input").value = title;
    }
