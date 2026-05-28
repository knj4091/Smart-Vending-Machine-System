package view;

import model.Order;

/**
 * // view/StatusViewListener.java
 * // MakerThread -> View 비동기 알림을 위한 콜백 인터페이스.
 * // 배경 (보고서 p.5 "MakerThread 와의 비동기 통신을 위해 자기 자신(View)을 리스너로 등록"):
 * //  - MakerThread 는 별도 스레드에서 돌기 때문에, 제조 상태가 바뀌었음을 알릴 수단이 필요하다.
 * //  - 화면이 직접 polling 하지 않고, 상태 변화가 있을 때만 컨트롤러가 View 를 다시 그리도록 한다.
 */
public interface StatusViewListener {

    /**
     * 큐 또는 현재 제조중인 주문이 바뀌었을 때 호출됨.
     * @param currentStatus 현재 제조 중인 상품 정보 및 남은 시간 텍스트
     * @param queueDetails 대기열에 남아있는 주문 목록 텍스트
     */
    void onOrderStatusChanged(String currentStatus, String queueDetails);
}
