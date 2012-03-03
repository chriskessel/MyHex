package kessel.hex.domain;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({ "unchecked" })
public class KingdomTest
{
  private static final Logger LOG = Logger.getLogger( KingdomTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Kingdom kingdom = (Kingdom) Kingdoms.KINGDOMS.values().toArray()[0];
    assertEquals( 15, Kingdoms.KINGDOMS.size() );
    String json = Game.GSON.toJson( kingdom, Kingdom.class );
    LOG.debug( json );
    Kingdom kingdomIn = Game.GSON.fromJson( json, Kingdom.class );
    assertEquals( kingdom, kingdomIn );
  }
}
