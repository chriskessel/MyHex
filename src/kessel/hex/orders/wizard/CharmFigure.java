package kessel.hex.orders.wizard;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Figure;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Player;
import kessel.hex.domain.Wizard;

import java.util.Map;

/** Charm a figure, which prevents them from executing any orders for the turn. */
public class CharmFigure extends AbstractSpell
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "targetId";

  protected Figure _target;
  protected transient Integer _jsonTargetId;
  protected transient boolean _targetIsMissing = false;

  public CharmFigure() { super(); } // GSON only
  public CharmFigure( Wizard wizard, Figure target )
  {
    super( wizard );
    _target = target;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleTargetProtected( game ) ) return false;
    if ( handleTargetWizard( game ) ) return false;
    return true;
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " was unable to locate " + _target.getName() + " to attempt the charm." );
      return true;
    }
    return false;
  }

  private boolean handleTargetProtected( Game game )
  {
    if ( isFigureProtected( game, _target ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " was unable to charm the protected target " +
                                      _target.getName() + "(" + _target.getOwnerName() + ")" );
      return true;
    }
    return false;
  }

  private boolean handleTargetWizard( Game game )
  {
    if ( _target instanceof Wizard )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " did not attempt to charm " +
                                      _target.getName() + " of the " + _target.getOwnerName() + " as wizards are immune." );
      return true;
    }
    return false;
  }

  protected void processOrder( Game game )
  {
    publishSuccessEvent( game );
    publishVictimEvent( game );

    // Nothing else to do. It's the responsibility of each order that can be affected by charm to check Order.isCharmed()
    // and act accordingly.
  }

  private void publishSuccessEvent( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " feels confident the charm on " +
                                    _target.getName() + "(" + _target.getOwnerName() + ")" + " was successful." );
  }

  private void publishVictimEvent( Game game )
  {
    addPlayerEvent( game, _target, _target.getName() + " fell victim to a charm." );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " charm " + _target.getName() + " of the " + _target.getOwnerName();
  }

  public Figure getTarget() { return _target; }

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
    _target = (Figure) game.getItem( _jsonTargetId );
    if ( _target == null ) { _targetIsMissing = true; }
  }
}
