package kessel.hex.orders.king;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test improving pop centers. */
public class ImprovePopCenterTest extends AbstractOrderTest<ImprovePopCenter>
{
  private static final Logger LOG = Logger.getLogger( ImprovePopCenterTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( ImprovePopCenter a, ImprovePopCenter b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a._target.getId(), b._jsonTargetId.intValue() );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    PopCenter pop = playerA.getPopCenters().get( 0 );
    ImprovePopCenter oldOrder = new ImprovePopCenter( playerA.getKing(), pop );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );

    ImprovePopCenter newOrder = Game.GSON.fromJson( jsonOrder, ImprovePopCenter.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testNotEnoughMoney()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 0 );
    PopCenter pop = playerA.getPopCenters().get( 0 );
    ImprovePopCenter order = new ImprovePopCenter( playerA.getKing(), pop );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testImproved()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 10000 );
    PopCenter pop = playerA.getPopCenters().get( 0 );
    int oldLevel = pop.getLevel();
    ImprovePopCenter order = new ImprovePopCenter( playerA.getKing(), pop );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( oldLevel + 1, pop.getLevel() );
  }
}
