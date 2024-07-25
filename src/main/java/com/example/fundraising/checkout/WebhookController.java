package com.example.fundraising.checkout;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.fundraising.donation.Donation;
import com.example.fundraising.donation.DonationDAO;
import com.google.gson.JsonSyntaxException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;

@RestController
public class WebhookController {
    private final DonationDAO donationDAO;

    public WebhookController(DonationDAO donationDAO) {
        this.donationDAO = donationDAO;
    }

    //a.d.h.v. deze methode eventId instellen
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        Event event = null;
        try {
            event = ApiResource.GSON.fromJson(payload, Event.class);
        } catch (JsonSyntaxException e) {
            return ResponseEntity.badRequest().body("");
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            return ResponseEntity.badRequest().body("");
        }
        switch (event.getType()) {
            case "checkout.session.completed":
                Session session = (Session) stripeObject;
                double amount = ((double) session.getAmountTotal() /100);
                //created = date -> type = Unix timestamp
                long unixTimestamp = session.getCreated();
                // event en user id afleiden uit clientReferenceId
                String eventAndUser = session.getClientReferenceId();
                String[] splitted = eventAndUser.split("__");
                // variabelen invullen
                String string_eventid = null;
                String string_userId = null;
                String string_donation_name = null;
                if(splitted.length==1){
                    string_eventid = splitted[0];
                }
                if(splitted.length==2){
                    string_eventid = splitted[0];
                    string_userId = splitted[1];
                }
                if(splitted.length==3) {
                    string_eventid = splitted[0];
                    string_userId = splitted[1];
                    string_donation_name = splitted[2];
                }
                Instant instant = Instant.ofEpochSecond(unixTimestamp);
                LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                Donation testDonation = new Donation(amount, localDate);
                // als eventid aanwezig -> nr long
                Long eventid = null;
                if(string_eventid!= null && !string_eventid.equals("")){
                    eventid = Long.parseLong(string_eventid);
                }
                // als userid aanwezig -> "_" -> "|"
                if(string_userId!= null && !string_userId.equals("")){
                    String userid = string_userId.replace("_","|");
                    testDonation.setUserId(userid);
                }
                if(string_donation_name!= null && !string_donation_name.equals("")){
                    testDonation.setShowName(string_donation_name);
                }
                if(eventid != null){
                    donationDAO.addDonation(testDonation, eventid);
                }
                else {
                    donationDAO.addDonation(testDonation);
                }
                return ResponseEntity.ok().body("{\"status\": \"success\", \"amount\": " + amount);
            case "charge.dispute.created":
                Charge failedPaymentIntent = (Charge) stripeObject;

                return ResponseEntity.ok().body("{\"status\": \"failed\"}");
            default:
                return ResponseEntity.ok().body("");
        }
    }

}