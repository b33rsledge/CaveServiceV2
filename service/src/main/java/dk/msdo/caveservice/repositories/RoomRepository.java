package dk.msdo.caveservice.repositories;
import dk.msdo.caveservice.domain.Point3;
import dk.msdo.caveservice.domain.Room;
import dk.msdo.caveservice.repositories.exceptions.RoomRepositoryException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Interface for the Room Repository
 *
 * Author: Team Alpha
 */
public interface RoomRepository {
    String WILL_CROWTHER_ID = "0";

    // Default values for the 5 rooms, which must be initialized by implementation classes
     Point3 p000 = new Point3(0, 0, 0);
     String p000_description =  "You are standing at the end of a road before a small brick building.";
     Point3 p010 = new Point3(0, 1, 0);
     String p010_description = "You are in open forest, with a deep valley to one side.";
     Point3 p100 = new Point3(1, 0, 0);
     String p100_description = "You are inside a building, a well house for a large spring.";
     Point3 p_100 = new Point3(-1, 0, 0);
     String p_100_description = "You have walked up a hill, still in the forest.";
     Point3 p001 = new Point3(0, 0, 1);
     String p001_description = "You are in the top of a tall tree, at the end of a road.";

    /**
     * Initialize the cave - if room position (0,0,0) does not exist, create initial rooms
     */
    void initialize() ;

    /**
     * Update a room at position.
     *
     * If it is a new room it is validated if it is adjacent to
     * an existing room before creation.
     *
     * If it is an existing room, it is validated that it is the creator of the room
     * whom updates the room and if so, the description is updated.
     *
     * Input:  position (0,0,0) and RoomRecord from which to create or update the room
     * Output: Updated RoomRecord
     ***/
    Room updateRoom(String position, Room roomToUpdate) throws RoomRepositoryException;

    /**
     * Update a room at position.
     *
     * If it is a new room it is validated if it is adjacent to
     * an existing room before creation.
     *
     * If it is an existing room, it is validated that it is the creator of the room
     * whom updates the room and if so, the description is updated.
     *
     * Input:  position (0,0,0) and RoomRecord from which to create or update the room
     * Output: Updated RoomRecord
     ***/
    Room addRoom(String position, Room roomToAdd) throws RoomRepositoryException;

    /**
     * Get room at position
     *
     * Input:  positionString (0,0,0)
     * Output: requested room if it exists otherwise it returns null
     */
    Room getRoom(String position) throws RoomRepositoryException;

    /**
     * Returns all exits at position
     *   Input:  positionString (0,0,0)
     *   Output: Exits at postion in the format of ArrayList<String>
     *
     *           Example ["NORTH", "SOUTH", "EAST"]
     */
    ArrayList<String> getAllExitsAtPosition(String position) throws RoomRepositoryException;

    /**
     * Checks whether a new postion is valid
     *   Input:  positionString (0,0,0)
     *   Output: true/false
     */
    boolean isNewPositionValid(String position);

    static void validateInput(String position, Room roomToValidate) throws RoomRepositoryException {
        if (Objects.isNull(roomToValidate.getCreatorId())) {
            throw new RoomRepositoryException("Creator ID missing" + position, HttpStatus.BAD_REQUEST);
        }

        if (!Point3.isPositionStringValid(position)) {
            throw new RoomRepositoryException("invalid position string", HttpStatus.BAD_REQUEST);
        }
    }

}