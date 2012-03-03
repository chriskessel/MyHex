package kessel.hex.orders.diplomat;

import kessel.hex.domain.Diplomat;
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

/** Test mapping a region */
public class MapRegionTest extends AbstractOrderTest<MapRegion>
{
  private static final Logger LOG = Logger.getLogger( MapRegionTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( MapRegion a, MapRegion b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getRegionalCity().getId(), b._jsonCityId.intValue() );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    MapRegion oldOrder = new MapRegion( diplomat, game.getPopCenter( 0, 0 ) );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    MapRegion newOrder = Game.GSON.fromJson( jsonOrder, MapRegion.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testBadTarget()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 100000 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    MapRegion order = new MapRegion( diplomat, game.getPopCenter( 1, 1 ) );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), MapRegion.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testNotEnoughMoney()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 0 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    MapRegion order = new MapRegion( diplomat, game.getPopCenter( 0, 0 ) );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), MapRegion.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testMapped()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 100000 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    Region region = diplomat.getLocation().getRegion();
    MapRegion order = new MapRegion( diplomat, game.getPopCenter( 0, 0 ) );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), MapRegion.class );
    order.fixDeserializationReferences( game );
    assertEquals( 2, playerA.getGameView().getMap().getRegion( region.getName() ).getLocations().size() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 3, playerA.getGameView().getMap().getRegion( region.getName() ).getLocations().size() );
  }
}
