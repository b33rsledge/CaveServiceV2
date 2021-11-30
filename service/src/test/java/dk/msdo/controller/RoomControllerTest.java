package dk.msdo.controller;


import com.google.gson.Gson;
import dk.msdo.caveservice.ServiceApplication;
import dk.msdo.caveservice.controller.RoomController;
import dk.msdo.caveservice.domain.Direction;
import dk.msdo.caveservice.domain.Room;
import dk.msdo.caveservice.repositories.MemoryRoomRepositoryImpl;
import dk.msdo.caveservice.repositories.RoomRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=RoomController.class, properties = "storage.room=memoryStorage" )
@ContextConfiguration(classes={ServiceApplication.class, MemoryRoomRepositoryImpl.class})
public class RoomControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(RoomControllerTest.class);

    RoomController controller = new RoomController(new MemoryRoomRepositoryImpl());

    @Test
    public void shouldGetDefaultRooms() {
        // P000
        ResponseEntity<String> result = controller.getV2(RoomRepository.p000.getPositionString());
        Room room = new Gson().fromJson(result.getBody(), Room.class);
        logger.info("method=shouldGetDefaultRooms, implementationClass=" + this.getClass().getName() + "Body: " + result.getBody());

        //Verify request succeed
        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCodeValue());
        Assertions.assertTrue(room.getCreatorId().contains("0"));
        Assertions.assertEquals(RoomRepository.p000_description, room.getDescription());
        Assertions.assertNotNull(room.getCreationTimeISO8601());

        // P001
        result = controller.getV2(RoomRepository.p001.getPositionString());
        room = new Gson().fromJson(result.getBody(), Room.class);
        logger.info("method=shouldGetDefaultRooms, implementationClass=" + this.getClass().getName() + "Body: " + result.getBody());

        //Verify request succeed
        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCodeValue());
        Assertions.assertEquals(room.getCreatorId(), "0");
        Assertions.assertEquals(room.getDescription(), RoomRepository.p001_description);
        Assertions.assertNotNull(room.getCreationTimeISO8601());

        // P010
        result = controller.getV2(RoomRepository.p010.getPositionString());
        room = new Gson().fromJson(result.getBody(), Room.class);
        logger.info("method=shouldGetDefaultRooms, implementationClass=" + this.getClass().getName() + "Body: " + result.getBody());

        //Verify request succeed
        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCodeValue());
        Assertions.assertTrue(room.getCreatorId().contains("0"));
        Assertions.assertEquals(room.getDescription(), RoomRepository.p010_description);
        Assertions.assertNotNull(room.getCreationTimeISO8601());

        // P100
        result = controller.getV2(RoomRepository.p100.getPositionString());
        room = new Gson().fromJson(result.getBody(), Room.class);
        logger.info("method=shouldGetDefaultRooms, implementationClass=" + this.getClass().getName() + "Body: " + result.getBody());

        //Verify request succeed
        Assertions.assertEquals(HttpStatus.OK.value(),result.getStatusCodeValue());
        Assertions.assertTrue(room.getCreatorId().contains("0"));
        Assertions.assertEquals(room.getDescription(), RoomRepository.p100_description);
        Assertions.assertNotNull(room.getCreationTimeISO8601());

        // P_100
        result = controller.getV2(RoomRepository.p_100.getPositionString());
        room = new Gson().fromJson(result.getBody(), Room.class);
        logger.info("method=shouldGetDefaultRooms, implementationClass=" + this.getClass().getName() + "Body: " + result.getBody());

        //Verify request succeed
        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCodeValue());
        Assertions.assertTrue(room.getCreatorId().contains("0"));
        Assertions.assertEquals(room.getDescription(), RoomRepository.p_100_description);
        Assertions.assertNotNull(room.getCreationTimeISO8601());
    }

    @Test
    public void shouldNotGetNonExistingRoom()  {
        try {
            controller.getV2("(2,7,3)");
        } catch (HttpClientErrorException e) {
            Assertions.assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
        }
    }

    @Test
    public void getRoomShouldNotUseMalformedPosition() {
        try {
            controller.getV2("(2,***)");
        } catch (HttpClientErrorException e) {
            Assertions.assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        }
    }

    @Test
    public void  getExitShouldNotUseMalformedPosition(){
        try {
            controller.getRoomExitsAtPosition("(72,***)");
        } catch (HttpClientErrorException e) {
            Assertions.assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        }
    }

    @Test
    public void shouldGetExitsAtPosition() {
         ResponseEntity<String>  result  = controller.getRoomExitsAtPosition(RoomRepository.p000.getPositionString());
         logger.info("method=shouldGetExitsAtPosition, implementationClass=" + this.getClass().getName() + "Body: " + result.getBody());
         Assertions.assertEquals(HttpStatus.OK.value(),  result.getStatusCodeValue());
         Assertions.assertTrue(Objects.requireNonNull(result.getBody()).contains(Direction.NORTH.toString()));
         Assertions.assertTrue(result.getBody().contains(Direction.EAST.toString()));
         Assertions.assertTrue(result.getBody().contains(Direction.WEST.toString()));
         Assertions.assertTrue(result.getBody().contains(Direction.UP.toString()));
         Assertions.assertFalse(result.getBody().contains(Direction.DOWN.toString()));
         Assertions.assertFalse(result.getBody().contains(Direction.SOUTH.toString()));
    }

    @Test
    public void shouldUpdateRoomDescription() {
        Room room = new Room ("","new description", "0", "");
        ResponseEntity<String>  result = controller.PutV2(RoomRepository.p000.getPositionString(), room);

        Assertions.assertEquals(HttpStatus.OK.value(),  result.getStatusCodeValue());
        Room updatedRoom = new Gson().fromJson(result.getBody(), Room.class);
        Assertions.assertEquals(room.getDescription(), updatedRoom.getDescription());
    }

    @Test
    public void shouldNotUpdateRoomDescription() {
        Room room = new Room ("","new description", "42", "");
        ResponseEntity<String>  result = controller.PutV2(RoomRepository.p000.getPositionString(), room);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(),  result.getStatusCodeValue());
    }

    @Test
    public void shouldNotUpdateRoomAtMalformedPosition() {
        Room room = new Room ("","new description", "42", "");
        ResponseEntity<String>  result = controller.PutV2("2,2,)", room);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(),  result.getStatusCodeValue());
    }


    @Test
    public void shouldCreateRoom() {
        Room room = new Room ("","new description", "0", "");
        ResponseEntity<String>  result = controller.PostV2("(2,0,0)", room);

        Assertions.assertEquals(HttpStatus.CREATED.value(),  result.getStatusCodeValue());
        Room updatedRoom = new Gson().fromJson(result.getBody(), Room.class);
        Assertions.assertEquals(updatedRoom.getDescription(), "new description");
        Assertions.assertEquals(updatedRoom.getCreatorId(), "0");
        Assertions.assertNotNull(updatedRoom.getCreationTimeISO8601());
        Assertions.assertNotNull(updatedRoom.getId());
    }

    @Test
    public void shouldNotCreateRoomMissingID() {
        Room room = new Room ("","new description", null, "");
        ResponseEntity<String>  result = controller.PostV2("(3,0,0)", room);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(),  result.getStatusCodeValue());
    }

    @Test
    public void shouldNotCreateRoomAtMalformedPosition() {
        Room room = new Room ("","new description", "0", "");
        ResponseEntity<String>  result = controller.PostV2("(2,2", room);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(),  result.getStatusCodeValue());
    }

    @Test
    public void shouldNotCreateRoomAtIllegalPosition() {
        Room room = new Room ("","new description", "0", "");
        ResponseEntity<String>  result = controller.PostV2("(2,7,3)", room);
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(),  result.getStatusCodeValue());
    }

}

