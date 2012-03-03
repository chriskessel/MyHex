package kessel.hex.orders.diplomat;

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

/** Test diplomat purchasing. */
public class HireDiplomatTest extends AbstractHireFigureTest<HireDiplomat>
{
  private static final Logger LOG = Logger.getLogger( HireDiplomatTest.class );

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

    HireDiplomat oldOrder = new HireDiplomat( playerA.getKing(), popForA );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    HireDiplomat newOrder = Game.GSON.fromJson( jsonOrder, HireDiplomat.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testBadPop()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = playerB.getCapitol();

    HireDiplomat order = new HireDiplomat( playerA.getKing(), popForB );
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

    HireDiplomat order = new HireDiplomat( playerA.getKing(), popForA );
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

    HireDiplomat order = new HireDiplomat( playerA.getKing(), popForA );
    assertEquals( 8, playerA.getDiplomats().size() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 9, playerA.getDiplomats().size() );
  }
}
