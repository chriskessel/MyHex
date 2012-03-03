package kessel.hex.orders.wizard;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;

import java.util.Map;

/** Move a wizard's base of operations. */
public class TeleportSelf extends AbstractSpell
{
  // Use for json persistence.
  public static final String NEW_BASE_ID_JSON = "newBase";

  /** The game item that's the figure's new base of operations. */
  protected GameItem _newBase;
  protected transient Integer _jsonNewBaseId;
  protected transient boolean _newBaseIsMissing = false;

  public TeleportSelf() { super(); } // GSON only
  public TeleportSelf( Wizard wizard, GameItem newBase )
  {
    super( wizard );
    _newBase = newBase;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleTargetProtected( game ) ) return false;
    if ( handleIllegalDestination( game ) ) return false;
    if ( handleAlreadyActedThisTurn( game ) ) return false;
    if ( handleInactiveArmyTarget( game ) ) return false;
    return true;
  }

  protected void processOrder( Game game )
  {
    _subject.setBase( _newBase );
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _newBaseIsMissing )
    {
      String message = _subject.getName() + " cannot teleport to " + _newBase.getName() + " since it does not exist.";
      addPlayerEvent( game, _subject, message, _newBase.getLocation() );
      return true;
    }
    return false;
  }

  private boolean handleTargetProtected( Game game )
  {
    if ( isFigureProtected( game, _subject ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " was magically shielded and could not teleport." );
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
        _subject.getName() + " tried to teleport to an area without a population center or friendly army." );
      return true;
    }
    return false;
  }

  private boolean handleInactiveArmyTarget( Game game )
  {
    if ( (_newBase instanceof Army) && !((Army) _newBase).isActive() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " tried to teleport to an inactive army " + _newBase.getName() );
      return true;
    }
    return false;
  }


  public String getShortDescription()
  {
    return _subject.getName() + " teleport to " + _newBase.getShortStatusName() + _newBase.getLocation().getCoord();
  }

  protected void updateViews( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " self teleported to " + _subject.getBase().getName() );
  }

  public GameItem getNewBase() { return _newBase; }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( NEW_BASE_ID_JSON, _newBase.getId() );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonNewBaseId = context.deserialize( jsonOrder.getAsJsonObject().get( NEW_BASE_ID_JSON ), Integer.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _newBase = game.getItem( _jsonNewBaseId );
    if ( _newBase == null ) { _newBaseIsMissing = true; }
  }
}
