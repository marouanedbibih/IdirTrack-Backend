package com.idirtrack.backend.subscription;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.idirtrack.backend.boitier.Boitier;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findAllByBoitier(Boitier boitier);

    // find all subscriptions by boitier id
    List<Subscription> findAllByBoitierId(Long boitierId);

    // Search subscriptions by keyword
    @Query("SELECT s FROM Subscription s WHERE " +
            "s.boitier.device.imei LIKE %:keyword% OR " +
            "s.boitier.sim.phone LIKE %:keyword% OR " +
            "s.boitier.vehicle.client.user.name LIKE %:keyword% OR " +
            "s.boitier.vehicle.matricule LIKE %:keyword%")
    Page<Subscription> searchSubscription(@Param("keyword") String keyword, Pageable paging);

}
