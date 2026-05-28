package view;

import controller.AdminController;
import model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 관리자 시스템 화면.
 * 구성 컴포넌트 (보고서 p.5):
 * - JTextField : 보충 수량 입력, 보충 상품 ID 입력
 * - JTextArea  : sales.txt 파일 데이터 출력 영역 (매출 내역)
 * - JButton    : "재고 보충", "매출 내역 불러오기", "메인으로"
 * - JTable     : (선택) 상품별 현재 재고 표시
 * 책임:
 * - 재고 보충: 사용자가 입력한 상품 ID + 수량을 AdminController/InventoryController로 위임.
 * - 매출 조회: AdminController.loadSales() 호출 결과를 JTextArea에 표시.
 * 캡슐화:
 * - 보충 수량 입력 검증(숫자 여부, 음수 여부)은 Controller에서 책임지도록 위임.
 */
public class AdminView extends JFrame {

    private final AdminController adminController;

    // 구성 컴포넌트 명시
    private JTextField productIdInput;     // 보충할 상품 ID 입력창
    private JTextField stockCountInput;    // 보충할 수량 입력창
    private JTextArea salesLogArea;         // sales.txt 내용 출력 영역
    private JTable inventoryTable;          // 상품별 현재 재고 표시 테이블 (선택 사항 반영)
    private DefaultTableModel tableModel;

    private JButton replenishButton;       // "재고 보충"
    private JButton loadSalesButton;       // "매출 내역 불러오기"
    private JButton mainMenuButton;        // "메인으로"

    public AdminView(AdminController adminController) {
        this.adminController = adminController;

        // 1. JFrame 기본 설정 (가독성 최우선 디자인)
        setTitle("스마트 자판기 시스템 - 관리자 모드");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 관리자창만 닫기
        setLocationRelativeTo(null); // 화면 중앙 배치
        setLayout(new BorderLayout(15, 15));

        // 2. 상단 타이틀 영역
        JLabel titleLabel = new JLabel("⚙️ 자판기 관리자 컨트롤 시스템 ⚙️", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 3. 중앙 영역: 왼쪽(현재 재고 테이블) + 오른쪽(매출 내역 텍스트 영역) 쪼개기
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // 3-1. 왼쪽: 현재 재고 표시 (JTable 활용하여 친절한 시각화 달성)
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("실시간 자판기 재고 현황"));
        
        String[] columnNames = {"상품 ID", "상품명", "현재 재고"};
        tableModel = new DefaultTableModel(columnNames, 0);
        inventoryTable = new JTable(tableModel);
        leftPanel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        centerPanel.add(leftPanel);

        // 3-2. 오른쪽: sales.txt 출력 영역 (JTextArea + JScrollPane 내장으로 가독성 확보)
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("매출 기록 파일 로그 (sales.txt)"));
        salesLogArea = new JTextArea();
        salesLogArea.setEditable(false); // 수정 불가 (보안 및 정보 은닉)
        salesLogArea.setFont(new Font("D2Coding", Font.PLAIN, 12));
        rightPanel.add(new JScrollPane(salesLogArea), BorderLayout.CENTER);
        centerPanel.add(rightPanel);

        add(centerPanel, BorderLayout.CENTER);

        // 4. 하단 제어 및 입력 영역 (JPanel 중첩 활용)
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // 4-1. 입력창 행 (ID 입력 + 수량 입력)
        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        productIdInput = new JTextField(6);
        stockCountInput = new JTextField(6);
        replenishButton = new JButton("재고 보충");
        replenishButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));

        inputRow.add(new JLabel("상품 ID:"));
        inputRow.add(productIdInput);
        inputRow.add(new JLabel("보충 수량:"));
        inputRow.add(stockCountInput);
        inputRow.add(replenishButton);
        bottomPanel.add(inputRow);

        // 4-2. 기능 버튼 행 ("매출 내역 불러오기", "메인으로")
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        loadSalesButton = new JButton("매출 내역 불러오기");
        mainMenuButton = new JButton("메인으로");
        
        loadSalesButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        mainMenuButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        mainMenuButton.setBackground(new Color(230, 230, 230)); // 닫기 버튼 색상 차별화

        buttonRow.add(loadSalesButton);
        buttonRow.add(mainMenuButton);
        bottomPanel.add(buttonRow);

        add(bottomPanel, BorderLayout.SOUTH);

        // 5. 컴포넌트 액션 리스너 이벤트 연결
        replenishButton.addActionListener(e -> handleReplenish());
        loadSalesButton.addActionListener(e -> handleLoadSales());
        mainMenuButton.addActionListener(e -> dispose()); // 창 닫고 메인으로 회귀

        // 화면 켜질 때 자동으로 재고 테이블 데이터 로드
        refreshInventoryTable();
        setVisible(true);
    }

    /**
     * 책임: 사용자가 입력한 상품 ID + 수량을 가져와서 보충 처리를 컨트롤러에 위임
     */
    private void handleReplenish() {
        String productId = productIdInput.getText().trim();
        String countText = stockCountInput.getText().trim();

        if (productId.isEmpty() || countText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "상품 ID와 보충 수량을 모두 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // [캡슐화 장벽 구현] 수량 문자열 유효성 검증(숫자 여부, 음수 여부)은 컨트롤러가 전담하여 예외를 처리하도록 위임
        // 컨트롤러가 실패 시 false를 리턴하거나 예외 메시지를 주도록 구조화
        boolean isSuccess = adminController.replenishInventory(productId, countText);

        if (isSuccess) {
            JOptionPane.showMessageDialog(this, "재고가 성공적으로 보충되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            productIdInput.setText("");
            stockCountInput.setText("");
            refreshInventoryTable(); // 보충 결과 실시간 반영
        } else {
            JOptionPane.showMessageDialog(this, "재고 보충 실패: 올바르지 않은 상품 ID이거나 수량(양수) 입력 오류입니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 책임: AdminController.loadSales() 호출 결과를 받아와 JTextArea에 출력
     */
    private void handleLoadSales() {
        // 서영님이 작성할 loadSales() 또는 getSalesReportSummary() 호출 호출 중계
        String salesLog = adminController.loadSales(); 
        
        if (salesLog == null || salesLog.isEmpty()) {
            salesLogArea.setText("기록된 매출 내역 파일이 비어있거나 존재하지 않습니다.");
        } else {
            salesLogArea.setText(salesLog); // 파일 읽어온 가공 텍스트 출력 (가독성 가이드라인 만족)
        }
    }

    /**
     * 선택 사항 컴포넌트 제어: 상품 테이블 뷰 갱신
     */
    private void refreshInventoryTable() {
        tableModel.setRowCount(0); // 기존 데이터 지우기
        
        // 컨트롤러를 거쳐 다형성 Product 타입 리스트 일괄 수집
        List<Product> productList = adminController.getAllProducts();
        if (productList != null) {
            for (Product p : productList) {
                tableModel.addRow(new Object[]{p.getId(), p.getName(), p.getStock() + "개"});
            }
        }
    }
}
