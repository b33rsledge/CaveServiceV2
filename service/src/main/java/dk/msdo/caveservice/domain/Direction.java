package dk.msdo.caveservice.domain;


public enum Direction {
   NORTH, SOUTH, EAST, WEST, UP, DOWN;


   public static boolean isValid(String str) {
      for (Direction me : Direction.values()) {
         if (me.name().equalsIgnoreCase(str))
            return true;
      }
      return false;
   }
}
