package kessel.hex.orders.wizard;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Agent;
import kessel.hex.domain.Army;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Figure;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.King;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractMoveFigure;
import kessel.hex.orders.agent.MoveAgent;
import kessel.hex.orders.diplomat.MoveDiplomat;

import java.util.Map;

/**
 * Move another figure's base of operations. The move is silent, has infinite range, and cannot be blocked (unlike a diplomat's move). It
 * also has the advantage of not counting as an action for the target figure, though it does cost the target figure's normal move cost.
 */
public class TeleportFigure extends AbstractSpell
{
  // Use for json persistence.
  public static final String NEW_BASE_ID_JSON = "newBase";
  public static final String TARGET_ID_JSON = "targetId";

  /** The game item that's the figure's new base of operations. */
  protected GameItem _newBase;
  protected transient Integer _jsonNewBaseId;
  protected transient boolean _newBaseIsMissing = false;

  /** Who to move. */
  protected Figure _target;
  protected transient Integer _jsonTargetId;
  protected transient boolean _targetIsMissing = false;

  public TeleportFigure() { super(); } // GSON only
  public TeleportFigure( Wizard wizard, Figure target, GameItem newBase )
  {
    super( wizard );
    _newBase = newBase;
    _target = target;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleNewBaseMissing( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleTargetProtected( game ) ) return false;
    if ( handleIllegalDestination( game ) ) return false;
    if ( handleInactiveArmyTarget( game ) ) return false;
    if ( handleTargetNotMine( game ) ) return false;
    if ( handleTargetIsKing( game ) ) return false;
    return true;
  }

  protected void processOrder( Game game )
  {
    _target.setBase( _newBase );
  }

  private boolean handleNewBaseMissing( Game game )
  {
    if ( _newBaseIsMissing )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " cannot teleport " + _target.getName() +
                                      " because the destination " + _newBase.getName() + " does not exist." );
      return true;
    }
    return false;
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing )
    {
      addPlayerEvent( game, _subject,
        _subject.getName() + " cannot teleport " + _target.getName() + " since the figure doesn't exist." );
      return true;
    }
    return false;
  }

  private boolean handleTargetProtected( Game game )
  {
    if ( isFigureProtected( game, _subject ) )
    {
      addPlayerEvent( game, _subject,
        _subject.getName() + " was unable to teleport " + _target.getName() + " due to magic shielding." );
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
      addPlayerEvent( game, _subject, _subject.getName() + " tried to teleport " + _target.getName() +
                                      " to an area without a population center or friendly army." );
      return true;
    }
    return false;
  }

  private boolean handleInactiveArmyTarget( Game game )
  {
    if ( (_newBase instanceof Army) && !((Army) _newBase).isActive() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " tried to teleport " + _target.getName() +
                                      " to an inactive army " + _newBase.getName() );
      return true;
    }
    return false;
  }

  private boolean handleTargetNotMine( Game game )
  {
    if ( !_target.getOwner().equals( _subject.getOwner() ) )
    {
      addPlayerEvent( game, _subject,
        _subject.getName() + " cannot teleport " + _target.getName() + " since they aren't of our kingdom." );
      return true;
    }
    return false;
  }

  private boolean handleTargetIsKing( Game game )
  {
    if ( _target instanceof King )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " cannot teleport the king." );
      return true;
    }
    return false;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " teleport " + _target.getName() + " to " +
           _newBase.getShortStatusName() + _newBase.getLocation().getCoord();
  }

  protected void updateViews( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " teleported " + _target.getName() + " to " + _target.getBase().getName() );
    String message = _target.getName() + " was teleported to " + _target.getBase().getName();
    addPlayerEvent( game, _target, message );
  }

  @SuppressWarnings({ "ChainOfInstanceofChecks" })
  public int getOrderCost()
  {
    // The cost is the same as what the figure would normally require to move.
    AbstractMoveFigure fakeMove;
    if ( _target instanceof Diplomat )
    {
      fakeMove = new MoveDiplomat( (Diplomat) _target, null );
    }
    else if ( _target instanceof Agent )
    {
      fakeMove = new MoveAgent( (Agent) _target, null );
    }
    else if ( _target instanceof Wizard )
    {
      fakeMove = new MoveWizard( (Wizard) _target, null );
    }
    else
    {
      return 0;
    }
    return fakeMove.getOrderCost();
  }

  public GameItem getNewBase() { return _newBase; }
  public GameItem getTarget() { return _target; }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( NEW_BASE_ID_JSON, _newBase.getId() );
    map.put( TARGET_ID_JSON, _target.getId() );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonNewBaseId = context.deserialize( jsonOrder.getAsJsonObject().get( NEW_BASE_ID_JSON ), Integer.class );
    _jsonTargetId = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_ID_JSON ), Integer.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _target = (Figure) game.getItem( _jsonTargetId );
    if ( _target == null ) { _targetIsMissing = true; }
    _newBase = game.getItem( _jsonNewBaseId );
    if ( _newBase == null ) { _newBaseIsMissing = true; }
  }
}
