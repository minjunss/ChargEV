
    let map;
    let lastBounds = null;
    let debounceTimer = null;
    let openInfoWindow = null;
    let markers = []; // 마커들을 저장할 배열

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
                });
            })
            .catch(console.error);
    }

    function loadDetail(statId) {
        // CSRF 토큰 가져오기
        const csrf = getCsrfTokenAndHeader();

        fetch(`/api/chargingStation/detail?statId=${statId}`, {
            method: 'GET',
            headers: {
                [csrf.header]: csrf.token // CSRF 토큰 추가
            }
        })
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
                const container = document.getElementById("station-detail");
                container.innerHTML = `<h2>충전기 상세 정보</h2>${html}`;
                container.style.display = "block";
                container.scrollIntoView({ behavior: 'smooth', block: 'start' });
            })
            .catch(err => alert("상세정보를 불러오는 데 실패했습니다."));
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

    document.addEventListener('DOMContentLoaded', () => {
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
