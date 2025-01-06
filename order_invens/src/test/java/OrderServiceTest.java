import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {
    private final OrderService service = new OrderService();

    /**
     * 테스트 후 ThreadLocal 데이터를 정리하여 쓰레드 풀 환경에서의 데이터 혼동을 방지합니다.
     */
    @AfterEach
    void tearDown() {
        service.removeThreadLocalData();
    }

    /**
     * @throws InterruptedException 스레드 대기 중 인터럽트 발생 시
     */
    @Test
    void testConcurrentOrdersShouldLeaveCorrectStock() throws InterruptedException {
        String productName = "apple";
        int initialStock = service.getStock(productName);

        int orderAmount = 8;
        int threadCount = 100;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 각 스레드에서 주문을 수행하는 작업 생성
        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    service.order(productName, orderAmount);
                } finally {
                    latch.countDown(); // 작업 완료 후 카운트 감소
                }
            });
        }

        // 모든 스레드가 작업을 완료할 때까지 대기
        latch.await();
        executor.shutdown();

        // 최종 재고 값 확인
        int expectedStock = initialStock % orderAmount;
        int actualStock  = service.getStock(productName);

        // 테스트 결과를 로그로 출력합니다.
        System.out.println("==============================================");
        System.out.println("초기 Stock : " + initialStock);
        System.out.println("기대 Stock : " + expectedStock);
        System.out.println("결과 Stock : " + actualStock);
        System.out.println("==============================================");

        // 동시성 이슈로 인해 재고가 맞지 않는 경우를 확인
        assertEquals(expectedStock, actualStock, "재고 불일치 발생!");
    }
}