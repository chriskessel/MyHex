package kessel.hex.orders.king;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.map.Region;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test improving an embassy. */
public class ImproveEmbassyTest extends AbstractOrderTest<ImproveEmbassy>
{
  private static final Logger LOG = Logger.getLogger( ImproveEmbassyTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( ImproveEmbassy a, ImproveEmbassy b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a._region, b._region );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    ImproveEmbassy oldOrder = new ImproveEmbassy( playerA.getKing(), "Region 1" );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    ImproveEmbassy newOrder = Game.GSON.fromJson( jsonOrder, ImproveEmbassy.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testBadRegionName()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    ImproveEmbassy order = new ImproveEmbassy( playerA.getKing(), "foo" );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testSecondPolicyOrder()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    ImproveEmbassy order = new ImproveEmbassy( playerA.getKing(), "Region 1" );
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
    ImproveEmbassy order = new ImproveEmbassy( playerA.getKing(), "Region 1" );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testImproved()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 10000 );
    Region region = game.getMap().getRegions().get( 0 );
    int oldLevel = playerA.getEmbassyLevel( region );
    ImproveEmbassy order = new ImproveEmbassy( playerA.getKing(), region.getName() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( oldLevel + 1, playerA.getEmbassyLevel( region ) );
  }
}
