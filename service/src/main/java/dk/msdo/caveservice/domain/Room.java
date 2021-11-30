package dk.msdo.caveservice.domain;

import dk.msdo.caveservice.repositories.exceptions.RoomRepositoryException;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Room implements Serializable {
    private static final long serialVersionUID = -1L;
    private @Id String id;
    private String description;
    private String creatorId;
    private String creationTimeISO8601;

    public Room(String description, String creatorId) {
        this.description = description;
        this.creatorId = creatorId;
    }
}
