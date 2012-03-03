package kessel.hex.domain;

import kessel.hex.map.Location;
import kessel.hex.map.Terrain;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

public class WizardTest extends FigureTest<Wizard>
{
  private static final Logger LOG = Logger.getLogger( WizardTest.class );

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
    Wizard wizardA = new Wizard( GameItem.UNKNOWN_ID, "Bob", 4, popA );

    String json = Game.GSON.toJson( wizardA );
    LOG.debug( json );

    Wizard wizardB = Game.GSON.fromJson( json, Wizard.class );
    doEqualsTest( wizardA, wizardB );
  }
}
