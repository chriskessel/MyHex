package kessel.hex.domain;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import static org.junit.Assert.assertEquals;

public abstract class GameItemTest<X extends GameItem>
{
  private static final Logger LOG = Logger.getLogger( GameItemTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( X a, X b ) throws Exception
  {
    assertEquals( a.getId(), b.getId() );
    assertEquals( a.getName(), b.getName() );
    assertEquals( a.isInvisible(), b.isInvisible() );
    assertEquals( a.getTurnSeen(), b.getTurnSeen() );
    assertEquals( a.getLocation().getCoord(), b._jsonLocationTuple );
    assertEquals( a.getOwner().getName(), b._jsonOwnerName );
    assertEquals( a.getLastTurnStatus(), b.getLastTurnStatus() );
  }
}
