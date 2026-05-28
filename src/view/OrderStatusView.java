package view;

import controller.MachineController;
import javax.swing.*;
import java.awt.*;

/**
 * 실시간 제조 대기 현황 화면.
 * 구성 컴포넌트 (보고서 p.5):
 * - JLabel    : 현재 제조중인 상품 표시
 * - JTextArea : OrderQueue 대기열 표시
 * 책임:
 * - StatusViewListener 를 구현한다 (implements StatusViewListener).
 * - 생성 시 MachineController.registerStatusView(this) 로 자기 자신을 리스너로 등록.
 * - onOrderStatusChanged() 콜백을 받으면 SwingUtilities.invokeLater(...) 안에서 
 * 대기열 결과를 다시 그린다.
 * 멀티스레드 안전성 (보고서 p.5):
 * - "멀티스레드 환경에서 UI 컴포넌트를 안전하게 갱신" -> 모든 UI 변경은 EDT 에서만 처리.
 * - "화면이 스스로 루프를 돌지 않고, 상태 변화가 있을 때만 컨트롤러가 화면을 리렌더링" -> polling 금지.
 */
public class OrderStatusView extends JFrame implements StatusViewListener {

    private final MachineController machineController;

    // 구성 컴포넌트 명시
    private JLabel currentProductLabel;     // 현재 제조중인 상품 표시 레이블
    private JTextArea queueDisplayArea;     // OrderQueue 대기열 표시 텍스트 영역

    public OrderStatusView(MachineController machineController) {
        this.machineController = machineController;

        // 1. JFrame 기본 디자인 세팅
        setTitle("실시간 음료 제조 모니터링 시스템");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 현재 현황창만 닫기
        setLocationRelativeTo(null); // 화면 중앙 배치
        setLayout(new BorderLayout(15, 15));

        // 2. 상단: 현재 제조 중인 상품 레이블 영역 (시각적 가독성 강조)
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(BorderFactory.createTitledBorder("⚡ 현재 제조 중인 상품"));
        
        currentProductLabel = new JLabel("대기 중...", SwingConstants.CENTER);
        currentProductLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        currentProductLabel.setForeground(new Color(0, 102, 204)); // 신뢰감을 주는 파란색 톤
        currentProductLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        northPanel.add(currentProductLabel, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        // 3. 중앙: 제조 대기열 리스트 영역 (JTextArea + 스크롤바 내장으로 가독성 가이드라인 준수)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("📋 제조 대기열 목록 (FIFO Queue)"));

        queueDisplayArea = new JTextArea();
        queueDisplayArea.setEditable(false); // 정보 은닉: 사용자가 대기열 글자를 지우지 못하게 차단
        queueDisplayArea.setFont(new Font("D2Coding", Font.PLAIN, 13));
        queueDisplayArea.setBackground(new Color(250, 250, 250));
        
        centerPanel.add(new JScrollPane(queueDisplayArea), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 4. 하단: 안내 메시지 풋터 영역
        JLabel footerLabel = new JLabel("※ 본 화면은 실시간으로 제조 상태를 수신하여 자동 갱신됩니다. (Polling 금지 준수)", SwingConstants.CENTER);
        footerLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        footerLabel.setForeground(Color.GRAY);
        footerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(footerLabel, BorderLayout.SOUTH);

        // 5. [핵심 요구사항] 생성 시 컨트롤러에 자기 자신(View)을 리스너로 등록하여 알림 창구 개설
        machineController.registerStatusView(this);

        setVisible(true);
    }

    /**
     * 책임: MakerThread(Model)가 상태를 바꾸고 컨트롤러가 콜백을 호출하면 실행되는 핵심 메서드
     * 멀티스레드 안전성 극대화: SwingUtilities.invokeLater() 필수 적용
     */
    @Override
    public void onOrderStatusChanged(String currentStatus, String queueDetails) {
        
        // [크리티컬 포인트] 작업 스레드가 아닌 Swing의 이벤트 디스패치 스레드(EDT)에서 안전하게 화면을 리렌더링하도록 스케줄링
        SwingUtilities.invokeLater(() -> {
            
            // 현재 제조중인 상품 정보 레이블 업데이트
            if (currentStatus == null || currentStatus.isEmpty()) {
                currentProductLabel.setText("모든 제조 완료 / 대기 중");
            } else {
                currentProductLabel.setText("☕ " + currentStatus + " 제조 중...");
            }

            // 대기열 목록 영역 텍스트 업데이트
            if (queueDetails == null || queueDetails.isEmpty()) {
                queueDisplayArea.setText(" 현재 대기 중인 주문이 없습니다.");
            } else {
                queueDisplayArea.setText(queueDetails);
            }
        });
    }
}
