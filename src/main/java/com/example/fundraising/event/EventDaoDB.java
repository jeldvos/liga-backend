package com.example.fundraising.event;

import com.example.fundraising.donation.Donation;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
//implements EventDao
public class EventDaoDB implements EventDao {

    private final EventRepository repository;

    public EventDaoDB(EventRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Event> getAllEvents() {
        return repository.findAll();
    }

    //veronderstel alle events waarvan attribute accepted true is? -> laten nakijken door Emiel
    @Override
    public List<Event> getAcceptedEvents() {
        return repository.findByAcceptedIsTrue();
    }

    @Override
    public List<Event> getUnacceptedEvents() {
        return repository.findByAcceptedIsFalse();
    }

    @Override
    public Optional<Event> getEvent(final long id) {
        return repository.findById(id);
    }

    @Override
    public void addEvent(final Event event) {
        repository.save(event);
    }

    @Override
    public void deleteEvent(final long id) {
        Optional<Event> event = repository.findById(id);
        if (event.isPresent()) {
            List<Donation> donations = event.get().getDonations();
            for (Donation dono : donations) {
                dono.setEvent(null);
            }
        }
        repository.deleteById(id);
    }

    @Override
    public void updateEvent(final long id, final Event event) {
        repository.save(event);
    }

    @Override
    public void acceptEvent(long id){
        Optional<Event> event= repository.findById(id);
        event.ifPresent(value -> {value.setAccepted(true); repository.save(value);});
    }

    @Override
    public List<Event> getEventsFromUser(String id) {
        return repository.findByUserSub(id);
    }
}
