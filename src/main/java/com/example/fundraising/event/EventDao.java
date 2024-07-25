package com.example.fundraising.event;

import java.util.List;
import java.util.Optional;

public interface EventDao {
    // Geeft alle events terug (ook niet geaccepteerde door admin)
    List<Event> getAllEvents();
    // events van user
    List<Event> getEventsFromUser(String id);
    // Geeft alle(niet)geaccepteerde events terug
    List<Event> getAcceptedEvents();
    List<Event> getUnacceptedEvents();
    // Geeft 1 event terug
    Optional<Event> getEvent(final long id);
    // Voegt 1 event toe
    void addEvent(final Event event);
    // verwijdert event (als geen opbrengst of als niet geaccepteerd)
    void deleteEvent(final long id);
    // past event aan (bij nieuwe donaties of als datum ofzo wijzigt
    void updateEvent(final long id, final Event event);
    // event accepteren
    void acceptEvent(long id);
}
