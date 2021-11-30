package dk.msdo.caveservice.repositories;

import javax.annotation.Resource;

import dk.msdo.caveservice.common.NowStrategy;
import dk.msdo.caveservice.common.RealNowStrategy;
import dk.msdo.caveservice.domain.Direction;
import dk.msdo.caveservice.domain.Point3;
import dk.msdo.caveservice.domain.Room;
import dk.msdo.caveservice.repositories.exceptions.RoomRepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implementation for the Room Repository interface based on Redis DB
 * <p>
 * Author: Team Alpha
 */
@Repository
@ConditionalOnProperty(value = "storage.room",
        havingValue = "redisStorage")
public class RedisDBRoomRepositoryImpl implements RoomRepository {

    private static final Logger logger = LoggerFactory.getLogger(RedisDBRoomRepositoryImpl.class);
    private final String hashReference = "CaveRoom";

    @Resource(name = "roomTemplate")          // 'redisTemplate' is defined as a Bean in Configuration.java
    private RedisTemplate<String, Room> redisTemplate;
    private HashOperations<String, String, Room> roomOperations;

    /**
     * Initialize the cave - if room position (0,0,0) does not exist, create initial rooms
     */
    @Override
    public void initialize() {
        // set up hasoperations - just simpler notation.
        roomOperations = redisTemplate.opsForHash();

        // If the room at entrance does not exist it is safe to assume that other initial rooms does not exist either
        try {
            Room room = this.getRoom(p000.getPositionString());
            if (room == null) {
                room = new Room(p000_description, RoomRepository.WILL_CROWTHER_ID);
                this.addRoom(p000.getPositionString(), room);

                room = new Room(p010_description, RoomRepository.WILL_CROWTHER_ID);
                this.addRoom(p010.getPositionString(), room);

                room = new Room(p100_description, RoomRepository.WILL_CROWTHER_ID);
                this.addRoom(p100.getPositionString(), room);

                room = new Room(p_100_description, RoomRepository.WILL_CROWTHER_ID);
                this.addRoom(p_100.getPositionString(), room);

                room = new Room(p001_description, RoomRepository.WILL_CROWTHER_ID);
                this.addRoom(p001.getPositionString(), room);
            }
        } catch (RoomRepositoryException ex) {
            logger.error("method=initialize, implementationClass="
                    + this.getClass().getName()
                    + "Unable to initialize cave: " + ex);
        }
    }

