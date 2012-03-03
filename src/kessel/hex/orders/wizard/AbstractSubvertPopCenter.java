package kessel.hex.orders.wizard;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Game;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;

import java.util.List;
import java.util.Map;

/** Take control of a non-capitol pop center. */
public abstract class AbstractSubvertPopCenter extends AbstractSpell
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "targetId";

  protected PopCenter _target;
  protected transient Integer _jsonTargetId;
  protected transient boolean _targetIsMissing = false;
  protected transient Player _originalOwner;

  protected AbstractSubvertPopCenter() { super(); } // GSON only
  protected AbstractSubvertPopCenter( Wizard wizard, PopCenter pop )
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
    if ( handleTargetTooBig( game ) ) return false;
    if ( handleTargetIsCapitol( game ) ) return false;
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

  private boolean handleTargetTooBig( Game game )
  {
    if ( !getSubvertablePopTypes().contains( _target.getType() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " could not subvert " + _target.getName() +
                                      " as it was too big a population center for the spell." );
      return true;
    }
    return false;
  }

  private boolean handleTargetIsCapitol( Game game )
  {
    if ( _target.isCapitol() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " could not subvert the capitol " + _target.getName() );
      return true;
    }
    return false;
  }

  private boolean handleTargetProtected( Game game )
  {
    if ( isRegionProtected( game, _target.getLocation().getRegion() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " could not subvert " + _target.getName() +
                                      " because the region was magically shielded." );
      return true;
    }
    return false;
  }

  public void processOrder( Game game )
  {
    _originalOwner = _target.getOwner();
    _originalOwner.remove( _target );
    _subject.getOwner().add( _target );
    publishCasterEvent( game );
    publishVictimEvent( game );
  }

  protected void publishCasterEvent( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " subverted the " + _target.getType().name() + " of " + _target.getName() );
  }

  protected void publishVictimEvent( Game game )
  {
    addPlayerEvent( game, _target, _target.getName() + " was magically subverted by the " + _subject.getOwnerName() );
  }

  protected void updateViews( Game game )
  {
    PopCenter intelPop = new PopCenter( (PopCenter) _subject.getBase() );
    _originalOwner.addKnownItem( intelPop );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " subvert " + _target.getName() + _target.getLocation().getCoord();
  }

  protected abstract List<PopCenter.PopType> getSubvertablePopTypes();

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
