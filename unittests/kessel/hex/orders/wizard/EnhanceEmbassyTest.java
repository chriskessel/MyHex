package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class EnhanceEmbassyTest extends AbstractOrderTest<EnhanceEmbassy>
{
  private static final Logger LOG = Logger.getLogger( EnhanceEmbassyTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    EnhanceEmbassy oldOrder = new EnhanceEmbassy( wizardForA );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    EnhanceEmbassy newOrder = Game.GSON.fromJson( jsonOrder, EnhanceEmbassy.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testSuccess()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setLevel( 5 );

    EnhanceEmbassy order = new EnhanceEmbassy( wizardForA );
    int oldEmbassy = playerA.getEmbassyLevel( wizardForA.getLocation().getRegion() );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertEquals( oldEmbassy + 1, playerA.getEmbassyLevel( wizardForA.getLocation().getRegion() ) );
  }
}
