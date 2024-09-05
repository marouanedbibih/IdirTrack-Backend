package com.idirtrack.backend.subscription;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.idirtrack.backend.utils.MyResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    // Filter subscriptions by DateEnd From and DateEnd To
    public MyResponse filterSubscriptionsByDateEnd(String dateEndFrom, String dateEndTo, int page, int size) {
        // Convert string to LocalDate
        LocalDate dateEndFromLocalDate = LocalDate.parse(dateEndFrom);
        LocalDate dateEndToLocalDate = LocalDate.parse(dateEndTo);

        // Get page of subscriptions
        Pageable paging = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Subscription> subscriptionsPage = subscriptionRepository.filterSubscriptionsByDateEnd(dateEndFromLocalDate, dateEndToLocalDate, paging);

        if (subscriptionsPage.getContent().isEmpty()) {
            return MyResponse.builder()
                    .status(HttpStatus.NO_CONTENT)
                    .message("No subscriptions found")
                    .build();
        } else {
            // Convert list of subscriptions to list of SubscriptionTableDTO
            List<SubscriptionTableDTO> subscriptionTableDTOs = subscriptionsPage.getContent().stream()
                    .map(this::toSubscriptionTableDTO)
                    .toList();

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("totalPages", subscriptionsPage.getTotalPages());
            metadata.put("totalElements", subscriptionsPage.getTotalElements());
            metadata.put("currentPage", page);
            metadata.put("size", size);

            return MyResponse.builder()
                    .status(HttpStatus.OK)
                    .data(subscriptionTableDTOs)
                    .metadata(metadata)
                    .build();
        }
    }   

    // Service to get list of subscriptions
    public MyResponse getListOfSubscriptions(int page, int size) {
        // Get page of subscriptions
        Pageable paging = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Subscription> subscriptionsPage = subscriptionRepository.findAll(paging);

        if (subscriptionsPage.getContent().isEmpty()) {
            return MyResponse.builder()
                    .status(HttpStatus.NO_CONTENT)
                    .message("No subscriptions found")
                    .build();
        } else {
            // Convert list of subscriptions to list of SubscriptionTableDTO
            List<SubscriptionTableDTO> subscriptionTableDTOs = subscriptionsPage.getContent().stream()
                    .map(this::toSubscriptionTableDTO)
                    .toList();

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("totalPages", subscriptionsPage.getTotalPages());
            metadata.put("totalElements", subscriptionsPage.getTotalElements());
            metadata.put("currentPage", page);
            metadata.put("size", size);

            return MyResponse.builder()
                    .status(HttpStatus.OK)
                    .data(subscriptionTableDTOs)
                    .metadata(metadata)
                    .build();

        }
    }

    // Service to search for a subscription by keyword
    public MyResponse searchSubscription(String keyword, int page, int size) {
        Pageable paging = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Subscription> subscriptionsPage = subscriptionRepository.searchSubscription(keyword, paging);

        if (subscriptionsPage.getContent().isEmpty()) {
            return MyResponse.builder()
                    .status(HttpStatus.OK)
                    .message("No subscriptions found")
                    .build();
        } else {
            List<SubscriptionTableDTO> subscriptionTableDTOs = subscriptionsPage.getContent().stream()
                    .map(this::toSubscriptionTableDTO)
                    .toList();

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("totalPages", subscriptionsPage.getTotalPages());
            metadata.put("totalElements",subscriptionsPage.getTotalElements());
            metadata.put("currentPage", page);
            metadata.put("size", size);

            return MyResponse.builder()
                    .status(HttpStatus.OK)
                    .data(subscriptionTableDTOs)
                    .metadata(metadata)
                    .build();

        }
    }

    private Map<String, String> getSubscriptionDayLeft(Subscription subscription) {
        Map<String, String> result = new HashMap<>();

        // Convert java.sql.Date to java.time.LocalDate
        LocalDate endDate = subscription.getEndDate().toLocalDate();
        LocalDate currentDate = LocalDate.now();

        // Check if the subscription is already ended
        if (endDate.isBefore(currentDate) || endDate.isEqual(currentDate)) {
            result.put("timeLeft", "0 day");
            result.put("status", "left");
            return result;
        }

        // Calculate the period between the current date and end date
        Period period = Period.between(currentDate, endDate);

        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        // Build the time left string
        StringBuilder timeLeftBuilder = new StringBuilder();
        if (years > 0) {
            timeLeftBuilder.append(years).append(" year ");
        }
        if (months > 0) {
            timeLeftBuilder.append(months).append(" month ");
        }
        if (days > 0) {
            timeLeftBuilder.append(days).append(" day");
        }

        String timeLeft = timeLeftBuilder.toString().trim();

        // Determine status based on time left
        String status = "current";
        if (years == 0 && months == 0 && days <= 15) {
            status = "close";
        } else if (years == 0 && months == 0 && days == 0) {
            status = "left";
        }

        result.put("timeLeft", timeLeft);
        result.put("status", status);

        return result;
    }

    private SubscriptionTableDTO toSubscriptionTableDTO(Subscription subscription) {
        Map<String, String> timeLeft = getSubscriptionDayLeft(subscription);

        return SubscriptionTableDTO.builder()
                .id(subscription.getId())
                .boitierId(subscription.getBoitier().getId())
                .imei(subscription.getBoitier().getDevice().getImei())
                .phone(subscription.getBoitier().getSim().getPhone())
                .matricule(subscription.getBoitier().getVehicle().getMatricule())
                .clientName(subscription.getBoitier().getVehicle().getClient().getUser().getName())
                .startDate(subscription.getStartDate().toString())
                .endDate(subscription.getEndDate().toString())
                .timeLeft(timeLeft.get("timeLeft"))
                .status(timeLeft.get("status"))
                .build();
    }

    public MyResponse getStatisticsOfSubscriptionsByTimeLeft() {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        Map<String, Integer> statistics = new HashMap<>();

        // Initialize statistics
        statistics.put("current", 0);
        statistics.put("close", 0);
        statistics.put("left", 0);

        // Count the number of subscriptions in each status
        for (Subscription subscription : subscriptions) {
            Map<String, String> timeLeft = getSubscriptionDayLeft(subscription);
            String status = timeLeft.get("status");

            statistics.put(status, statistics.get(status) + 1);
        }

        // Count the total number of subscriptions
        statistics.put("total", subscriptions.size());

        return MyResponse.builder()
                .status(HttpStatus.OK)
                .data(statistics)
                .build();
    }

}
