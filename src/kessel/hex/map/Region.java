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
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.util.Tuple;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static kessel.hex.map.ControlLevel.*;

/** A region on the game board. */
public class Region
{
  public static final Region UNKNOWN_REGION = new Region( "Unknown" );

  private String _name;
  private List<Location> _locations = new ArrayList<>();
  List<Tuple> _jsonLocTuples = new ArrayList<>();
  private List<PopCenter> _popCenters = new ArrayList<>();
  List<Integer> _jsonPopIds = new ArrayList<>();

  public Region( String name )
  {
    _name = name;
  }

  public String getName() { return _name; }
  public void setName( String name ) { _name = name; }

  public List<Location> getLocations() { return _locations; }

  public void addLocation( Location location ) { _locations.remove( location ); _locations.add( location ); }
  public void removeLocation( Location viewLocation ) { _locations.remove( viewLocation ); }

  public List<PopCenter> getPopCenters() { return _popCenters; }

  public void addPopCenter( PopCenter popCenter )
  {
    _popCenters.remove( popCenter ); // clear out any old reference to the pop.
    _popCenters.add( popCenter );    // use the new reference.
  }

  public int getTotalPopLevels()
  {
    int total = 0;
    for ( PopCenter popCenter : _popCenters )
    {
      total += popCenter.getLevel();
    }
    return total;
  }

  /**
   * @param player   the player gaining the knowledge.
   * @param popTypes the pop types to be discovered
   * @param turn     the turn the info was gained.
   */
  public void gainRegionalPopKnowledge( Player player, List<PopCenter.PopType> popTypes, int turn )
  {
    List<PopCenter> popsInRegion = new ArrayList<>( _popCenters );
    popsInRegion.removeAll( player.getKnownItems() );
    for ( PopCenter popCenter : popsInRegion )
    {
      if ( popTypes.contains( popCenter.getType() ) )
      {
        PopCenter intelPop = new PopCenter( popCenter );
        intelPop.setOwner( Player.UNKNOWN );
        intelPop.setLevel( PopCenter.VALUE_UNKNOWN );
        intelPop.setTurnSeen( turn );
        player.addKnownItem( intelPop );

        player.addKnownLocation( popCenter.getLocation() );
      }
    }
  }

  public boolean equals( Object o )
  {
    Region that = (Region) o;
    return _name.equals( that._name );
  }

  public int hashCode()
  {
    return _name.hashCode();
  }

  public ControlLevel getControlLevel( Player player )
  {
    int playerLevel = 0;
    boolean ownsCity = false;
    for ( PopCenter popCenter : _popCenters )
    {
      if ( popCenter.getOwner().equals( player ) )
      {
        playerLevel += popCenter.getLevel();
        if ( popCenter.getType() == PopCenter.PopType.City )
        {
          ownsCity = true;
        }
      }
    }
    int playerPercentage = (playerLevel * 100) / getTotalPopLevels();
    if ( playerPercentage < Presence.getPercentRequired() )
    {
      return None;
    }
    else if ( playerPercentage < Control.getPercentRequired() )
    {
      return Presence;
    }
    else if ( playerPercentage < Domination.getPercentRequired() )
    {
      return Control;
    }
    else
    {
      return ownsCity ? Domination : Control;
    }
  }

  public static class MyJsonAdapter implements JsonSerializer<Region>, JsonDeserializer<Region>
  {
    private static final String NAME = "name";
    private static final String LOCS = "locationReferences";
    private static final String POPS = "popReferences";

    public JsonElement serialize( Region region, Type type, JsonSerializationContext context )
    {
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.add( NAME, context.serialize( region._name ) );
      jsonResponse.add( LOCS, context.serialize( getLocationReferences( region ) ) );
      jsonResponse.add( POPS, context.serialize( getPopReferences( region ) ) );
      return jsonResponse;
    }

    private List<Tuple> getLocationReferences( Region region )
    {
      List<Tuple> locationReferences = new ArrayList<>();
      for ( Location location : region._locations )
      {
        locationReferences.add( location.getCoord() );
      }
      return locationReferences;
    }

    private List<Integer> getPopReferences( Region region )
    {
      List<Integer> popReferences = new ArrayList<>();
      for ( PopCenter popCenter : region.getPopCenters() )
      {
        popReferences.add( popCenter.getId() );
      }
      return popReferences;
    }

    public Region deserialize( JsonElement jsonOrder, Type type, JsonDeserializationContext context )
      throws JsonParseException
    {
      Region region = new Region( context.<String>deserialize( jsonOrder.getAsJsonObject().get( NAME ), String.class ) );
      region._jsonLocTuples = context.deserialize( jsonOrder.getAsJsonObject().get( LOCS ), new TypeToken<List<Tuple>>(){}.getType() );
      region._jsonPopIds = context.deserialize( jsonOrder.getAsJsonObject().get( POPS ), new TypeToken<List<Integer>>(){}.getType() );
      return region;
    }
  }

  public void fixDeserializationReferences( Game game )
  {
    fixLocationsDeserializationReferences( game );
    fixPopCentersDeserializationReferences( game );
  }

  private void fixLocationsDeserializationReferences( Game game )
  {
    _locations = new ArrayList<>();
    for ( Tuple tuple : _jsonLocTuples )
    {
      Location location = game.getMap().getLocation( tuple );
      location.setRegion( this );
      _locations.add( location );
    }
  }

  private void fixPopCentersDeserializationReferences( Game game )
  {
    _popCenters = new ArrayList<>();
    for ( Integer popId : _jsonPopIds )
    {
      _popCenters.add( game.getPopCenter( popId ) );
    }
  }
}