    @Override
    public Room addRoom(String position, Room roomToUpdate) throws RoomRepositoryException {
        //creates one record in Redis DB if record with that Id is not present
        if (Objects.isNull(roomToUpdate.getCreatorId())) {
            throw new RoomRepositoryException("Creator ID missing" + position, HttpStatus.BAD_REQUEST);
        }

        if (!Point3.isPositionStringValid(position)) {
            logger.error("method=addRoom, implementationClass=" + this.getClass().getName() + "Getting exits for room at position: " + position);
            throw new RoomRepositoryException("invalid position string", HttpStatus.BAD_REQUEST);
        }

        // Get a room - we need to know if it is a new or existing room
        Room existingRoom = getRoom(position);

        if (Objects.isNull(existingRoom)) {

            if (p000.getPositionString().equals(position) || isNewPositionValid(position)) {
                // if room does not exist - it's new
                Room newRoom = new Room();

                // Using a UUID cause i'm lazy and it works
                newRoom.setId(position);

                // Set creation time
                NowStrategy n = new RealNowStrategy();
                newRoom.setCreationTimeISO8601(n.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

                // Set value
                newRoom.setCreatorId(roomToUpdate.getCreatorId());
                newRoom.setDescription(roomToUpdate.getDescription());

                // Store it in the repository
                roomOperations.put(hashReference, position, newRoom);
                return newRoom;
            } else {
                throw new RoomRepositoryException("Invalid room position: " + position, HttpStatus.FORBIDDEN);
            }
        } else {
            logger.error("method=addRoom" +
                    "implementationClass=" + this.getClass().getName() +
                    "Unauthorized attempt to  update room: " + position + " by user " + existingRoom.getCreatorId());
            throw new RoomRepositoryException("Not allowed to create room at position: " + position, HttpStatus.CONFLICT);
        }
    }

    /**
     * Update a room at position.
     * <p>
     * If it is a new room it is validated if it is adjacent to
     * an existing room before creation.
     * <p>
     * If it is an existing room, it is validated that it is the creator of the room
     * whom updates the room and if so, the description is updated.
     * <p>
     * Input:  position (0,0,0) and RoomRecord from which to create or update the room
     * Output: Updated RoomRecord
     ***/
    @Override
    public Room updateRoom(String position, Room roomToUpdate) throws RoomRepositoryException {
        //creates one record in Redis DB if record with that Id is not present

        if (Objects.isNull(roomToUpdate.getCreatorId())) {
            throw new RoomRepositoryException("Creator ID missing" + position, HttpStatus.BAD_REQUEST);
        }

        if (!Point3.isPositionStringValid(position)) {
            logger.error("method=updateRoom, implementationClass=" + this.getClass().getName() + "Getting exits for room at position: " + position);
            throw new RoomRepositoryException("invalid position string", HttpStatus.BAD_REQUEST);
        }

        // Get a room - we need to know if it is a new or existing room
        Room existingRoom = getRoom(position);

        if (Objects.isNull(existingRoom)) {
            logger.error("method=updateRoom, implementationClass="
                    + this.getClass().getName()
                    + "Unable to update room at position: " + position + " ");
            throw new RoomRepositoryException("Invalid room position: " + position, HttpStatus.NOT_FOUND);
        } else if (roomToUpdate.getCreatorId().equals(existingRoom.getCreatorId())) {
            // Room exists - just update description.
            existingRoom.setDescription(roomToUpdate.getDescription());
            roomOperations.put(hashReference, position, existingRoom);
            return existingRoom;
        } else {
            logger.error("method=updateRoom" +
                    "implementationClass=" + this.getClass().getName() +
                    "Unauthorized attempt to  update room: " + position + " by user " + existingRoom.getCreatorId());
            throw new RoomRepositoryException("Creator ID " + existingRoom.getCreatorId() + "does not match that of the existing room at position " + position, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Get room at position
     * <p>
     * Input:  positionString (0,0,0)
     * Output: requested room if it exists otherwise it returns null
     */
    @Override
    public Room getRoom(String position) throws RoomRepositoryException {
        logger.info("method=getRoom, implementationClass=" + this.getClass().getName() + "Getting room at position: " + position);
        if (!Point3.isPositionStringValid(position)) {
            logger.error("method=getAllExitsAtPosition, implementationClass=" + this.getClass().getName() + "Getting exits for room at position: " + position);
            throw new RoomRepositoryException("invalid position string", HttpStatus.BAD_REQUEST);
        }

        return roomOperations.get(hashReference, position);
    }

    /**
     * Returns all exits at position
     * Input:  positionString (0,0,0)
     * Output: Exits at postion in the format of ArrayList<String>
     * <p>
     * Example ["NORTH", "SOUTH", "EAST"]
     */
    @Override
    public ArrayList<String> getAllExitsAtPosition(String position) throws RoomRepositoryException {
        logger.info("method=getAllExitsAtPosition, implementationClass=" + this.getClass().getName() + "Getting exits for room at position: " + position);
        if (!Point3.isPositionStringValid(position)) {
            logger.error("method=getAllExitsAtPosition, implementationClass=" + this.getClass().getName() + "Getting exits for room at position: " + position);
            throw new RoomRepositoryException("invalid position string", HttpStatus.BAD_REQUEST);
        }
        ArrayList<String> rcList = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            Point3 tempPoint = (Point3) Point3.parseString(position).clone();
            tempPoint.translate(direction);
            Room r = roomOperations.get(hashReference, tempPoint.getPositionString());
            if (!Objects.isNull(r))
                rcList.add(direction.toString());
        }
        return rcList;
    }

    /**
     * Validates whether a given postion is an appropriate location for
     * the creation of a new room. Meaning it must be adjacent to an existing
     * room
     * <p>
     * Input:  positionString (0,0,0)
     * Output: True if it is an appropriate location
     * False if it is not
     */
    @Override
    public boolean isNewPositionValid(String position) {

        if (!Point3.isPositionStringValid(position)) return false;

        try {
            ArrayList<String> exitList = getAllExitsAtPosition(position);
            if (exitList.isEmpty()) return false;
        } catch (RoomRepositoryException e) {
            return false;
        }

        ArrayList<String> rcList = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            Point3 tempPoint = (Point3) Point3.parseString(position).clone();
            tempPoint.translate(direction);
            Room r = roomOperations.get(hashReference, tempPoint.getPositionString());
            if (Objects.isNull(r))
                return true;
        }
        return false;
    }

}
