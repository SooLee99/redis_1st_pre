import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderService {

    private final Map<String, Integer> productDatabase = new ConcurrentHashMap<>();

    private final ThreadLocal<Map<String, OrderInfo>> latestOrderDatabase =
            ThreadLocal.withInitial(ConcurrentHashMap::new);

    public OrderService() {
        productDatabase.put("apple", 100);
        productDatabase.put("banana", 50);
        productDatabase.put("orange", 75);
    }

    /**
     * 주문 처리 메서드
     * @param productName 주문할 상품명
     * @param amount      주문 수량
     */
    public void order(String productName, int amount) {
        productDatabase.compute(productName, (key, currentStock) -> {

            // 해당 상품이 없으면 아무 작업도 하지 않음
            if (currentStock == null) {
                return null;
            }

            // 인위적 지연: 동시성 이슈를 유발하기 위한 지연 (테스트 목적)
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            // 재고가 충분한 경우 재고를 차감
            if (currentStock >= amount) {
                int updatedStock = currentStock - amount;

                // 주문 내역 갱신
                Map<String, OrderInfo> perThreadOrderMap = latestOrderDatabase.get();
                OrderInfo existingOrder = perThreadOrderMap.get(key);

                if (existingOrder == null) {
                    // 첫 번째 주문인 경우 새로운 OrderInfo 생성
                    perThreadOrderMap.put(key, new OrderInfo(key, amount, System.currentTimeMillis()));
                } else {
                    // 기존 주문 내역이 있는 경우 수량을 누적하고 타임스탬프 업데이트
                    perThreadOrderMap.put(key, existingOrder.incrementAmount(amount));
                }

                System.out.println(
                        "주문 완료: " + Thread.currentThread().getName() +
                                " | Order: " + amount +
                                " | Updated Stock: " + updatedStock
                );

                return updatedStock;
            } else {
                // 재고가 부족한 경우 재고를 그대로 유지
                System.out.println(
                        "재고 부족: " + Thread.currentThread().getName() +
                                " | Order: " + amount +
                                " | Insufficient Stock: " + currentStock
                );
                return currentStock;
            }
        });
    }

    /**
     * 재고 조회 메서드
     * @param productName 조회할 상품명
     * @return 현재 재고 수량
     */
    public int getStock(String productName) {
        return productDatabase.getOrDefault(productName, 0);
    }

    /**
     * (옵션) 각 스레드(=유저)가 구매한 최신 주문 정보 조회
     * @param productName 조회할 상품명
     * @return 최신 OrderInfo 객체 또는 null
     */
    public OrderInfo getLatestOrderForThread(String productName) {
        return latestOrderDatabase.get().get(productName);
    }

    /**
     * (옵션) ThreadLocal 데이터 정리 메서드
     */
    public void removeThreadLocalData() {
        latestOrderDatabase.remove();
    }
}