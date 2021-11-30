package dk.msdo.repository;

import dk.msdo.caveservice.domain.Direction;
import dk.msdo.caveservice.domain.Point3;
import dk.msdo.caveservice.domain.Room;
import dk.msdo.caveservice.repositories.RoomRepository;
import dk.msdo.caveservice.repositories.exceptions.RoomRepositoryException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;

import static dk.msdo.caveservice.repositories.RoomRepository.p000;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * TDD of RoomRepository interface and
 * driving the MemoryRepository implementation.
 *
 * @author Team Alpha, Aarhus University.
 */
class RoomRepositoryTests {

    RoomRepository storage;

    // Unconnected new room position
    private final Point3 p273 = new Point3(2, 7, 3);
    private final String p273_description = "This room must never be made.";
    private final String p273_creatorId = "BlackHat";

    // Connected new room position
    private final Point3 p200 = new Point3(2, 0, 0);
    private final String p200_description = "Valid new room position.";
    private final String p200_creatorId = "Højbjerg";

    void setup(RoomRepository storage) {
        this.storage = storage;
        this.storage.initialize();
    }


    public void shouldReadInitialRoomsInStorage() throws RoomRepositoryException {
        Room room = this.storage.getRoom(p000.getPositionString());
        assertThat(room.getDescription(), is(RoomRepository.p000_description));
        assertThat(room.getCreatorId(), is(RoomRepository.WILL_CROWTHER_ID));

        room = this.storage.getRoom(RoomRepository.p_100.getPositionString());
        assertThat(room.getDescription(), containsString(RoomRepository.p_100_description));
        assertThat(room.getCreatorId(), is(RoomRepository.WILL_CROWTHER_ID));

        room = this.storage.getRoom(RoomRepository.p001.getPositionString());
        assertThat(room.getDescription(), containsString(RoomRepository.p001_description));
        assertThat(room.getCreatorId(), is(RoomRepository.WILL_CROWTHER_ID));

        room = this.storage.getRoom(RoomRepository.p100.getPositionString());
        assertThat(room.getDescription(), containsString(RoomRepository.p100_description));
        assertThat(room.getCreatorId(), is(RoomRepository.WILL_CROWTHER_ID));

        room = this.storage.getRoom(RoomRepository.p010.getPositionString());
        assertThat(room.getDescription(), containsString(RoomRepository.p010_description));
        assertThat(room.getCreatorId(), is(RoomRepository.WILL_CROWTHER_ID));
    }

    public void shouldNotCreatedUnConnectedRoomPosition() throws RoomRepositoryException {

        //Validate that the room is not made
        try {
            storage.addRoom(p273.getPositionString(), new Room(p273_description, p273_creatorId));
        }catch (RoomRepositoryException ex) {
            assertThat(ex.error, is(HttpStatus.FORBIDDEN));

        }
    }

    public void shouldNotCreatedOnExistingPosition() {
        Room room;

        //Validate that the room is not made
        try {
            storage.addRoom(RoomRepository.p001.getPositionString(), new Room(p200_description, p200_creatorId));
        } catch (RoomRepositoryException re) {
            assertThat(re.error, is(HttpStatus.CONFLICT));
        }
    }

    public void shouldCreateAndUpdateRoom() throws RoomRepositoryException {
        Room room;

        //Validate that rooms can be made

        room = storage.addRoom(p200.getPositionString(), new Room(p200_description, p200_creatorId));
        assertThat(room, notNullValue());
        assertThat(room.getCreationTimeISO8601(), notNullValue());
        assertThat(room.getId(), notNullValue());
        assertThat(room.getCreatorId(), is(p200_creatorId));
        assertThat(room.getDescription(), is(p200_description));

        //Validate that the room can be updated
        room = storage.updateRoom(p200.getPositionString(), new Room("Another description", p200_creatorId));
        assertThat(room.getCreatorId(), is(p200_creatorId));
        assertThat(room.getDescription(), is("Another description"));
    }

    public void shouldRejectInvalidPositionString() {
        try {
            ArrayList<String> exits = storage.getAllExitsAtPosition("KamelString");
        } catch (RoomRepositoryException e) {
            assertThat(e.error, is(HttpStatus.BAD_REQUEST));
        }
    }

