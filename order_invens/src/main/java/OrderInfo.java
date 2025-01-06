/**
 * OrderInfo 클래스는 주문 정보를 담는 데이터 객체입니다.
 * - 불변성을 유지하며, 주문 수량을 누적할 때 새로운 인스턴스를 생성합니다.
 */
public class OrderInfo {
    private final String productName;
    private final int amount;
    private final long timestamp;

    /**
     * 생성자: 주문 정보를 초기화합니다.
     *
     * @param productName 주문한 상품명
     * @param amount      주문 수량
     * @param timestamp   주문 시각 (밀리초 단위)
     */
    public OrderInfo(String productName, int amount, long timestamp) {
        this.productName = productName;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    /**
     * 주문 수량을 누적하고 새로운 OrderInfo 객체를 반환합니다.
     *
     * @param additionalAmount 추가 주문 수량
     * @return 새로운 OrderInfo 객체
     */
    public OrderInfo incrementAmount(int additionalAmount) {
        return new OrderInfo(this.productName, this.amount + additionalAmount, System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return "OrderInfo{" +
                "productName='" + productName + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}
