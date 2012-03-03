package kessel.hex.orders.wizard;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Game;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;

import java.util.Map;

/** Reduce the level of a population center. */
public class DamagePopCenter extends AbstractSpell
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "targetId";

  protected PopCenter _target;
  protected transient Integer _jsonTargetId;
  protected transient boolean _targetIsMissing = false;

  public DamagePopCenter() { super(); } // GSON only
  public DamagePopCenter( Wizard wizard, PopCenter pop )
  {
    super( wizard );
    _target = pop;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleTargetNotInRegion( game ) ) return false;
    if ( handleTargetProtected( game ) ) return false;
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

  private boolean handleTargetNotInRegion( Game game )
  {
    if ( !_target.getLocation().getRegion().equals( _subject.getLocation().getRegion() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " was not in the same region as the target." );
      return true;
    }
    return false;
  }

  private boolean handleTargetProtected( Game game )
  {
    if ( isRegionProtected( game, _subject.getLocation().getRegion() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " could not damage " + _target.getName() +
                                      " because the region was magically shielded." );
      return true;
    }
    return false;
  }

  public void processOrder( Game game )
  {
    _target.degradeLevel();
    publishCasterEvent( game );
    publishVictimEvent( game );
  }

  private void publishCasterEvent( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " magically damaged " + _target.getName() );
  }

  protected void publishVictimEvent( Game game )
  {
    addPlayerEvent( game, _target, _target.getName() + " was damaged by magical storms." );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " damage " + _target.getName() + _target.getLocation().getCoord();
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
