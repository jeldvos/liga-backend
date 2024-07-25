package com.example.fundraising.event;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.fundraising.event.ImageStorage.StorageFileNotFoundException;
import com.example.fundraising.event.ImageStorage.StorageService;


@RestController
public class EventController {
    private final Logger logger = LoggerFactory.getLogger(EventController.class);

    private final StorageService storageService;//ok
    private final EventDao eventDao;
    public EventController(StorageService storageService, EventDao eventDao) {
        this.storageService = storageService;
        this.eventDao = eventDao;
    }
    @GetMapping("/events/unaccepted")
    public List<Event> getUnAcceptedEvents(){
        return eventDao.getUnacceptedEvents();
    }
    @GetMapping("/events")
    public List<Event> getAcceptedEvents(){
        return eventDao.getAcceptedEvents();
    }

    @GetMapping("/events/all")
    public List<Event> getAllEvents(){
        return eventDao.getAllEvents();
    }

    @GetMapping("/{id}/files")
    @ResponseBody
    public ResponseEntity<List<Path>> serveFiles(@PathVariable("id") String id) {
        // geen idee wrm to list opeens niet meer werkt na het mergen
        List<Path> resources = storageService.loadAll(id).collect(Collectors.toList());

        if (resources.isEmpty()) {
            // TODO: return default image
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @GetMapping("/events/{id}")
    public Event get(@PathVariable long id){
        return eventDao.getEvent(id).orElseThrow(()-> new EventNotFoundException(id));
    }

    @GetMapping("/events/history/{userid}")
    public List<Event> get(@PathVariable String userid){
        return eventDao.getEventsFromUser(userid);
    }
    @PostMapping(value = "/events")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> post(@RequestBody Event event) {
        eventDao.addEvent(event);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(event.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PostMapping("/events/files/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> postImage(@RequestPart("image") MultipartFile[] files, @PathVariable String id){
        File dir = storageService.createDir(id);
        if(dir!=null){
            if(files.length>0) {
                for (MultipartFile file : files) {
                    storageService.store(file, dir);
                }
            }
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    /*TODO => enigste OG code van Emiel aangepast is "storageService.deleteDir(String.valueOf(id))" doordat
    we nu geen mongoDB hebben met String als sleutel
     */
    @DeleteMapping("/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete (@PathVariable("id") long id) {
        try {
            eventDao.getEvent(id).ifPresent(el -> {
                try {
                    storageService.deleteDir(String.valueOf(id));
                    eventDao.deleteEvent(id);
                }
                catch (Exception e){
                    eventDao.deleteEvent(id);
                }
            });
        }
        catch(Exception e){
                throw new EventNotFoundException(id);
        }
    }



    @PutMapping("/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePost(@RequestBody Event event, @PathVariable("id") long id) {
        if(id != event.getId()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ID in path does not match ID in the body");
        }
        eventDao.updateEvent(id, event);
    }

    @PutMapping("/events/{id}/accept")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void acceptEvent(@PathVariable("id") long id){
        eventDao.acceptEvent(id);
    }

    @GetMapping("/{id}/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable("id") String id, @PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename, id);

        if (file == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }


    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}
