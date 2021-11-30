package dk.msdo.caveservice.repositories;

import dk.msdo.caveservice.common.NowStrategy;
import dk.msdo.caveservice.common.RealNowStrategy;
import dk.msdo.caveservice.domain.Direction;
import dk.msdo.caveservice.domain.Point3;
import dk.msdo.caveservice.domain.Room;
import dk.msdo.caveservice.repositories.exceptions.RoomRepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@Repository
@ConditionalOnProperty(value = "storage.room", havingValue = "mongoStorage")
public class MongoDBRoomRepositoryImpl implements RoomRepository {
    private static final Logger logger = LoggerFactory.getLogger(RedisDBRoomRepositoryImpl.class);

    @Resource(name = "mongoTemplate")          // 'redisTemplate' is defined as a Bean in Configuration.java
    private MongoOperations roomOperations;

    @Override
    public void initialize() {
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
    public Room updateRoom(String position, Room roomToUpdate) throws RoomRepositoryException {

        RoomRepository.validateInput(position, roomToUpdate);

        // Get a room - we need to know if it is a new or existing room
        Room existingRoom = getRoom(position);

        if (Objects.isNull(existingRoom)) {
            logger.error("method=updateRoom, implementationClass="
                    + this.getClass().getName()
                    + "Unable to update room at position: " + position + " ");
            throw new RoomRepositoryException("Invalid room position: " + position, HttpStatus.NOT_FOUND);
        } else if (roomToUpdate.getCreatorId().equals(existingRoom.getCreatorId())) {
            // Room exists - just update description.

            //existingRoom.setDescription(roomToUpdate.getDescription());

            // Update

            roomOperations.updateFirst(query(where("id").is(position)), update("description", roomToUpdate.getDescription()), Room.class);
            return getRoom(position);
        } else {
            logger.error("method=updateRoom" +
                    "implementationClass=" + this.getClass().getName() +
                    "Unauthorized attempt to  update room: " + position + " by user " + existingRoom.getCreatorId());
            throw new RoomRepositoryException("Creator ID " + existingRoom.getCreatorId() + "does not match that of the existing room at position " + position, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public Room addRoom(String position, Room roomToAdd) throws RoomRepositoryException {

        RoomRepository.validateInput(position, roomToAdd);

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
                newRoom.setCreatorId(roomToAdd.getCreatorId());
                newRoom.setDescription(roomToAdd.getDescription());

                // Store it in the repository
                roomOperations.insert(newRoom);
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

    @Override
    public Room getRoom(String position) throws RoomRepositoryException {
        logger.info("method=getRoom, implementationClass=" + this.getClass().getName() + "Getting room at position: " + position);
        if (!Point3.isPositionStringValid(position)) {
            logger.error("method=getAllExitsAtPosition, implementationClass=" + this.getClass().getName() + "Getting exits for room at position: " + position);
            throw new RoomRepositoryException("invalid position string", HttpStatus.BAD_REQUEST);
        }
        return roomOperations.findById(position, Room.class);
    }

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
            Room r = roomOperations.findById(tempPoint.getPositionString(), Room.class);
            if (!Objects.isNull(r))
                rcList.add(direction.toString());
        }
        return rcList;

    }

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
            Room r =  roomOperations.findById(tempPoint.getPositionString(), Room.class);
            if (Objects.isNull(r))
                return true;
        }
        return false;

    }
}
