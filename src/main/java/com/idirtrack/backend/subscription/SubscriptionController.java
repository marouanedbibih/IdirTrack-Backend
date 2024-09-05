package com.idirtrack.backend.subscription;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idirtrack.backend.utils.MyResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // Endpoint to filter subscriptions by DateEnd From and DateEnd To
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/api/v1/subscriptions/filter")
    public ResponseEntity<MyResponse> filterSubscriptionsByDateEnd(
            @RequestParam(value = "dateEndFrom") String dateEndFrom,
            @RequestParam(value = "dateEndTo") String dateEndTo,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {

        MyResponse response = subscriptionService.filterSubscriptionsByDateEnd(dateEndFrom, dateEndTo, page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Endpoint to get list of subscriptions
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/api/v1/subscriptions")
    public ResponseEntity<MyResponse> getListOfSubscriptions(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {

        MyResponse response = subscriptionService.getListOfSubscriptions(page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Endpoint to search for a subscription by keyword
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/api/v1/subscriptions/search")
    public ResponseEntity<MyResponse> searchSubscription(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {

        MyResponse response = subscriptionService.searchSubscription(keyword, page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Endpoint to get statistics of subscriptions
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/api/v1/subscriptions/statistics/total-by-time-left")
    public ResponseEntity<MyResponse> getStatisticsOfSubscriptionsByTimeLeft() {

        MyResponse response = subscriptionService.getStatisticsOfSubscriptionsByTimeLeft();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
