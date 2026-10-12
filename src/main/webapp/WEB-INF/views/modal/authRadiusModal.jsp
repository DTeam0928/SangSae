<!-- 위치 인증 반경 선택 모달 -->
<div class="modal-bg" id="authRadiusModal"
     onclick="App.quest.closeModalOutside(event, 'authRadiusModal')">

    <div class="modal">
        <div class="modal-head">
            <h2 class="title">📍 위치 인증 반경 설정</h2>
            <button type="button" class="close"
                    onclick="App.quest.closeModal('authRadiusModal')">×</button>
        </div>

        <div style="margin-top: 20px;">
            <div style="display:flex; justify-content:space-between;
	                        align-items:center; margin-bottom:12px;">

                <label for="authRadiusM" class="subtitle">
                    GPS 인증 허용 반경
                </label>

                <strong id="authRadiusValue">300m</strong>
            </div>

            <input type="range"
                   id="authRadiusM"
                   min="100"
                   max="1000"
                   step="50"
                   value="300"
                   style="width:100%; cursor:pointer;"
                   oninput="
	                       document.getElementById('authRadiusValue').textContent =
	                           this.value + 'm';
	                       document.getElementById('authRadiusDescription').textContent =
	                           '목표 장소에서 ' + this.value +
	                           'm 이내에 도착하면 GPS 인증이 가능합니다.';
	                   ">

            <div style="display:flex; justify-content:space-between;
	                        font-size:12px; margin-top:6px;">
                <span>100m</span>
                <span>1000m</span>
            </div>

            <p class="desc" id="authRadiusDescription"
               style="margin-top:14px;">
                목표 장소에서 300m 이내에 도착하면 GPS 인증이 가능합니다.
            </p>

            <button type="button"
                    class="btn btn-primary"
                    style="width:100%; margin-top:20px;"
                    onclick="App.quest.confirmAuthRadius()">
                이 반경으로 일정 추가하기
            </button>
        </div>
    </div>
</div>