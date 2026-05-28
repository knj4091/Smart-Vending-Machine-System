package view;

import controller.OrderController;
import javax.swing.*;
import java.awt.*;

/**
 * 결제 처리 화면.
 * 구성 컴포넌트 (보고서 p.5):
 * - JRadioButton : CASH / CARD / POINT 선택 (ButtonGroup 으로 묶기)
 * - JTextField   : 투입 금액 입력 (현금일 때 활성화)
 * - JButton      : "결제하기", "취소"
 * - JLabel       : 안내 메시지, 잔액/거스름돈 표시
 * * 책임:
 * - 결제 수단 선택 + 금액 입력 UI 제공.
 * - "결제하기" 버튼 클릭 시 executePayment() 호출 -> 선택된 결제 타입과 금액을 컨트롤러로 전달.
 * - 결제 성공 시 -> MainMenuView 의 재고 표시 갱신 + OrderStatusView 호출.
 * - 결제 실패 시 -> JOptionPane 등으로 사용자에게 사유 표시.
 */
public class PaymentView extends JDialog {
    private final MainMenuView parentView;
    private final OrderController orderController;
    private final String productId;

    // 구성 컴포넌트 명시
    private JRadioButton cashRadio;
    private JRadioButton cardRadio;
    private JRadioButton pointRadio;
    private JTextField amountField;
    private JButton payButton;
    private JButton cancelButton;
    private JLabel infoLabel;        // 안내 메시지 및 거스름돈 표시용

    public PaymentView(MainMenuView parentView, OrderController orderController, String productId) {
        super(parentView, "결제 진행 단계", true); // 모달(Modal) 창으로 제어 흐름 제한
        this.parentView = parentView;
        this.orderController = orderController;
        this.productId = productId;

        // 1. GUI 전체 레이아웃 설정 (가독성 높은 배치)
        setSize(420, 280);
        setLocationRelativeTo(parentView);
        setLayout(new BorderLayout(10, 10));

        // 2. 상단 안내 메시지 영역 (JLabel)
        infoLabel = new JLabel("결제 수단을 선택하고 투입 금액을 입력하세요.", SwingConstants.CENTER);
        infoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        infoLabel.setForeground(Color.DARK_GRAY);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        add(infoLabel, BorderLayout.NORTH);

        // 3. 중앙 입력 양식 영역 (라디오 버튼 + 텍스트 필드)
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        // 3-1. 결제 수단 선택 (ButtonGroup 바인딩)
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        radioPanel.setBorder(BorderFactory.createTitledBorder("결제 수단"));
        cashRadio = new JRadioButton("현금 (CASH)", true);
        cardRadio = new JRadioButton("카드 (CARD)");
        pointRadio = new JRadioButton("포인트 (POINT)");

        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(cashRadio);
        paymentGroup.add(cardRadio);
        paymentGroup.add(pointRadio);

        radioPanel.add(cashRadio);
        radioPanel.add(cardRadio);
        radioPanel.add(pointRadio);
        centerPanel.add(radioPanel);

        // 3-2. 금액 입력 필드 영역
        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        amountField = new JTextField(12);
        amountField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        amountPanel.add(new JLabel("투입 금액: "));
        amountPanel.add(amountField);
        centerPanel.add(amountPanel);
        
        add(centerPanel, BorderLayout.CENTER);

        // 4. 하단 제어 버튼 영역 ("결제하기", "취소")
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        payButton = new JButton("결제하기");
        cancelButton = new JButton("취소");
        
        payButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        cancelButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        
        bottomPanel.add(payButton);
        bottomPanel.add(cancelButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // 5. 조건부 컴포넌트 활성화 조작 (현금일 때만 금액 입력 활성화 라이프사이클 제어)
        cashRadio.addActionListener(e -> amountField.setEnabled(true));
        cardRadio.addActionListener(e -> {
            amountField.setText("");      // 카드/포인트는 입력창 초기화 및 비활성화
            amountField.setEnabled(false);
        });
        pointRadio.addActionListener(e -> {
            amountField.setText("");
            amountField.setEnabled(false);
        });

        // 6. 버튼 이벤트 리스너 액션 연결
        payButton.addActionListener(e -> executePayment());
        cancelButton.addActionListener(e -> dispose()); // 취소 시 모달 창 닫기

        setVisible(true);
    }

    /**
     * 책임: "결제하기" 버튼 클릭 시 호출 -> 선택된 결제 타입과 금액을 컨트롤러로 전달.
     * 캡슐화: 결제 결과 처리 로직(잔액 차감 등)은 절대 View에 두지 않는다 -> Controller / Model 이 담당.
     */
    private void executePayment() {
        String paymentType = "CASH";
        if (cardRadio.isSelected()) paymentType = "CARD";
        if (pointRadio.isSelected()) paymentType = "POINT";

        int amount = 0;
        
        // 현금 결제일 때만 금액 유효성 검사 진행
        if (cashRadio.isSelected()) {
            String amountText = amountField.getText().trim();
            if (amountText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "현금을 투입해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                amount = Integer.parseInt(amountText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "올바른 액수의 숫자만 입력 가능합니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // [캡슐화 장벽 구현] 실제 데이터 연산 처리는 Controller에 파라미터를 던져 전적으로 위임
        boolean isSuccess = orderController.processPaymentAndManufacture(paymentType, amount);

        if (isSuccess) {
            // 거스름돈이나 잔액 정보를 레이블에 친절하게 뿌려주기 (컨트롤러에서 계산한 결과 메시지를 받아올 수도 있음)
            infoLabel.setText("결제 성공! 거스름돈 처리가 진행됩니다.");
            JOptionPane.showMessageDialog(this, "결제가 정상 승인되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
            
            // 결제 성공 시 핵심 후속 조치
            parentView.refreshMenuDisplay(); // 1. MainMenuView의 재고 표시 갱신
            dispose();                       // 2. 현재 창 닫기
            
            // 3. 실시간 제조 대기 화면(OrderStatusView) 호출
            new OrderStatusView(orderController.getMachineController());
        } else {
            // 결제 실패 시 사유 예외 핸들링 표시
            JOptionPane.showMessageDialog(this, "결제 실패: 잔액 부족 또는 시스템 제한 사항을 확인하세요.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
