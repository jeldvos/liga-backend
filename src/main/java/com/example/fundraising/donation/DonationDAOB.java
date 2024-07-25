package com.example.fundraising.donation;

import com.example.fundraising.event.Event;
import com.example.fundraising.event.EventRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DonationDAOB implements DonationDAO {

    private final DonationRepository donationRepository;
    private final EventRepository eventRepository;

    public DonationDAOB(DonationRepository donationRepository, EventRepository eventRepository) {
        this.donationRepository = donationRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    //!!!Nakijken of genoeg is om donation zo toe te voegen -> heeft een Event attribuut!!!
    @Override
    public void addDonation(final Donation donation, final long eventId) {
        //krijgen een donation object mee, maar heeft nog geen fk user EN fk event
        //zoekt event -> wanneer evnet niet bestaat =
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("event met id = " + eventId + "bestaat niet"));
        donation.setEvent(event);
        donationRepository.save(donation);
        event.addDonation(donation);
        eventRepository.save(event);//update het event
    }

    @Override
    public Optional<Donation> getDonation(long id) {
        return donationRepository.findById(id);
    }

    @Override
    public void addDonation(Donation donation) {
        donationRepository.save(donation);
    }

    @Override
    public List<Donation> getAllDonationsUser(String user) {
        return donationRepository.findByUserId(user);
    }

    @Override
    public void remove(long id) {
        Optional<Donation> donation = donationRepository.findById(id);
        if(donation.isPresent()) {
            donationRepository.deleteById(id);
            Event ev = donation.get().getEvent();
            if(ev != null){
                ev.removeDonation(donation.get());
            }
        }
    }

    @Override
    public void removeAllUser(String userid) {
        List<Donation> toDelete = getAllDonationsUser(userid);
        for(Donation don : toDelete){
            donationRepository.deleteById(don.getId());
        }
    }

}
