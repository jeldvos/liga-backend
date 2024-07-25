package com.example.fundraising.donation;

import com.example.fundraising.auth0.Auth;
import com.example.fundraising.event.Event;
import com.example.fundraising.event.EventDao;
import com.example.fundraising.event.EventNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.List;
import java.util.Optional;


@RestController
public class DonationController {

    private final Logger logger = LoggerFactory.getLogger(DonationController.class);

    private final DonationDAO donationDAO;
    private final EventDao eventDao;

    public DonationController(DonationDAO donationDAO, EventDao eventDao){
        this.donationDAO = donationDAO;
        this.eventDao = eventDao;
    }

    @GetMapping("/donation")
    public List<Donation> getAllDonations(){
        return donationDAO.getAllDonations();
    }

    @GetMapping("/donation/{id}")
    public Donation getDonation(@PathVariable("id")long id){
        return this.donationDAO.getDonation(id).orElseThrow(() -> new DonationNotFoundException(id));
    }

    @GetMapping("/donation/history/{userId}")
    public List<Donation> getAllDonationsFromUser(@PathVariable("userId")String userID){
        return this.donationDAO.getAllDonationsUser(userID);
    }

    @PostMapping("/donation")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> addPost(@RequestBody Donation donation) {
        this.donationDAO.addDonation(donation);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(donation.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PostMapping("/{eventid}/donation")
    @ResponseStatus(HttpStatus.CREATED)
    public void addPost(@PathVariable("eventid") long eventId, @RequestBody Donation donation) {
        Optional<Event> event = this.eventDao.getEvent(eventId);
        if(event.isPresent()) {
            this.donationDAO.addDonation(donation, eventId);
        }
        else
            throw new EventNotFoundException(eventId);
    }

    @DeleteMapping("/donation/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllwithUser(@PathVariable long id){
        this.donationDAO.remove(id);
    }

    @DeleteMapping("/donation/user/{userid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllwithUser(@PathVariable String userid){
        this.donationDAO.removeAllUser(userid);
    }

    @GetMapping("/donation/export")
    @ResponseBody
    public void exportDonationsCSV(HttpServletResponse response) throws IOException {
        // Set response headers
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"donations.csv\"");
        // Write CSV data to response
        try (PrintWriter out = response.getWriter()) {
            // Write header
            out.println("donatie_id,bedrag,datum,user_id");
            donationDAO.getAllDonations().stream().forEach(donation -> {
                if(donation.getUserId()==null){
                    donation.setUserId("");
                }
                out.println(donation.toCsv());
            });
        }
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(DonationNotFoundException.class)
    public void handleNotFound(Exception ex) {
        logger.warn("Exception is: " + ex.getMessage());
        // return 404, when donation is not found
    }
}
