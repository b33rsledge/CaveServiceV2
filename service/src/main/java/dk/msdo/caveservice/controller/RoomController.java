package dk.msdo.caveservice.controller;

import com.google.gson.Gson;
import dk.msdo.caveservice.domain.Room;
import dk.msdo.caveservice.repositories.RoomRepository;
import dk.msdo.caveservice.repositories.exceptions.RoomRepositoryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;
/**
 * Rest controller for the Room part of the CaveService
 *
 * Author: Team Alpha
 */
@RestController
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
        this.roomRepository.initialize();
    }

    /**
     * Get all exits at position
     *
     * EXAMPLE:
     *
     * GET http://localhost:8080/v2/room/{1,1,1}/exits
     */
    @GetMapping(path="/v2/room/{position}/exits", produces = "application/json")
    public ResponseEntity <String> getRoomExitsAtPosition(@PathVariable(value = "position") String position   )
    {
        ArrayList<String> exitList;
        try {
            exitList = roomRepository.getAllExitsAtPosition(position);
        } catch (RoomRepositoryException e) {
            return new ResponseEntity<>( "Bad request",e.error);
        }
        return ResponseEntity.ok().body(exitList.toString());
    }

    /**
     * Get room at position
     *
     * EXAMPLE:
     *
     * GET http://localhost:8080/v2/room/{1,1,1}
     */
    @GetMapping(path="/v2/room/{position}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity <String> getV2(@PathVariable(value = "position") String position   )
    {
        try {
            Room room = roomRepository.getRoom(position);
            if (Objects.isNull(room)) {
                return new ResponseEntity<>("Room not found", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new Gson().toJson(room), HttpStatus.OK);
        } catch (RoomRepositoryException e) {
            return new ResponseEntity<>("Invalid position", HttpStatus.BAD_REQUEST);
        }
    }
    /**
     * Post room at position
     *
     * EXAMPLE:
     *
     * POST http://localhost:8080/v2/room/(1,0,0)
     * Content-Type: application/json
     * Accept: application/json
     *
     * {
     *    "description" : "Yppiekieae",
     *    "creatorId" : "PHG"
     * }
     *
     */
    @PutMapping(path="/v2/room/{position}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity <String> PutV2(@PathVariable String position, @RequestBody Room room) {

        try {
            room = roomRepository.updateRoom(position, room);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();

            return ResponseEntity.ok()
                    .header("location", location.toString())
                    .body(new Gson().toJson(room));
        } catch (RoomRepositoryException e) {
            return new ResponseEntity<>( e.toString() , e.error);
        }
    }
    /**
     * Post room at position
     *
     * EXAMPLE:
     *
     * PUT http://localhost:8080/v2/room/(1,0,0)
     * Content-Type: application/json
     * Accept: application/json
     *
     * {
     *    "description" : "Yppiekieae",
     *    "creatorId" : "PHG"
     * }
     *
     */
    @PostMapping (path="/v2/room/{position}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity <String> PostV2(@PathVariable String position, @RequestBody Room room) {
        try {
            room = roomRepository.addRoom(position, room);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
            return ResponseEntity.created(location).body(new Gson().toJson(room));
        } catch (RoomRepositoryException e) {
            return new ResponseEntity<>( e.toString() , e.error);
        }
    }
}
