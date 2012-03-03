package kessel.hex.domain;

import kessel.hex.map.Location;
import kessel.hex.map.Terrain;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

public class KingTest extends GameItemTest<King>
{
  private static final Logger LOG = Logger.getLogger( KingTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Location loc = new Location( 1, 1 );
    loc.setTerrain( Terrain.Forest );
    PopCenter popA = new PopCenter( GameItem.UNKNOWN_ID, "Bob", 4, loc, PopCenter.PopType.Hamlet );
    King kingA = new King( GameItem.UNKNOWN_ID, "Bob", 4, popA );

    String json = Game.GSON.toJson( kingA );
    LOG.debug( json );

    King kingB = Game.GSON.fromJson( json, King.class );
    doEqualsTest( kingA, kingB );
  }
}
