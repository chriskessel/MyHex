package kessel.hex.domain;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

public class PlayerViewTest
{
  private static final Logger LOG = Logger.getLogger( PlayerViewTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
//        Game game = GameTest.createSimpleGame();
//
//        // Create some stuff for the view to know about: locations, regions, figures, etc.
//        Player playerA = game.getPlayers().get( 0 );
//        PlayerView viewA = new PlayerView();
//        Location loc = new Location( 1, 1 );
//        loc.setTerrain( Terrain.Forest );
//        Region region = new Region( "Region One" );
//        region.addLocation( loc );
//        loc.setRegion( region );
//
//        PopCenter popA = new PopCenter( GameItem.UNKNOWN_ID, "Bob", 4, loc, PopCenter.PopType.Hamlet );
//        Diplomat diplomatA = new Diplomat( GameItem.UNKNOWN_ID, "Bob", 4, popA );
//        viewA.addKnownItem( popA );
//        viewA.addKnownItem( diplomatA );
//        viewA.addKnownLocation( loc );
//        viewA.addGameEvent( new GameEvent( "test", Location.NOWHERE, 1 ) );
//        viewA.addKnownEmbassyLevel( region, playerA, 1 );
//        viewA.addKnownControlLevel( region, playerA, ControlLevel.Control );
//
//        String json = Game.GSON.toJson( viewA );
//        LOG.debug( json );
//
//        PlayerView viewB = Game.GSON.fromJson( json, PlayerView.class );
//        viewB.postLoadInit();
//        assertEquals( viewA.getKnownItems().get( 0 ).getLocation(), viewB.getKnownItems().get( 0 ).getLocation() );
//        assertEquals( 1, viewB.getKnownRegions().size() );
//        assertEquals( 1, viewB.getKnownRegions().get( 0 ).getLocationsKnown().size() );
//        assertEquals( loc, viewB.getKnownRegions().get( 0 ).getLocationsKnown().get( 0 ) );
//        assertEquals( 1, viewB.getGameEvents().size() );
//        assertEquals( 1, viewB.getRegionView( "Region One" ).getKnownEmbassyLevels().size() );
//        assertEquals( 1, viewB.getRegionView( "Region One" ).getKnownControlLevels().size() );
//        assertEquals( viewA, viewB );
  }
}
