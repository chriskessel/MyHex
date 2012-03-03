package kessel.hex.orders.king;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Game;
import kessel.hex.domain.King;
import kessel.hex.domain.Player;
import kessel.hex.map.Region;
import kessel.hex.orders.Order;
import kessel.hex.orders.PolicyOrder;

import java.util.Map;

/** Raise a player's embassy level in a region. */
public class ImproveEmbassy extends Order<King> implements PolicyOrder
{
  String _region;

  public ImproveEmbassy() { super(); } // GSON only

  public ImproveEmbassy( King king, String region )
  {
    super( king );
    _region = region;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleIsCharmed( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleKingWasAlreadyBusy( game ) ) return false;
    if ( handleNotEnoughMoney( game ) ) return false;
    return true;
  }

  private boolean handleTargetMissing( Game game )
  {
    Region region = game.getMap().getRegion( _region );
    if ( region == null )
    {
      addPlayerEvent( game, _subject, "Unable to establish an embassy since " + _region + " is not a valid region name." );
      return true;
    }
    return false;
  }

  private boolean handleKingWasAlreadyBusy( Game game )
  {
    if ( _subject.hasIssuedPolicyOrder() )
    {
      addPlayerEvent( game, _subject, "Unable to establish an embassy since " + _subject.getName() +
                                      " has already issued a policy order this turn." );
      return true;
    }
    return false;
  }

  private boolean handleNotEnoughMoney( Game game )
  {
    if ( _subject.getOwner().getGold() < getOrderCost() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " lacked sufficient funds to improve the embassy in " + _region );
      return true;
    }
    return false;
  }

  public void processOrder( Game game )
  {
    _subject.getOwner().improveEmbassy( _region );
  }

  public String getRegion() { return _region; }

  public int getOrderCost()
  {
    return Game.GOLD_GRANULARITY * Game.BASE_HAMLET_PRODUCTION * 3;
  }

  public String getShortDescription()
  {
    return "Improve Embassy in " + _region;
  }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( REGION_NAME_JSON, _region );
    return map;
  }

  // Use for json persistence.
  public static final String REGION_NAME_JSON = "_targetId";

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _region = context.deserialize( jsonOrder.getAsJsonObject().get( REGION_NAME_JSON ), String.class );
  }
}
