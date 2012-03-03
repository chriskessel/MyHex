package kessel.hex.map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import kessel.hex.domain.Game;
import kessel.hex.domain.PopCenter;
import kessel.hex.util.Tuple;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Creates the Game. This includes creating the map, the towns, the regions, etc. */
@SuppressWarnings({ "ObjectEquality" })
public class GameMap
{
  private static final Logger LOG = Logger.getLogger( GameMap.class );

  private int _width, _height;
  private Location[][] _locations;
  private List<Region> _regions = new ArrayList<>();
  private Game _game;

  /** Locations by tuple is such a common need, it's computed once and cached. */
  private Map<Tuple, Location> _locationsByHex;

  private GameMap() {} // Used only for testing

  /**
   * This creates a square map that gets as close a possible to the size neccessary, without going over, to support the combination of
   * players/regions/etc.
   */
  @SuppressWarnings({ "SuspiciousNameCombination" })
  public GameMap( int numPlayers, int hexesPerPlayer )
  {
    int totalHexes = numPlayers * hexesPerPlayer;
    int tmpWidth = (int) Math.sqrt( totalHexes );
    _width = tmpWidth + (tmpWidth % 2); // width must be even to handle wrap around maps
    _height = _width;
    initLocations();
  }

  public static GameMap createBySize( int width, int height )
  {
    GameMap map = new GameMap();
    map._width = width;
    map._height = height;
    map.initLocations();
    return map;
  }

  private void cacheLocationsByHex()
  {
    _locationsByHex = new HashMap<>();
    for ( int x = 0; x < _width; x++ )
    {
      for ( int y = 0; y < _height; y++ )
      {
        _locationsByHex.put( new Tuple( x, y ), _locations[x][y] );
      }
    }
  }

  void initLocations()
  {
    Location[][] locations = _locations == null ? new Location[_width][_height] : _locations;
    for ( int x = 0; x < _width; x++ )
    {
      for ( int y = 0; y < _height; y++ )
      {
        if ( locations[x][y] == null )
        {
          locations[x][y] = new Location( x, y );
        }
      }
    }
    _locations = locations;
    cacheLocationsByHex();
  }

  public boolean isOnMap( Tuple hex )
  {
    List<Tuple> list = new ArrayList<>();
    list.add( hex );
    removeOffMapTuples( list, _width, _height );
    return !list.isEmpty();
  }

  static void removeOffMapTuples( List<Tuple> neighbors, int width, int height )
  {
    for ( Iterator<Tuple> iter = neighbors.iterator(); iter.hasNext(); )
    {
      Tuple neighbor = iter.next();
      if ( (neighbor.x < 0) || (neighbor.x >= width) ||
           (neighbor.y < 0) || (neighbor.y >= height) )
      {
        iter.remove();
      }
    }
  }

  public Location getLocation( Tuple tuple ) { return _locations[tuple.x][tuple.y]; }

  public Location[][] getLocations() { return _locations; }

  public void setLocations( Location[][] locations )
  {
    _locations = locations;
    cacheLocationsByHex();
  }

  public Map<Tuple, Location> getLocationsByHex() { return _locationsByHex; }

  public int getWidth() { return _locations.length; }

  public int getHeight() { return _locations[0].length; }

  public Region getRegion( String regionName )
  {
    for ( Region region : _regions )
    {
      if ( region.getName().equals( regionName ) )
      {
        return region;
      }
    }
    return null;
  }

  void clearRegions() { _regions.clear(); }

  public List<Region> getRegions() { return _regions; }

  public void addRegion( Region region ) { _regions.add( region ); }

  public Location getLocation( int x, int y ) { return _locations[x][y]; }

  public void setGame( Game game ) { _game = game; }

  public boolean equals( Object o )
  {
    return EqualsBuilder.reflectionEquals( this, o, Arrays.asList( "_game" ) );
  }

  public int hashCode()
  {
    return HashCodeBuilder.reflectionHashCode( this, Arrays.asList( "_game" ) );
  }

  public static class MyJsonAdapter implements JsonSerializer<GameMap>, JsonDeserializer<GameMap>
  {
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String LOCATIONS = "locations";
    private static final String REGIONS = "regions";

    public JsonElement serialize( GameMap map, Type type, JsonSerializationContext context )
    {
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.add( WIDTH, context.serialize( map._width ) );
      jsonResponse.add( HEIGHT, context.serialize( map._height ) );
      jsonResponse.add( LOCATIONS, context.serialize( map._locations ) );
      jsonResponse.add( REGIONS, context.serialize( map._regions ) );
      return jsonResponse;
    }

    public GameMap deserialize( JsonElement jsonOrder, Type type, JsonDeserializationContext context )
      throws JsonParseException
    {
      int width = context.deserialize( jsonOrder.getAsJsonObject().get( WIDTH ), Integer.class );
      int height = context.deserialize( jsonOrder.getAsJsonObject().get( HEIGHT ), Integer.class );
      Location[][] locations = context.deserialize( jsonOrder.getAsJsonObject().get( LOCATIONS ), new TypeToken<Location[][]>(){}.getType() );
      List<Region> regions = context.deserialize( jsonOrder.getAsJsonObject().get( REGIONS ), new TypeToken<List<Region>>(){}.getType() );
      GameMap map = createBySize( width, height );
      map._locations = locations;
      map._regions = regions;
      return map;
    }
  }

  public void fixDeserializationReferences( Game game )
  {
    _game = game;
    for ( Region region : _regions )
    {
      region.fixDeserializationReferences( game );
    }
    cacheLocationsByHex();
  }

  public String debugInfo()
  {
    StringBuilder sb = new StringBuilder( _width + " by " + _height + "\n");
    for ( Region region : _regions )
    {
      sb.append( region.getName() + " " + region.getLocations().size() + " locations, " + region.getPopCenters().size() + " pops.\n" );

    }
    return sb.toString();
  }
}
