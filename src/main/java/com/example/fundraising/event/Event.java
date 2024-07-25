package com.example.fundraising.event;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.fundraising.donation.Donation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue
    private Long id;

    private String organisator;
    private String eventNaam;
    private LocalDate startDatum;
    private LocalDate eindDatum;
    private String omschrijving;
    private String persoonlijkeReden;
    private String userSub;
    private double totaalBedrag;
    private double doel;
    @OneToMany(mappedBy = "event")
    @JsonIgnoreProperties("event")
    private List<Donation> donations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "eventPhoto", joinColumns = @JoinColumn(name = "eventId"))
    @Column(name = "photoPath")
    private List<String> fotoPath = new ArrayList<>();

    private boolean accepted;

    public Event() {
        this.accepted = false;
        this.totaalBedrag = 0;
    }
    public Event(long id) {
        this.id = id;
        this.accepted = false;
        this.totaalBedrag = 0;
    }


    public double getTotaalBedrag() {
        return totaalBedrag;
    }

    public void setTotaalBedrag(double totaalBedrag) {
        this.totaalBedrag = totaalBedrag;
    }
    public void addBedrag(double bedrag){
        this.totaalBedrag += bedrag;
    }
    public List<Donation> getDonations() {
        return donations;
    }

    public void setDonations(List<Donation> donations) {
        this.donations = donations;
    }

    public void addDonation(Donation donation) {
        donations.add(donation);
        addBedrag(donation.getAmount());
        donation.setEvent(this);
    }

    public void removeDonation(Donation donation) {
        donations.remove(donation);
        donation.setEvent(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrganisator() {
        return organisator;
    }

    public void setOrganisator(String organisator) {
        this.organisator = organisator;
    }

    public String getEventNaam() {
        return eventNaam;
    }

    public void setEventNaam(String eventName) {
        this.eventNaam = eventName;
    }

    public LocalDate getStartDatum() {
        return startDatum;
    }

    public void setStartDatum(LocalDate startDate) {
        this.startDatum = startDate;
    }

    public LocalDate getEindDatum() {
        return eindDatum;
    }

    public void setEindDatum(LocalDate endDate) {
        this.eindDatum = endDate;
    }

    public String getOmschrijving() {
        return omschrijving;
    }

    public void setOmschrijving(String description) {
        this.omschrijving = description;
    }

    public String getPersoonlijkeReden() {
        return persoonlijkeReden;
    }

    public void setPersoonlijkeReden(String personalReason) {
        this.persoonlijkeReden = personalReason;
    }

    public List<String> getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(List<String> photoPaths) {
        this.fotoPath = photoPaths;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getUserSub() {
        return userSub;
    }

    public void setUserSub(String userSub) {
        this.userSub = userSub;
    }

    public double getDoel() {
        return doel;
    }

    public void setDoel(double doel) {
        this.doel = doel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return accepted == event.accepted && Objects.equals(id, event.id) && Objects.equals(organisator, event.organisator) && Objects.equals(eventNaam, event.eventNaam) && Objects.equals(startDatum, event.startDatum) && Objects.equals(eindDatum, event.eindDatum) && Objects.equals(omschrijving, event.omschrijving) && Objects.equals(persoonlijkeReden, event.persoonlijkeReden) && Objects.equals(donations, event.donations) && Objects.equals(fotoPath, event.fotoPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, organisator, eventNaam, startDatum, eindDatum, omschrijving, persoonlijkeReden, donations, fotoPath, accepted);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", organisator='" + organisator + '\'' +
                ", eventName='" + eventNaam + '\'' +
                ", startDate=" + startDatum +
                ", endDate=" + eindDatum +
                ", description='" + omschrijving + '\'' +
                ", personalReason='" + persoonlijkeReden + '\'' +
                ", donations=" + donations +
                ", photoPaths=" + fotoPath +
                ", accepted=" + accepted +
                '}';
    }
}
