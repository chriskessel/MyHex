package kessel.hex.orders.agent;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.orders.AbstractHireFigureTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test agent purchasing. */
public class HireAgentTest extends AbstractHireFigureTest<HireAgent>
{
  private static final Logger LOG = Logger.getLogger( HireAgentTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    PopCenter popForA = playerA.getCapitol();

    HireAgent oldOrder = new HireAgent( playerA.getKing(), popForA );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    HireAgent newOrder = Game.GSON.fromJson( jsonOrder, HireAgent.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testBadPop()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = playerB.getCapitol();

    HireAgent order = new HireAgent( playerA.getKing(), popForB );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testOutOfMoney()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    PopCenter popForA = playerA.getCapitol();
    playerA.setGold( 1 );

    HireAgent order = new HireAgent( playerA.getKing(), popForA );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testSuccess()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    PopCenter popForA = playerA.getCapitol();
    playerA.setGold( Game.GOLD_GRANULARITY * 100 );

    HireAgent order = new HireAgent( playerA.getKing(), popForA );
    assertEquals( 4, playerA.getAgents().size() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 5, playerA.getAgents().size() );
  }
}
