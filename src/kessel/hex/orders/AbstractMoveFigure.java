package kessel.hex.orders;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Army;
import kessel.hex.domain.Figure;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.King;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Location;
import kessel.hex.util.HexCalculator;

import java.util.Map;

/** Move a figure's (agent, Diplomat, etc) base of operations. */
public abstract class AbstractMoveFigure<T extends Figure> extends Order<T>
{
  // Use for json persistence.
  public static final String NEW_BASE_ID = "newBaseId";

  /** The game item that's the figure's new base of operations. */
  protected GameItem _newBase;
  protected transient Integer _jsonNewBaseId;
  protected transient boolean _newBaseIsMissing = false;

  protected AbstractMoveFigure() { super(); } // GSON only

  /** @param newBase the army or pop center the figure is making it's base of operations. */
  protected AbstractMoveFigure( T figure, GameItem newBase )
  {
    super( figure );
    _newBase = newBase;
  }

  public void processOrder( Game game )
  {
    _subject.setBase( _newBase );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleIsCharmed( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleIsKing( game ) ) return false;
    if ( handleIllegalDestination( game ) ) return false;
    if ( handleOutOfRange( game ) ) return false;
    if ( handleAlreadyActedThisTurn( game ) ) return false;
    if ( handleInactiveArmyTarget( game ) ) return false;
    if ( handleNotEnoughMoney( game ) ) return false;
    return true;
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _newBaseIsMissing )
    {
      String message = _subject.getName() + " cannot move to " + _newBase.getName() + " since it does not exist.";
      addPlayerEvent( game, _subject, message, _newBase.getLocation() );
      return true;
    }
    return false;
  }

  private boolean handleIsKing( Game game )
  {
    if ( _subject instanceof King )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " is the King and can not leave the capitol." );
      return true;
    }
    return false;
  }

  private boolean handleIllegalDestination( Game game )
  {
    boolean isNewBasePop = _newBase instanceof PopCenter;
    boolean isNewBaseSamePlayerArmy = _subject.getOwner().getArmy( _newBase.getId() ) != null;
    if ( !isNewBasePop && !isNewBaseSamePlayerArmy )
    {
      addPlayerEvent( game, _subject,
        _subject.getName() + " tried to move to an area without a population center or friendly army." );
      return true;
    }
    return false;
  }

  private boolean handleOutOfRange( Game game )
  {
    if ( !moveIsInRange() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " tried to move further than was possible." );
      return true;
    }
    return false;
  }

  private boolean handleInactiveArmyTarget( Game game )
  {
    if ( (_newBase instanceof Army) && !((Army) _newBase).isActive() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " tried to move to the inactive army " + _newBase.getName() );
      return true;
    }
    return false;
  }

  private boolean handleNotEnoughMoney( Game game )
  {
    if ( _subject.getOwner().getGold() < getOrderCost() )
    {
      addPlayerEvent( game, _subject, "Can not move " + _subject.getName() + " as there isn't enough gold in the treasury." );
      return true;
    }
    return false;
  }

  public int getOrderCost()
  {
    // Moving is cheaper than real orders.
    int cost = (_subject.getLevel() * Game.BASE_HAMLET_PRODUCTION * Game.GOLD_GRANULARITY) / 10;
    return cost;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " move to " + _newBase.getShortStatusName() + _newBase.getLocation().getCoord();
  }

  protected boolean moveIsInRange()
  {
    Location oldLocation = _subject.getLocation();
    Location newLocation = _newBase.getLocation();
    int distance = HexCalculator.calculateDistance( oldLocation.getCoord(), newLocation.getCoord() );
    return distance <= _subject.getRange();
  }

  public GameItem getNewBase() { return _newBase; }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( NEW_BASE_ID, _newBase.getId() );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonNewBaseId = context.deserialize( jsonOrder.getAsJsonObject().get( NEW_BASE_ID ), Integer.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _newBase = game.getItem( _jsonNewBaseId );
    if ( _newBase == null ) { _newBaseIsMissing = true; }
  }
}
