package com.example.fundraising.event;

import com.example.fundraising.donation.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByAcceptedIsTrue();
    List<Event> findByAcceptedIsFalse();
    List<Event> findByUserSub(String userid);
}
