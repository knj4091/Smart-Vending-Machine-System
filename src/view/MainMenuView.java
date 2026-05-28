package view;

import controller.OrderController;
import controller.AdminController;
import model.Product;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 메인 메뉴 & 상품 선택 화면. 보고서 p.5 의 ProductSelectionView 와 동일 역할.
 * * 책임:
 * - 화면을 렌더링한다 (JFrame / JPanel 구성).
 * - 사용자의 입력(상품 ID, 버튼 클릭)을 잡아 컨트롤러에 위임한다 — 직접 Model 을 만지지 않는다.
 * - refreshMenuDisplay() : InventoryController 또는 MachineController 에서 호출하면 현재 재고를 다시 읽어 JTextArea 를 갱신한다.
 */
public class MainMenuView extends JFrame {
    
    // 단방향 흐름: View는 Controller의 참조만 가진다 (생성자 주입).
    private final OrderController orderController;
    private final AdminController adminController;

    // 구성 컴포넌트 (보고서 명시)
    private JTextArea menuDisplayArea;      // 메뉴(상품 리스트) 표시
    private JTextField productIdInput;      // 상품 ID 입력
    private JButton orderButton;            // "주문하기"
    private JButton adminButton;            // "관리자 모드 진입"

    public MainMenuView(OrderController orderController, AdminController adminController) {
        this.orderController = orderController;
        this.adminController = adminController;

        // 1. JFrame 기본 설정 (가독성 높은 친절한 코딩)
        setTitle("스마트 웰니스 자판기 시스템");
        setSize(550, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 화면 중앙에 배치
        setLayout(new BorderLayout(10, 10));

        // 2. 상단 타이틀 영역
        JLabel titleLabel = new JLabel("🥤 SMART VENDING MACHINE 🥤", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 3. 중앙 메뉴 표시 영역 (JTextArea + JScrollPane 내장으로 가독성 확보)
        menuDisplayArea = new JTextArea();
        menuDisplayArea.setEditable(false); // 수정 불가 설정 (정보 은닉)
        menuDisplayArea.setFont(new Font("D2Coding", Font.PLAIN, 14));
        menuDisplayArea.setBackground(new Color(245, 245, 245));
        add(new JScrollPane(menuDisplayArea), BorderLayout.CENTER);

        // 4. 하단 입력 및 버튼 제어 영역 (JPanel 활용)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        productIdInput = new JTextField(8);
        productIdInput.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        
        orderButton = new JButton("주문하기");
        adminButton = new JButton("관리자 모드 진입");
        
        // 버튼 디자인 살짝 추가 (친절한 코딩 가이드라인 적용)
        orderButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        adminButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));

        bottomPanel.add(new JLabel("상품 ID 입력:"));
        bottomPanel.add(productIdInput);
        bottomPanel.add(orderButton);
        bottomPanel.add(adminButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // 5. 컴포넌트 이벤트 리스너 연결
        orderButton.addActionListener(e -> handleOrderAction());
        adminButton.addActionListener(e -> handleAdminAction());

        // 초기 화면 구동 시 메뉴판 자동 로드
        refreshMenuDisplay();
        setVisible(true);
    }

    /**
     * 책임: InventoryController 또는 MachineController 에서 호출하면 
     * 현재 재고를 다시 읽어 JTextArea 를 갱신한다.
     * * 다형성 활용: "부모 타입(Product) 으로 리스트를 일괄 수집"
     */
    public void refreshMenuDisplay() {
        menuDisplayArea.setText(""); // 기존 텍스트 초기화
        menuDisplayArea.append(" ====================================================\n");
        menuDisplayArea.append("   상품 ID\t상품 이름\t\t가격\t재고 상태\n");
        menuDisplayArea.append(" ====================================================\n");

        // 서영님이 작성할 OrderController를 통해 부모 타입(Product) 리스트 일괄 수집
        List<Product> products = orderController.getAvailableProducts();
        
        if (products == null || products.isEmpty()) {
            menuDisplayArea.append("   현재 자판기에 등록된 상품이 없습니다.\n");
        } else {
            for (Product p : products) {
                String stockText = p.getStock() > 0 ? p.getStock() + "개" : "품절";
                menuDisplayArea.append(String.format("   [%s]\t%-15s\t%d원\t%s\n", 
                        p.getId(), p.getName(), p.getPrice(), stockText));
            }
        }
        menuDisplayArea.append(" ====================================================\n");
    }

    /**
     * 캡슐화 장벽: "뷰가 직접 연산하지 않고 컨트롤러에 파라미터만 안전하게 전달"
     */
    private void handleOrderAction() {
        String productId = productIdInput.getText().trim();
        
        if (productId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "상품 ID를 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 버튼 핸들러는 서영님이 제공할 유효성 및 주문 생성 컨트롤러 메서드만 호출
        boolean isOrderCreated = orderController.createOrder(productId, "GuestUser");
        
        if (isOrderCreated) {
            // 주문 성공 시 입력창을 비우고 결제 화면(PaymentView) 팝업으로 이동
            productIdInput.setText("");
            new PaymentView(this, orderController, productId);
        } else {
            JOptionPane.showMessageDialog(this, "존재하지 않거나 품절된 상품입니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 관리자 모드 진입 액션 핸들러
     */
    private void handleAdminAction() {
        // 관리자 뷰(AdminView)를 호출하며 관리자 컨트롤러를 주입
        new AdminView(adminController);
    }
}
