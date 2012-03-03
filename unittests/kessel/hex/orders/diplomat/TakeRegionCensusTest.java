package kessel.hex.orders.diplomat;

import kessel.hex.domain.Diplomat;
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

/** Test taking the census of a region. */
public class TakeRegionCensusTest extends AbstractOrderTest<TakeRegionCensus>
{
  private static final Logger LOG = Logger.getLogger( TakeRegionCensusTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( TakeRegionCensus a, TakeRegionCensus b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getRegionalCity().getId(), b._jsonCityId.intValue() );
    assertEquals( a._popType, b._popType );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    TakeRegionCensus oldOrder = new TakeRegionCensus( diplomat, game.getPopCenter( 0, 0 ), PopCenter.PopType.Town );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    TakeRegionCensus newOrder = Game.GSON.fromJson( jsonOrder, TakeRegionCensus.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testNotEnoughMoney()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 0 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    TakeRegionCensus order = new TakeRegionCensus( diplomat, game.getPopCenter( 0, 0 ), PopCenter.PopType.Town );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testCensusTaken()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 100000 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    diplomat.setBase( game.getPopCenter( 1, 1 ) );
    TakeRegionCensus order = new TakeRegionCensus( diplomat, game.getPopCenter( 2, 2 ), PopCenter.PopType.Hamlet );
    assertEquals( 24, playerA.getKnownItems().size() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 25, playerA.getKnownItems().size() );
  }
}
