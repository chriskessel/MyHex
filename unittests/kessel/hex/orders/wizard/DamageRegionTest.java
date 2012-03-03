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

public class DamageRegionTest extends AbstractOrderTest<DamageRegion>
{
  private static final Logger LOG = Logger.getLogger( DamageRegionTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Wizard wizardForA = game.getPlayers().get( 0 ).getWizards().get( 0 );

    DamageRegion oldOrder = new DamageRegion( wizardForA );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    DamageRegion newOrder = Game.GSON.fromJson( jsonOrder, DamageRegion.class);
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testDamageRegionBlockedByShield()
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
    DamageRegion imbueOrder = new DamageRegion( wizardForA );
    imbueOrder.execute( game );
    assertFalse( imbueOrder.wasExecuted() );
  }

  @Test
  public void testDamageRegionImpact()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setLevel( 7 );
    DamageRegion order = new DamageRegion( wizardForA );
    int oldValue = playerA.getCapitol().getLevel();
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( oldValue - 1, playerA.getCapitol().getLevel() );
  }
}
