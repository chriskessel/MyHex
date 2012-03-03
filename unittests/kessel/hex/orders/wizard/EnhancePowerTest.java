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

public class EnhancePowerTest extends AbstractOrderTest<EnhancePower>
{
  private static final Logger LOG = Logger.getLogger( EnhancePowerTest.class );

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
    EnhancePower oldOrder = new EnhancePower( wizardForA );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    EnhancePower newOrder = Game.GSON.fromJson( jsonOrder, EnhancePower.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTargetShielded()
  {
    // Cast the shield.
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardOneForA = playerA.getWizards().get( 0 );
    wizardOneForA.setLevel( 2 );

    ShieldFigure shieldOrder = new ShieldFigure( wizardOneForA, playerA.getKing() );
    shieldOrder.execute( game );
    assertTrue( shieldOrder.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( ShieldFigure.class ).size() );

    // Cast the charm, it should fail due to the protection.
    Wizard wizardTwoForA = playerA.getWizards().get( 1 );
    wizardTwoForA.setLevel( 5 );
    EnhancePower enhanceOrder = new EnhancePower( wizardTwoForA );
    enhanceOrder.execute( game );
    assertFalse( enhanceOrder.wasExecuted() );
  }

  @Test
  public void testSuccess()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setLevel( 5 );

    EnhancePower order = new EnhancePower( wizardForA );
    int oldPower = playerA.getPower();
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertEquals( oldPower + 1, playerA.getPower() );
  }
}
