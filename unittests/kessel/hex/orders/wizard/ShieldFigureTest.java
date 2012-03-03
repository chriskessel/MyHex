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

public class ShieldFigureTest extends AbstractOrderTest<ShieldFigure>
{
  private static final Logger LOG = Logger.getLogger( ShieldFigureTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( ShieldFigure a, ShieldFigure b ) throws Exception
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
    ShieldFigure oldOrder = new ShieldFigure( wizardForA, diplomatForB );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    ShieldFigure newOrder = Game.GSON.fromJson( jsonOrder, ShieldFigure.class );
    doEqualsTest( oldOrder, newOrder );
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

    ShieldFigure order = new ShieldFigure( wizardForA, diplomatForB );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( ShieldFigure.class ).size() );
  }
}
