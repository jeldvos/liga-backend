package com.example.fundraising;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import com.example.fundraising.donation.Donation;
import com.example.fundraising.event.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class DonationTests {
    private static final Logger logger = Logger.getLogger(DonationTests.class.getName());
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @BeforeEach
    @Transactional
    public void setupData() {
        int eventCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.event WHERE id IN (1, 2, 3)", Integer.class);
        if (eventCount == 0) {
            jdbcTemplate.execute("INSERT INTO public.event (id, accepted, doel, eind_datum, event_naam, omschrijving, organisator, persoonlijke_reden, start_datum,  totaal_bedrag, user_sub) " +
                    "VALUES (1, true, 1000.0, '2023-12-31', 'Event 1', 'Description 1', 'Organizer 1', 'Personal reason 1', '2023-01-01',  500.0, 'user1'), " +
                    "(2, false, 2000.0, '2024-06-30', 'Event 2', 'Description 2', 'Organizer 2', 'Personal reason 2', '2023-07-01', 1000.0, 'user2'), " +
                    "(3, true, 1500.0, '2023-09-30', 'Event 3', 'Description 3', 'Organizer 3', 'Personal reason 3', '2023-06-01',  750.0, 'user3')");
    
            jdbcTemplate.execute("INSERT INTO public.event_photo (event_id, photo_path) " +
                    "VALUES (1, '/path/to/photo1.jpg'), " +
                    "(1, '/path/to/photo2.jpg'), " +
                    "(2, '/path/to/photo3.jpg'), " +
                    "(3, '/path/to/photo4.jpg'), " +
                    "(3, '/path/to/photo5.jpg')");
        }
    
        jdbcTemplate.execute("INSERT INTO public.donation (id, amount, donation_date, user_id, event_id) " +
                "VALUES (1, 100.0, '2023-05-01', 'user1', 1), " +
                "(2, 200.0, '2023-05-02', 'user2', 1), " +
                "(3, 150.0, '2023-05-03', 'user3', 2), " +
                "(4, 300.0, '2023-05-04', 'user1', 2), " +
                "(5, 250.0, '2023-05-05', 'user2', 3), " +
                "(6, 500.0, '2023-05-06', 'user3', 3)");
    }

    @AfterEach
    @Transactional
    public void cleanupData() {
        jdbcTemplate.execute("DELETE FROM public.donation WHERE id IN (1, 2, 3, 4, 5,6)");
    
        jdbcTemplate.execute("DELETE FROM public.event_photo WHERE event_id IN (1, 2, 3)");
    
        jdbcTemplate.execute("DELETE FROM public.event WHERE id IN (1, 2, 3)");
    }
    
    @Test
    public void testGetDonation() {
        webTestClient.get().uri("/donation/{id}", 1)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.amount").isEqualTo(100.0)
            .jsonPath("$.userId").isEqualTo("user1");
    }
    @Test
    @Transactional
    public void testDeleteDonation() {
        Long donationIdToDelete = 2L;
        webTestClient.delete().uri("/donation/{id}", donationIdToDelete)
            .exchange()
            .expectStatus().isNoContent();

        webTestClient.get().uri("/donation/{id}", donationIdToDelete)
            .exchange()
            .expectStatus().isNotFound();
    }
    @Test
    public void testAddDonation() {
        Donation donation = new Donation(null, 90.0, LocalDate.now(),  null , "user4");
        WebTestClient.ResponseSpec responseSpec = webTestClient.post().uri("/donation")
            .contentType(APPLICATION_JSON)
            .bodyValue(donation)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().exists("Location");
        String id = responseSpec.returnResult(Void.class)
                .getResponseHeaders()
                .getFirst(HttpHeaders.LOCATION).split("/donation/")[1];
        webTestClient.get().uri("/donation/{id}", id)
                .exchange()
                .expectStatus().isOk();
        webTestClient.delete().uri("/donation/user/{userid}", "user4")
            .exchange()
            .expectStatus().isNoContent();
        webTestClient.get().uri("/donation/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }
    @Test
    @Transactional
    public void testDeleteDonationWithUser() {
        String userId = "user3";
        webTestClient.delete().uri("/donation/user/{userid}", userId)
            .exchange()
            .expectStatus().isNoContent();

        webTestClient.get().uri("/donation/history/{userId}", userId)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Donation.class)
            .hasSize(0);
    }

    @Test
    public void testDonationNotFoundException() {
        webTestClient.get().uri("/donation/{id}", 999)
            .exchange()
            .expectStatus().isNotFound();
    }
@Test
public void testGetAllDonations() {
    List<Object> donationsData = webTestClient.get().uri("/donation")
    .exchange()
    .expectStatus().isOk()
    .expectBodyList(Object.class)
    .returnResult()
    .getResponseBody();
    assertNotNull(donationsData);
    assertTrue(donationsData.size() >= 6);
}

@Test
public void testGetAllDonationsFromUser() {
    String userId = "user3";
    List<Object> donationsData = webTestClient.get().uri("/donation/history/{userId}", userId)
    .exchange()
    .expectStatus().isOk()
    .expectBodyList(Object.class)
    .returnResult()
    .getResponseBody();
    assertNotNull(donationsData);
    assertTrue(donationsData.size() == 2);}


@Test
public void testGetAcceptedEvents() {
    List<Object> eventsData = webTestClient.get().uri("/events")
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(Object.class)
        .returnResult()
        .getResponseBody();

    assertNotNull(eventsData);
    assertTrue(eventsData.size() >= 2);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    List<Event> events = eventsData.stream()
        .map(eventData -> objectMapper.convertValue(eventData, Event.class))
        .collect(Collectors.toList());

    // Check if there are events with ID 1 and 3
    assertTrue(events.stream().anyMatch(event -> event.getId() == 1));
    assertTrue(events.stream().anyMatch(event -> event.getId() == 3));
}


@Test
public void testGetUnacceptedEvents() {
    List<Object> eventsData = webTestClient.get().uri("/events/unaccepted")
    .exchange()
    .expectStatus().isOk()
    .expectBodyList(Object.class)
    .returnResult()
    .getResponseBody();
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    List<Event> events = eventsData.stream()
        .map(eventData -> objectMapper.convertValue(eventData, Event.class))
        .collect(Collectors.toList());
    assertTrue(events.size() >= 1);
    assertTrue(events.stream().anyMatch(event -> event.getId() == 2));
}

@Test
public void testGetAllEvents() {
    List<Object> eventsData = webTestClient.get().uri("/events/all")
    .exchange()
    .expectStatus().isOk()
    .expectBodyList(Object.class)
    .returnResult()
    .getResponseBody();
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    List<Event> events = eventsData.stream()
        .map(eventData -> objectMapper.convertValue(eventData, Event.class))
        .collect(Collectors.toList());
    assertTrue(events.size() >= 1);
    assertTrue(events.stream().anyMatch(event -> event.getId() == 1));
    assertTrue(events.stream().anyMatch(event -> event.getId() == 2));
    assertTrue(events.stream().anyMatch(event -> event.getId() == 3));
}

@Test
public void testGetEventById() {
    webTestClient.get().uri("/events/{id}", 3)
        .exchange()
        .expectStatus().isOk()
        .expectBody(Object.class)
        .value(eventData -> {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            Event event = objectMapper.convertValue(eventData, Event.class);
            assertEquals(3L, event.getId());
            assertEquals("Event 3", event.getEventNaam());
        });
}

@Test
public void testGetEventsFromUser() {
    String userId = "user1";
    List<Object> eventsData = webTestClient.get().uri("/events/history/{userid}", userId)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(Object.class)
        .returnResult()
        .getResponseBody();

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    List<Event> events = eventsData.stream()
        .map(eventData -> objectMapper.convertValue(eventData, Event.class))
        .collect(Collectors.toList());

    assertEquals(1, events.size());
    assertTrue(events.stream().allMatch(event -> event.getUserSub().equals(userId)));
}

@Test
public void testPostEvent() {
    Event newEvent = new Event();
    //newEvent.setId(5L);
    newEvent.setOrganisator("my Organizer");
    newEvent.setEventNaam("New Event");
    WebTestClient.ResponseSpec responseSpec = webTestClient.post().uri("/events")
        .contentType(APPLICATION_JSON)
        .bodyValue(newEvent)
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().exists("Location");

    String id = responseSpec.returnResult(Void.class)
            .getResponseHeaders()
            .getFirst(HttpHeaders.LOCATION).split("/events/")[1];

    webTestClient.get().uri("/events/{id}", id)
            .exchange()
            .expectStatus().isOk();

    webTestClient.delete().uri("/events/{id}", id)
        .exchange()
        .expectStatus().isNoContent();

    webTestClient.get().uri("/events/{id}", id)
            .exchange()
            .expectStatus().isNotFound();
}

@Test
public void testDeleteEvent() {
    webTestClient.get().uri("/events/{id}", 3)
        .exchange()
        .expectStatus().isOk()
        .expectBody(Object.class);

    webTestClient.delete().uri("/events/{id}", 3)
        .exchange()
        .expectStatus().isNoContent();

    webTestClient.get().uri("/events/{id}", 3)
            .exchange()
            .expectStatus().isNotFound();
}

@Test
public void testUpdateEvent() {
    Long eventIdToUpdate = 2L;
    Event updatedEvent = new Event();
    updatedEvent.setId(eventIdToUpdate);
    updatedEvent.setEventNaam("Updated Event");

    webTestClient.put().uri("/events/{id}", eventIdToUpdate)
        .contentType(APPLICATION_JSON)
        .bodyValue(updatedEvent)
        .exchange()
        .expectStatus().isNoContent();

    webTestClient.get().uri("/events/{id}", eventIdToUpdate)
        .exchange()
        .expectStatus().isOk()
        .expectBody(Object.class)
        .value(eventData -> {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            Event event = objectMapper.convertValue(eventData, Event.class);
            assertEquals(eventIdToUpdate, event.getId());
            assertEquals("Updated Event", event.getEventNaam());
        });
}
@Test
public void testGetNonExistentDonation() {
    Long nonExistentDonationId = 999L;
    webTestClient.get().uri("/donation/{id}", nonExistentDonationId)
        .exchange()
        .expectStatus().isNotFound();
}

@Test
public void testDeleteNonExistentDonation() {
    Long nonExistentDonationId = 999L;
    webTestClient.delete().uri("/donation/{id}", nonExistentDonationId)
        .exchange()
        .expectStatus().isNoContent();
}

@Test
public void testGetDonationsFromNonExistentUser() {
    String nonExistentUserId = "nonExistentUser";
    webTestClient.get().uri("/donation/history/{userId}", nonExistentUserId)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(Donation.class)
        .hasSize(0);
}


@Test
public void testGetNonExistentEvent() {
    Long nonExistentEventId = 999L;
    webTestClient.get().uri("/events/{id}", nonExistentEventId)
        .exchange()
        .expectStatus().isNotFound();
}

@Test
public void testGetEventsFromNonExistentUser() {
    String nonExistentUserId = "nonExistentUser";
    webTestClient.get().uri("/events/history/{userid}", nonExistentUserId)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(Event.class)
        .hasSize(0);
}


@Test
public void testUpdateNonExistentEvent() {
    Long nonExistentEventId = 999L;
    final Event[] eventArray = new Event[1];   
    
    webTestClient.get().uri("/events/{id}", 1L)
        .exchange()
        .expectStatus().isOk()
        .expectBody(Object.class)
        .value(eventData -> {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            Event event = objectMapper.convertValue(eventData, Event.class);
            eventArray[0] = event;
            assertEquals(1L, event.getId());
            assertEquals("Event 1", event.getEventNaam());
        });

    Event event1 = eventArray[0];

    webTestClient.put().uri("/events/{id}", nonExistentEventId)
        .contentType(APPLICATION_JSON)
        .bodyValue(event1)
        .exchange()
        .expectStatus().is4xxClientError();
}
}