package kessel.hex.orders.king;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test improving a player's power. */
public class ImprovePowerTest extends AbstractOrderTest<ImprovePower>
{
  private static final Logger LOG = Logger.getLogger( ImprovePowerTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    ImprovePower oldOrder = new ImprovePower( playerA.getKing() );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    ImprovePower newOrder = Game.GSON.fromJson( jsonOrder, ImprovePower.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testSecondPolicyOrder()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    ImprovePower order = new ImprovePower( playerA.getKing() );
    playerA.getKing().addOrderExecuted( order );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testNotEnoughMoney()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 0 );
    ImprovePower order = new ImprovePower( playerA.getKing() );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testImproved()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 20000 );
    int oldLevel = playerA.getPower();
    ImprovePower order = new ImprovePower( playerA.getKing() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertTrue( playerA.getGold() < 20000 );
    assertEquals( oldLevel + 1, playerA.getPower() );
  }
}
