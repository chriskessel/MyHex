package kessel.hex.orders.diplomat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Location;
import kessel.hex.util.HexCalculator;

import java.util.Map;

/** Learn all of the locations in a region. */
public class MapRegion extends AbstractDiplomatOrder
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "_targetId";

  protected PopCenter _regionalCity;
  protected transient Integer _jsonCityId;
  protected transient boolean _targetIsMissing = false;

  public MapRegion() { super(); } // GSON only
  public MapRegion( Diplomat diplomat, PopCenter regionalCity )
  {
    super( diplomat );
    _regionalCity = regionalCity;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleOutOfRange( game ) ) return false;
    return true;
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing || !_regionalCity.getType().equals( PopCenter.PopType.City ) )
    {
      addPlayerEvent( game, _subject, "Unable to map the region since the given location was not a valid regional city." );
      return true;
    }
    return false;
  }

  private boolean handleOutOfRange( Game game )
  {
    if ( !isTargetInRange() )
    {
      addPlayerEvent( game, _subject, "Diplomat " + _subject.getName() + " could not reach the regional city of " +
                                      _regionalCity.getName() + " to perform the map region research." );
      return true;
    }
    return false;
  }

  protected boolean isTargetInRange()
  {
    int distance = HexCalculator.calculateDistance( _subject.getLocation().getCoord(), _regionalCity.getLocation().getCoord() );
    return distance <= _subject.getRange();
  }

  public void processOrder( Game game )
  {
    // Nothing happens. It's all in updateViews()
  }

  protected void updateViews( Game game )
  {
    Player executingPlayer = _subject.getOwner();
    for ( Location location : _regionalCity.getLocation().getRegion().getLocations() )
    {
      executingPlayer.addKnownLocation( location );
    }
  }

  public int getOrderCost()
  {
    return 5 * Game.BASE_HAMLET_PRODUCTION * Game.GOLD_GRANULARITY;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " map region " + _regionalCity.getLocation().getRegion().getName();
  }

  public PopCenter getRegionalCity() { return _regionalCity; }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( TARGET_ID_JSON, _regionalCity.getId() );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonCityId = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_ID_JSON ), Integer.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _regionalCity = game.getPopCenter( _jsonCityId );
    if ( _regionalCity == null ) { _targetIsMissing = true; }
  }
}
