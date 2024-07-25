package com.example.fundraising.donation;

import com.example.fundraising.event.Event;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "donation")
public class Donation {

    @Id
    @GeneratedValue
    private Long id;

    private double amount;

    private LocalDate donationDate;

    @ManyToOne/*(fetch = FetchType.LAZY)*/
    @JoinColumn(name = "event_id") // Name of the foreign key column in the Donation table
    @JsonIgnoreProperties("donation")
    private Event event;

    @JoinColumn(name = "user_id") // Name of the foreign key column in the Donation table
    private String userId;
    private String showName;

    public Donation() {
    }


    public Donation(double amount, LocalDate donationDate) {
        this.amount = amount;
        this.donationDate = donationDate;
    }

    public Donation(Long id, double amount, LocalDate donationDate, Event event, String userId) {
        this.id = id;
        this.amount = amount;
        this.donationDate = donationDate;
        this.event = event;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(LocalDate donationDate) {
        this.donationDate = donationDate;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String ligaUser) {
        this.userId = ligaUser;
    }
    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Donation donation = (Donation) o;
        return Double.compare(amount, donation.amount) == 0 && Objects.equals(id, donation.id) && Objects.equals(donationDate, donation.donationDate) && Objects.equals(event, donation.event) && Objects.equals(userId, donation.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, donationDate, event, userId);
    }

    @Override
    public String toString() {
        return "Donation{" +
                "id=" + id +
                ", amount=" + amount +
                ", donationDate=" + donationDate +
                ", event=" + event +
                ", userId='" + userId + '\'' +
                '}';
    }

    public String toCsv(){
        return  id +","+ amount +","+ donationDate +","+ userId;
    }
}
