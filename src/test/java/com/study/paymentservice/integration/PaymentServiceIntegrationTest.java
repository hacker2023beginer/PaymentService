package com.study.paymentservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.study.paymentservice.client.RandomNumberClient;
import com.study.paymentservice.entity.Payment;
import com.study.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class PaymentServiceIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

    @Container
    static KafkaContainer kafka = new KafkaContainer("7.5.0");

    static WireMockServer wireMockServer = new WireMockServer(8089);

    @Autowired
    private com.study.paymentservice.service.PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @DynamicPropertySource
    static void setupProps(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("random.service.url", () -> "http://localhost:8089");
    }

    @BeforeAll
    static void startWiremock() {
        wireMockServer.start();
    }

    @AfterAll
    static void stopWiremock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void cleanDb() {
        paymentRepository.deleteAll();
    }

    @Autowired
    private RandomNumberClient randomNumberClient;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    void getRandomNumber_shouldReturnMockedValue() {
        Mockito.when(restTemplate.getForObject(
                Mockito.anyString(), Mockito.eq(int[].class)
        )).thenReturn(new int[]{42});

        int result = randomNumberClient.getRandomNumber();

        assertEquals(42, result);
    }

    @Test
    void getPayments_byUserId_shouldReturnData() {
        Payment payment = Payment.builder()
                .orderId("order1")
                .userId("user1")
                .paymentAmount(BigDecimal.TEN)
                .status("SUCCESS")
                .build();

        paymentRepository.save(payment);

        List<Payment> result = paymentService.getPayments("user1", null, null);

        assertEquals(1, result.size());
    }

    @Test
    void getUserSum_shouldCalculateCorrectly() {
        Instant now = Instant.now();

        Payment p1 = Payment.builder()
                .userId("user1")
                .paymentAmount(BigDecimal.valueOf(50))
                .timestamp(now.minusSeconds(60))
                .build();

        Payment p2 = Payment.builder()
                .userId("user1")
                .paymentAmount(BigDecimal.valueOf(30))
                .timestamp(now.minusSeconds(30))
                .build();

        paymentRepository.saveAll(List.of(p1, p2));

        var sum = paymentService.getUserSum(
                "user1",
                now.minusSeconds(120),
                now
        );

        assertEquals(BigDecimal.valueOf(80), sum);
    }
}