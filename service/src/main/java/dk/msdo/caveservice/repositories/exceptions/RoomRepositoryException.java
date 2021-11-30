package dk.msdo.caveservice.repositories.exceptions;

import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;

public class RoomRepositoryException extends Exception
{
    public HttpStatus error;

    public static  Integer E_TEAPOT = 418; // i'm a teapot

    public RoomRepositoryException(String str, HttpStatus error)
    {
        // calling the constructor of parent Exception
        super(str);
        this.error = error;
    }
}
