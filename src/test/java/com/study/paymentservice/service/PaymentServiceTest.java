package com.study.paymentservice.service;

import com.study.paymentservice.client.RandomNumberClient;
import com.study.paymentservice.dto.PaymentRequestDto;
import com.study.paymentservice.dto.PaymentResponseDto;
import com.study.paymentservice.entity.Payment;
import com.study.paymentservice.exception.PaymentServiceException;
import com.study.paymentservice.kafka.event.PaymentEvent;
import com.study.paymentservice.kafka.producer.PaymentProducer;
import com.study.paymentservice.mapper.PaymentMapper;
import com.study.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private RandomNumberClient randomClient;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProducer paymentProducer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --------------------- CREATE ---------------------
    @Test
    void createPayment_shouldSaveAndSendEvent() {
        PaymentRequestDto dto = new PaymentRequestDto("order1", "user1", BigDecimal.valueOf(100.0));

        Payment paymentEntity = Payment.builder()
                .orderId(dto.getOrderId())
                .userId(dto.getUserId())
                .paymentAmount(dto.getPaymentAmount())
                .build();

        Payment savedPayment = Payment.builder()
                .id("pay1")
                .orderId(dto.getOrderId())
                .userId(dto.getUserId())
                .paymentAmount(dto.getPaymentAmount())
                .status("SUCCESS")
                .build();

        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId(savedPayment.getId());
        responseDto.setOrderId(savedPayment.getOrderId());
        responseDto.setUserId(savedPayment.getUserId());
        responseDto.setPaymentAmount(savedPayment.getPaymentAmount());
        responseDto.setStatus(savedPayment.getStatus());

        when(paymentMapper.toEntity(dto)).thenReturn(paymentEntity);
        when(randomClient.getRandomNumber()).thenReturn(2); // четное -> SUCCESS
        when(paymentRepository.save(paymentEntity)).thenReturn(savedPayment);
        when(paymentMapper.toDto(savedPayment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.create(dto);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        verify(paymentRepository, times(1)).save(paymentEntity);
        verify(paymentProducer, times(1)).send(any(PaymentEvent.class));
    }

    // --------------------- GET BY ID ---------------------
    @Test
    void getPaymentById_shouldReturnPayment() {
        Payment payment = Payment.builder()
                .id("pay1")
                .orderId("order1")
                .userId("user1")
                .paymentAmount(BigDecimal.valueOf(100.0))
                .status("CREATED")
                .build();

        when(paymentRepository.getByOrderId("order1")).thenReturn(payment);

        Payment result = paymentService.getById("order1");

        assertNotNull(result);
        assertEquals("pay1", result.getId());
    }

    @Test
    void getPaymentById_notFound_shouldThrow() {
        when(paymentRepository.getByOrderId("order1")).thenReturn(null);

        assertThrows(PaymentServiceException.class, () -> paymentService.getById("order1"));
    }

    // --------------------- GET PAYMENTS ---------------------
    @Test
    void getPayments_byUserId_shouldReturnList() {
        Payment payment = Payment.builder().id("pay1").userId("user1").build();
        when(paymentRepository.findByUserId("user1")).thenReturn(List.of(payment));

        List<Payment> result = paymentService.getPayments("user1", null, null);

        assertEquals(1, result.size());
        assertEquals("pay1", result.get(0).getId());
    }

    @Test
    void getPayments_byOrderId_shouldReturnList() {
        Payment payment = Payment.builder().id("pay1").orderId("order1").build();
        when(paymentRepository.findByOrderId("order1")).thenReturn(List.of(payment));

        List<Payment> result = paymentService.getPayments(null, "order1", null);

        assertEquals(1, result.size());
        assertEquals("pay1", result.get(0).getId());
    }

    @Test
    void getPayments_byStatus_shouldReturnList() {
        Payment payment = Payment.builder().id("pay1").status("SUCCESS").build();
        when(paymentRepository.findByStatus("SUCCESS")).thenReturn(List.of(payment));

        List<Payment> result = paymentService.getPayments(null, null, "SUCCESS");

        assertEquals(1, result.size());
        assertEquals("SUCCESS", result.get(0).getStatus());
    }

    @Test
    void getPayments_noParams_shouldThrow() {
        assertThrows(PaymentServiceException.class, () -> paymentService.getPayments(null, null, null));
    }

    // --------------------- SUMS ---------------------
    @Test
    void getUserSum_shouldReturnCorrectSum() {
        Instant from = Instant.now();
        Instant to = Instant.now();
        Payment p1 = Payment.builder().paymentAmount(BigDecimal.valueOf(50)).build();
        Payment p2 = Payment.builder().paymentAmount(BigDecimal.valueOf(30)).build();

        when(paymentRepository.findByUserIdAndTimestampBetween("user1", from, to))
                .thenReturn(List.of(p1, p2));

        BigDecimal sum = paymentService.getUserSum("user1", from, to);

        assertEquals(BigDecimal.valueOf(80), sum);
    }

    @Test
    void getTotalSum_shouldReturnCorrectSum() {
        Instant from = Instant.now();
        Instant to = Instant.now();
        Payment p1 = Payment.builder().paymentAmount(BigDecimal.valueOf(50)).build();
        Payment p2 = Payment.builder().paymentAmount(BigDecimal.valueOf(30)).build();

        when(paymentRepository.findByTimestampBetween(from, to))
                .thenReturn(List.of(p1, p2));

        BigDecimal sum = paymentService.getTotalSum(from, to);

        assertEquals(BigDecimal.valueOf(80), sum);
    }
}