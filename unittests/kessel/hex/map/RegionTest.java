package kessel.hex.map;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RegionTest
{
  private static final Logger LOG = Logger.getLogger( RegionTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Region oldRegion = addRegion( game );
    String jsonOrder = Game.GSON.toJson( oldRegion );
    LOG.debug( jsonOrder );
    Region newRegion = Game.GSON.fromJson( jsonOrder, Region.class );
    assertEquals( oldRegion, newRegion );
    assertEquals( oldRegion.getLocations().size(), newRegion._jsonLocTuples.size() );
    assertEquals( oldRegion.getPopCenters().size(), newRegion._jsonPopIds.size() );
  }

  @Test
  public void testLogic() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Region region = addRegion( game );

    assertEquals( 10, region.getTotalPopLevels() );

    // Test player has no control level.
    Player player = game.getPlayers().get( 0 );
    assertEquals( ControlLevel.None, player.getControlLevel( region ) );

    // Test player has Presence.
    region.getPopCenters().get( 0 ).setOwner( player );
    assertEquals( ControlLevel.Presence, player.getControlLevel( region ) );

    // Test player has Control.
    region.getPopCenters().get( 0 ).setOwner( player );
    region.getPopCenters().get( 1 ).setOwner( player );
    region.getPopCenters().get( 2 ).setOwner( player );
    region.getPopCenters().get( 3 ).setOwner( player );
    region.getPopCenters().get( 4 ).setOwner( player );
    assertEquals( ControlLevel.Presence, player.getControlLevel( region ) );
    region.getPopCenters().get( 5 ).setOwner( player );
    assertEquals( ControlLevel.Control, player.getControlLevel( region ) );

    // Test player has Domination levels, but only Control due to lack of a city
    region.getPopCenters().get( 6 ).setOwner( player );
    region.getPopCenters().get( 7 ).setOwner( player );
    assertEquals( ControlLevel.Control, player.getControlLevel( region ) );

    // Test player has domination with city and 80%, loses it with 70% and city.
    region.getPopCenters().get( 6 ).setOwner( Player.UNOWNED );
    region.getPopCenters().get( 7 ).setOwner( Player.UNOWNED );
    region.getPopCenters().get( 8 ).setOwner( player );
    assertEquals( ControlLevel.Domination, player.getControlLevel( region ) );
    region.getPopCenters().get( 5 ).setOwner( Player.UNOWNED );
    assertEquals( ControlLevel.Control, player.getControlLevel( region ) );
  }

  private Region addRegion( Game game )
  {
    Region region = game.getMap().getRegions().get( 0 );
    region.getPopCenters().clear();

    // Add 8 towns, all level 1.
    for ( int i = 0; i < 8; i++ )
    {
      PopCenter pop = new PopCenter( game.generateUniqueId(), "Pop" + i, 0, new Location( i, i ), PopCenter.PopType.Town );
      pop.setLevel( 1 );
      region.addPopCenter( pop );
    }

    // Add a city, level 2.
    PopCenter pop = new PopCenter( game.generateUniqueId(), "City", 0, new Location( 8, 8 ), PopCenter.PopType.City );
    pop.setLevel( 2 );
    region.addPopCenter( pop );
    return region;
  }
}
