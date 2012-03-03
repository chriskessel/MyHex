package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class ShieldRegionTest extends AbstractOrderTest<ShieldRegion>
{
  private static final Logger LOG = Logger.getLogger( ShieldRegionTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Wizard wizardForA = game.getPlayers().get( 0 ).getWizards().get( 0 );

    ShieldRegion oldOrder = new ShieldRegion( wizardForA );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    ShieldRegion newOrder = Game.GSON.fromJson( jsonOrder, ShieldRegion.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testShieldSuccess()
  {
    Game game = GameTest.createSimpleGame();
    Wizard wizardForB = game.getPlayers().get( 1 ).getWizards().get( 0 );
    wizardForB.setLevel( 3 );
    ShieldRegion order = new ShieldRegion( wizardForB );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( ShieldRegion.class ).size() );
  }
}
