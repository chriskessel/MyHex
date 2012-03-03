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

public class ImbueRegionTest extends AbstractOrderTest<ImbueRegion>
{
  private static final Logger LOG = Logger.getLogger( ImbueRegionTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Wizard wizardForA = game.getPlayers().get( 0 ).getWizards().get( 0 );

    ImbueRegion oldOrder = new ImbueRegion( wizardForA );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    ImbueRegion newOrder = Game.GSON.fromJson( jsonOrder, ImbueRegion.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testImbueRegionBlockedByShield()
  {
    // Cast the shield spell.
    Game game = GameTest.createSimpleGame();
    Wizard wizardForB = game.getPlayers().get( 1 ).getWizards().get( 0 );
    wizardForB.setLevel( 3 );
    ShieldRegion shieldOrder = new ShieldRegion( wizardForB );
    shieldOrder.execute( game );
    assertTrue( shieldOrder.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( ShieldRegion.class ).size() );

    // Cast the charm spell.
    Wizard wizardForA = game.getPlayers().get( 0 ).getWizards().get( 0 );
    wizardForA.setBase( wizardForB.getBase() );
    wizardForA.setLevel( 7 );
    ImbueRegion imbueOrder = new ImbueRegion( wizardForA );
    imbueOrder.execute( game );
    assertFalse( imbueOrder.wasExecuted() );
  }

  @Test
  public void testImbueRegionImpact()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setLevel( 7 );
    ImbueRegion order = new ImbueRegion( wizardForA );
    int oldValue = playerA.getCapitol().getLevel();
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( oldValue + 1, playerA.getCapitol().getLevel() );
  }
}
