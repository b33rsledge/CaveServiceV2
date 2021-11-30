package dk.msdo.repository;

import dk.msdo.caveservice.Configuration;
import dk.msdo.caveservice.repositories.RedisDBRoomRepositoryImpl;
import dk.msdo.caveservice.repositories.exceptions.RoomRepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
/**
 * TDD of RoomRepository interface and
 * driving the RedisRepository implementation.
 *
 * @author Team Alpha, Aarhus University.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RedisDBRoomRepositoryImpl.class,
        Configuration.class},
        properties = {"storage.room=redisStorage"})
// Load the RedisDB test container defined in TestContainerRedisDB
@ContextConfiguration(initializers = TestContainerRedisDB.Initializer.class)
public class RoomRepositoryRedisStorageTest extends RoomRepositoryTests {

    @Autowired
    RedisDBRoomRepositoryImpl storage;

    @BeforeEach
    public void setup() {
        super.setup(storage);
    }

    @Test
    public void shouldReadInitialRoomsInStorage() throws RoomRepositoryException {
        super.shouldReadInitialRoomsInStorage();
    }

    @Test
    public void shouldNotCreatedUnConnectedRoomPosition() throws RoomRepositoryException {
        super.shouldNotCreatedUnConnectedRoomPosition();
    }

    @Test
    public void shouldNotCreatedOnExistingPosition() {
        super.shouldNotCreatedOnExistingPosition();
    }

    @Test
    public void shouldCreateAndUpdateRoom() throws RoomRepositoryException {
        super.shouldCreateAndUpdateRoom();
    }

    @Test
    public void shouldRejectInvalidPositionString() {
        super.shouldRejectInvalidPositionString();
    }

    @Test
    public void shouldNotGetExitSet() {
        super.shouldNotGetExitSet();
    }

    @Test
    public void shouldGetExitSet() {
        super.shouldGetExitSet();
    }

    @Test
    public void shouldNotAllowUpdateOfNonExistingRoom() {
        super.shouldNotAllowUpdateOfNonExistingRoom();
    }

    @Test
    public void shouldNotAllowNonCreatorToUpdateRoom() throws RoomRepositoryException {
        super.shouldNotAllowNonCreatorToUpdateRoom();
    }

    @Test
    public void shouldNotAllowMissingCreatorIDinAddRoom() {
        super.shouldNotAllowMissingCreatorIDinAddRoom();
    }

    @Test
    public void shouldNotAllowMissingCreatorIDinUpdateRoom() {
        super.shouldNotAllowMissingCreatorIDinUpdateRoom();
    }

    @Test
    public void shouldNotAllowInvalidPositionStringInAddRoom() {
        super.shouldNotAllowInvalidPositionStringInAddRoom();
    }

    @Test
    public void shouldNotAllowInvalidPositionStringInUpdateRoom() {
        super.shouldNotAllowInvalidPositionStringInUpdateRoom();
    }

    @Test
    public void shouldNotAllowInvalidPositionStringInGetRoom() {
        super.shouldNotAllowInvalidPositionStringInGetRoom();
    }

    @Test
    public void shouldNotAllowInvalidPositionStringInGetExits() {
        super.shouldNotAllowInvalidPositionStringInGetExits();
    }
}
