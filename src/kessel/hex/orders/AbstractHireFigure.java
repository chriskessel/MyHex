package kessel.hex.orders;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Game;
import kessel.hex.domain.King;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Location;

import java.util.Map;

/** Purchase a figure. */
public abstract class AbstractHireFigure extends Order<King>
{
  // Use for json persistence.
  public static final String BASE_ID_JSON = "baseId";

  protected PopCenter _base;
  protected transient Integer _jsonBaseId;
  private transient boolean _baseIsMissing = false;

  protected AbstractHireFigure() { super(); } // GSON only.

  protected AbstractHireFigure( King purchaser, PopCenter base )
  {
    super( purchaser );
    _base = base;
  }

  @SuppressWarnings({ "SimplifiableIfStatement", "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleMissingBase( game ) ) return false;
    if ( handleNotBaseOwner( game ) ) return false;
    if ( handleNotEnoughMoney( game ) ) return false;
    return true;
  }

  private boolean handleMissingBase( Game game )
  {
    if ( _baseIsMissing )
    {
      addPlayerEvent( game, _subject, "Can not hire as the specified population center does not exist." );
      return true;
    }
    return false;
  }

  private boolean handleNotBaseOwner( Game game )
  {
    if ( !_subject.getOwner().equals( _base.getOwner() ) )
    {
      addPlayerEvent( game, _subject, "Can not hire as the specified population center is not under our control." );
      return true;
    }
    return false;
  }

  protected boolean handleNotEnoughMoney( Game game )
  {
    if ( _subject.getOwner().getGold() < getOrderCost() )
    {
      addPlayerEvent( game, _subject, "Can not hire as there isn't enough gold in the treasury." );
      return true;
    }
    return false;
  }

  @Override // The base is the one that executes the order. The King is only necessary to know which kingdom is hiring.
  protected void trackOrderExecuted()
  { _base.addOrderExecuted( this ); }

  public Location getOrderLocation()
  {
    return _base.getLocation();
  }

  public PopCenter getBase() { return _base; }


  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( BASE_ID_JSON, _base.getId() );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonBaseId = context.deserialize( jsonOrder.getAsJsonObject().get( BASE_ID_JSON ), Integer.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _base = game.getPopCenter( _jsonBaseId );
    if ( _base == null ) { _baseIsMissing = true; }
  }
}
