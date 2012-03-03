package kessel.hex.orders.wizard;

import kessel.hex.domain.Diplomat;
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

public class CharmFigureTest extends AbstractOrderTest<CharmFigure>
{
  private static final Logger LOG = Logger.getLogger( CharmFigureTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( CharmFigure a, CharmFigure b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getTarget().getId(), b._jsonTargetId.intValue() );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    CharmFigure oldOrder = new CharmFigure( wizardForA, diplomatForB );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    CharmFigure newOrder = Game.GSON.fromJson( jsonOrder, CharmFigure.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTargetShielded()
  {
    // Cast the shield.
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    Wizard wizardForB = playerB.getWizards().get( 0 );
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    wizardForB.setLevel( 2 );

    ShieldFigure shieldOrder = new ShieldFigure( wizardForB, diplomatForB );
    shieldOrder.execute( game );
    assertTrue( shieldOrder.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( ShieldFigure.class ).size() );

    // Cast the charm, it should fail due to the protection.
    wizardForA.setLevel( 2 );
    CharmFigure charmOrder = new CharmFigure( wizardForA, diplomatForB );
    charmOrder.execute( game );
    assertFalse( charmOrder.wasExecuted() );
  }

  @Test
  public void testTargetWizard()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    Wizard wizardForB = playerB.getWizards().get( 0 );
    wizardForA.setLevel( 2 );

    CharmFigure order = new CharmFigure( wizardForA, wizardForB );
    order.execute( game );

    assertFalse( order.wasExecuted() );
    assertEquals( 0, game.getCurrentTurn().getOrdersOfType( CharmFigure.class ).size() );
  }

  @Test
  public void testSuccess()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    wizardForA.setLevel( 2 );

    CharmFigure order = new CharmFigure( wizardForA, diplomatForB );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( CharmFigure.class ).size() );
  }
}
