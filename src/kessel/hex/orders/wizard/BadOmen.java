package kessel.hex.orders.wizard;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Game;
import kessel.hex.domain.King;
import kessel.hex.domain.Wizard;

import java.util.Map;

/** Reduce a king's power. A king can fall victim to this only once a turn. */
public class BadOmen extends AbstractSpell
{
  public static final String TARGET_ID_JSON = "targetId";

  protected King _target;
  protected transient Integer _jsonTargetId;
  protected transient boolean _targetIsMissing = false;

  public BadOmen() { super(); } // GSON only
  public BadOmen( Wizard wizard, King target )
  {
    super( wizard );
    _target = target;
  }

  @SuppressWarnings({ "IfStatementWithIdenticalBranches" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    else if ( handleTargetMissing( game ) ) return false;
    else if ( handleAlreadyDamaged( game ) ) return false;
    else if ( handleTargetProtected( game ) ) return false;
    return true;
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing )
    {
      addPlayerEvent( game, _subject, "The target does not exist." );
      return true;
    }
    return false;
  }

  private boolean handleAlreadyDamaged( Game game )
  {
    game.getCurrentTurn().getOrdersOfType( BadOmen.class );
    for ( BadOmen badOmen : game.getCurrentTurn().getOrdersOfType( BadOmen.class ) )
    {
      if ( badOmen._wasExecuted && badOmen._target.equals( _target ) )
      {
        // We just don't want the victim hit twice, but the caster doesn't get to know that.
        addPlayerEvent( game, _subject, _subject.getName() + " found the " +
                                        _target.getOwnerName() + " king was already subject to a Bad Omen." );
        return true;
      }
    }
    return false;
  }

  private boolean handleTargetProtected( Game game )
  {
    if ( isFigureProtected( game, _target ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " was unable to create bad omens on the protected target " +
                                      _target.getName() + "(" + _target.getOwnerName() + ")" );
      return true;
    }
    return false;
  }

  protected void processOrder( Game game )
  {
    _target.getOwner().adjustPower( -1 );
  }

  public King getTarget() { return _target; }

  public String getShortDescription()
  {
    return _subject.getName() + " magically damage the power of " + _target.getOwnerName();
  }

  protected void updateViews( Game game )
  {
    publishCasterEvent( game );
    publishVictimEvent( game );
  }

  private void publishCasterEvent( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " magically damaged the power " + _target.getOwnerName() );
  }

  private void publishVictimEvent( Game game )
  {
    addPlayerEvent( game, _target, "Our king's power was magically damaged!" );
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
}
