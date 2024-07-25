package com.example.fundraising.donation;


import java.util.List;
import java.util.Optional;

public interface DonationDAO {
    List<Donation> getAllDonations();
    Optional<Donation> getDonation(final long id);
    void addDonation(final Donation donation);
    void addDonation(final Donation donation, long eventId);

    //alle donaties van een User ophalen
    List<Donation> getAllDonationsUser(final String userId);
    void remove(long id);
    void removeAllUser(String userid);
}
