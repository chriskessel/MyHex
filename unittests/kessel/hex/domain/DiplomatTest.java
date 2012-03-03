package kessel.hex.domain;

import kessel.hex.map.Location;
import kessel.hex.map.Terrain;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

public class DiplomatTest extends GameItemTest<Diplomat>
{
  private static final Logger LOG = Logger.getLogger( DiplomatTest.class );

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
    Diplomat diplomatA = new Diplomat( GameItem.UNKNOWN_ID, "Bob", 4, popA );

    String json = Game.GSON.toJson( diplomatA );
    LOG.debug( json );

    Diplomat diplomatB = Game.GSON.fromJson( json, Diplomat.class );
    doEqualsTest( diplomatA, diplomatB );
  }
}
