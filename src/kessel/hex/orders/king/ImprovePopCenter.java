package kessel.hex.orders.king;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Game;
import kessel.hex.domain.King;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Location;
import kessel.hex.orders.Order;

import java.util.Map;

/** Increase the level of a population center. */
public class ImprovePopCenter extends Order<King>
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "_targetId";

  protected PopCenter _target;
  protected transient Integer _jsonTargetId;
  protected transient boolean _targetIsMissing = false;

  public ImprovePopCenter() { super(); } // GSON only.

  public ImprovePopCenter( King purchaser, PopCenter pop )
  {
    super( purchaser );
    _target = pop;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleNotMyPop( game ) ) return false;
    if ( handleNotEnoughMoney( game ) ) return false;
    return true;
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing )
    {
      String message = _target.getName() + " does not exist and cannot execute the improvement order.";
      addPlayerEvent( game, _subject, message, _target.getLocation() );
      return true;
    }
    return false;
  }

  private boolean handleNotMyPop( Game game )
  {
    if ( !_target.getOwner().equals( _subject.getOwner() ) )
    {
      String message = _subject.getName() + " does not own " + _target.getName() + " and thus cannot execute the improvement order.";
      addPlayerEvent( game, _subject, message, _target.getLocation() );
      return true;
    }
    return false;
  }

  private boolean handleNotEnoughMoney( Game game )
  {
    if ( _subject.getOwner().getGold() < getOrderCost() )
    {
      String message = _subject.getName() + " lacked sufficient funds to improve the population center " + _target.getName() + ".";
      addPlayerEvent( game, _subject, message, _target.getLocation() );
      return true;
    }
    return false;
  }

  public void processOrder( Game game )
  {
    _target.improveLevel();
  }

  public int getOrderCost()
  {
    return Game.GOLD_GRANULARITY * Game.BASE_HAMLET_PRODUCTION * 2;
  }

  public Location getOrderLocation()
  {
    return _target.getLocation();
  }

  public String getShortDescription()
  {
    return "Improve " + _target.getName() + _target.getLocation().getCoord();
  }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( TARGET_ID_JSON, _target.getId() );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonTargetId = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_ID_JSON ), Integer.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _target = game.getPopCenter( _jsonTargetId );
    if ( _target == null ) { _targetIsMissing = true; }
  }
}
