package com.idirtrack.backend.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import com.idirtrack.backend.boitier.Boitier;
import com.idirtrack.backend.utils.MyResponse;

public class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetListOfSubscriptions_WhenSubscriptionsFound() {
        // Arrange
        Subscription subscription1 = createSubscription(1L, LocalDate.now(), LocalDate.now().plusMonths(3));
        Subscription subscription2 = createSubscription(2L, LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(1));
        List<Subscription> subscriptions = Arrays.asList(subscription1, subscription2);

        Pageable pageable = PageRequest.of(0, 2);
        Page<Subscription> subscriptionPage = new PageImpl<>(subscriptions, pageable, subscriptions.size());

        when(subscriptionRepository.findAll(any(Pageable.class))).thenReturn(subscriptionPage);

        // Act
        MyResponse response = subscriptionService.getListOfSubscriptions(1, 2);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getData());
        assertEquals(2, ((List<?>) response.getData()).size());
    }

    @Test
    void testGetListOfSubscriptions_WhenNoSubscriptionsFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);
        Page<Subscription> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(subscriptionRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        MyResponse response = subscriptionService.getListOfSubscriptions(1, 2);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
        assertEquals("No subscriptions found", response.getMessage());
    }

    private Subscription createSubscription(Long id, LocalDate startDate, LocalDate endDate) {
        Subscription subscription = Subscription.builder()
                .id(id)
                .startDate(Date.valueOf(startDate))
                .endDate(Date.valueOf(endDate))
                .boitier(mock(Boitier.class))
                .build();

        return subscription;
    }
}
