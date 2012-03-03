package kessel.hex.orders.wizard;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Game;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;

import java.util.Map;

/** Shield a pop center, which prevents any army attacks against it that turn. */
public class MagicDome extends AbstractSpell
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "targetId";

  protected PopCenter _target;
  protected transient Integer _jsonTargetId;
  protected transient boolean _targetIsMissing = false;

  public MagicDome() { super(); } // GSON only
  public MagicDome( Wizard wizard, PopCenter target )
  {
    super( wizard );
    _target = target;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    return true;
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " was unable to locate " + _target.getName() + " to attempt the dome." );
      return true;
    }
    return false;
  }

  protected void processOrder( Game game )
  {
    publishSuccessEvent( game );
    publishTargetEvent( game );

    // Nothing else to do. It's the responsibility of each order that can be affected to check and act accordingly.
  }

  private void publishSuccessEvent( Game game )
  {
    addPlayerEvent( game, _subject,
      _subject.getName() + " put a magic dome around " + _target.getName() + "(" + _target.getOwnerName() + ")." );
  }

  private void publishTargetEvent( Game game )
  {
    addPlayerEvent( game, _subject, _target.getName() + " was protected by a magical dome.", _target.getLocation() );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " magically dome " + _target.getName() + "(" + _target.getOwnerName() + ")";
  }

  public PopCenter getTarget() { return _target; }

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
