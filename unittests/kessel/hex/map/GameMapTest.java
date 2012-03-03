package kessel.hex.map;

import kessel.hex.domain.Game;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Test just things related to the map. */
public class GameMapTest
{
  private static final Logger LOG = Logger.getLogger( GameMapTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public static GameMap createSimpleMap( int width, int height )
  {
    GameMap map = GameMap.createBySize( width, height );
    return map;
  }

  @Test
  public void testPersistence()
  {
    // Create a map with at least a couple of everything interesting.
    GameMap oldMap = createSimpleMap( 2, 2 );
    Region region = new Region( "Region One" );
    oldMap.getLocations()[0][0].setTerrain( Terrain.Plain );
    oldMap.getLocations()[0][0].setRegion( region );
    oldMap.getLocations()[0][1].setTerrain( Terrain.Forest );
    oldMap.getLocations()[0][1].setRegion( region );
    region.addLocation( oldMap.getLocations()[0][0] );
    region.addLocation( oldMap.getLocations()[0][1] );
    oldMap.addRegion( region );

    String json = Game.GSON.toJson( oldMap );
    LOG.debug( json );

    GameMap newMap = Game.GSON.fromJson( json, GameMap.class );
    Game fakeGame = new Game( "fake" );
    fakeGame.setMap( newMap );
    newMap.fixDeserializationReferences( fakeGame );
    assertEquals( 1, newMap.getRegions().size() );
    assertEquals( 2, newMap.getRegions().iterator().next()._jsonLocTuples.size() );
    assertEquals( Terrain.Plain, newMap.getLocations()[0][0].getTerrain() );
    assertEquals( Terrain.Forest, newMap.getLocations()[0][1].getTerrain() );
    assertEquals( region, newMap.getLocations()[0][0].getRegion() );
    assertEquals( region, newMap.getLocations()[0][1].getRegion() );
    assertEquals( newMap, oldMap );
  }
}
