package kessel.hex.orders.diplomat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Region;
import kessel.hex.util.HexCalculator;

import java.util.Arrays;
import java.util.Map;

/** Learn the locations of all pop centers of a particular type in a specific region. */
public class TakeRegionCensus extends AbstractDiplomatOrder
{
  // Used for json persistence.
  public static final String POP_TYPE = "_popType";
  public static final String TARGET_ID_JSON = "_targetId";

  protected PopCenter _regionalCity;
  protected transient Integer _jsonCityId;
  protected PopCenter.PopType _popType;
  protected transient boolean _targetIsMissing = false;

  public TakeRegionCensus() { super(); } // GSON only
  public TakeRegionCensus( Diplomat diplomat, PopCenter regionalCity, PopCenter.PopType popType )
  {
    super( diplomat );
    _regionalCity = regionalCity;
    _popType = popType;
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
    if ( _targetIsMissing )
    {
      addPlayerEvent( game, _subject, "Unable to take the region census since the given location was not a valid regional city." );
      return true;
    }
    return false;
  }

  private boolean handleOutOfRange( Game game )
  {
    if ( !isTargetInRange() )
    {
      addPlayerEvent( game, _subject, "Diplomat " + _subject.getName() + " could not reach the regional city of " +
                                      _regionalCity.getName() + " to take the regional census." );
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
    // Nothing happens. It's all in updateViews().
  }

  protected void updateViews( Game game )
  {
    Player executingPlayer = _subject.getOwner();
    Region region = _regionalCity.getLocation().getRegion();
    region.gainRegionalPopKnowledge( executingPlayer, Arrays.asList( _popType ), game.getTurn() );
  }

  public int getOrderCost()
  {
    return 7 * Game.BASE_HAMLET_PRODUCTION * Game.GOLD_GRANULARITY;
  }

  public PopCenter getRegionalCity() { return _regionalCity; }

  public String getShortDescription()
  {
    return _subject.getName() + " take a " + _popType.name() + " census of " + _regionalCity.getLocation().getRegion().getName();
  }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( TARGET_ID_JSON, _regionalCity.getId() );
    map.put( POP_TYPE, _popType );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonCityId = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_ID_JSON ), Integer.class );
    _popType = context.deserialize( jsonOrder.getAsJsonObject().get( POP_TYPE ), PopCenter.PopType.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _regionalCity = game.getPopCenter( _jsonCityId );
    if ( _regionalCity == null ) { _targetIsMissing = true; }
  }
}
