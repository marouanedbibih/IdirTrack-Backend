package com.idirtrack.backend.subscription;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idirtrack.backend.boitier.Boitier;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>{

    List<Subscription> findAllByBoitier(Boitier boitier);
    //find all subscriptions by boitier id
    List<Subscription> findAllByBoitierId(Long boitierId);
    
}