    public void shouldNotGetExitSet() {
        try {
            ArrayList<String> exits;
            exits = storage.getAllExitsAtPosition(p273.getPositionString());
            assertThat(exits.size(), is(0));
        } catch (RoomRepositoryException e) {
            assertThat(e.error, is(HttpStatus.BAD_REQUEST));
        }
    }

    public void shouldGetExitSet() {
        try {
            ArrayList<String> exits;
            exits = storage.getAllExitsAtPosition(RoomRepository.p000.getPositionString());
            assertThat(exits.size(), is(4));

            assertThat(exits.contains(Direction.EAST.toString()), is(true));
            assertThat(exits.contains(Direction.WEST.toString()), is(true));
            assertThat(exits.contains(Direction.NORTH.toString()), is(true));
            assertThat(exits.contains(Direction.SOUTH.toString()), is(false));
            assertThat(exits.contains(Direction.DOWN.toString()), is(false));
            assertThat(exits.contains(Direction.UP.toString()), is(true));
        } catch (RoomRepositoryException e) {
            assertThat(e.error, is(HttpStatus.BAD_REQUEST));
        }

    }

    public void shouldNotAllowUpdateOfNonExistingRoom() {
        // When updating room
        Room updatedRoom = new Room("A description", "hans");
        try {
            // Then 404 is returned
            storage.updateRoom(p273.getPositionString(), updatedRoom);
        } catch (RoomRepositoryException ex) {
            assertThat(ex.error, is(HttpStatus.NOT_FOUND));
        }
    }

    public void shouldNotAllowNonCreatorToUpdateRoom() throws RoomRepositoryException {
        // Given the room by Crowther at 0,0,0

        Room updatedRoom = new Room("A description", "BlackHat");
        // Then 401 is returned
        try {
            storage.updateRoom(p000.getPositionString(), updatedRoom);
        } catch (RoomRepositoryException ex) {
            assertThat(ex.error, is(HttpStatus.UNAUTHORIZED));
        }
        // and room is not changed

        assertThat(storage.getRoom(RoomRepository.p000.getPositionString()).getDescription(),
                containsString(RoomRepository.p000_description));
    }

    public void shouldNotAllowMissingCreatorIDinAddRoom() {
        Room newRoom = new Room("A description", null);

        try {
            storage.addRoom(p000.getPositionString(), newRoom);
        } catch (RoomRepositoryException ex) {
            assertThat(ex.error, is(HttpStatus.BAD_REQUEST));
        }
    }

    public void shouldNotAllowMissingCreatorIDinUpdateRoom() {
        Room roomToUpdate = new Room("A description", null);

        try {
            storage.updateRoom(p000.getPositionString(), roomToUpdate);
        } catch (RoomRepositoryException ex) {
            assertThat(ex.error, is(HttpStatus.BAD_REQUEST));
        }
    }

    public void shouldNotAllowInvalidPositionStringInAddRoom() {
        Room newRoom = new Room("A description", "BlackHat");

        try {
            storage.addRoom("WhackAMole", newRoom);
        } catch (RoomRepositoryException ex) {
            assertThat(ex.error, is(HttpStatus.BAD_REQUEST));
        }
    }

    public void shouldNotAllowInvalidPositionStringInUpdateRoom() {
        Room newRoom = new Room("A description", "BlackHat");

        try {
            storage.updateRoom("WhackAMole", newRoom);
        } catch (RoomRepositoryException ex) {
            assertThat(ex.error, is(HttpStatus.BAD_REQUEST));
        }
    }

    public void shouldNotAllowInvalidPositionStringInGetRoom() {
        try {
            storage.getRoom("WhackAMole");
        } catch (RoomRepositoryException ex) {
            assertThat(ex.error, is(HttpStatus.BAD_REQUEST));
        }
    }

    public void shouldNotAllowInvalidPositionStringInGetExits() {
        try {
            storage.getAllExitsAtPosition("WhackAMole");
        } catch (RoomRepositoryException ex) {
            assertThat(ex.error, is(HttpStatus.BAD_REQUEST));
        }
    }
}
