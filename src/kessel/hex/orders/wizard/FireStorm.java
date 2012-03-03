package kessel.hex.orders.wizard;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Figure;
import kessel.hex.domain.Game;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;

import java.util.Map;

/** A fire storm rages through a pop center, killing all figures and halves the pop center's level. Capitols and cities are immune. */
public class FireStorm extends AbstractSpell
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "targetId";

  protected PopCenter _target;
  protected transient Integer _jsonTargetId;
  protected transient boolean _targetIsMissing = false;

  public FireStorm() { super(); } // GSON only
  public FireStorm( Wizard wizard, PopCenter pop )
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
    if ( handleTargetIsCapitol( game ) ) return false;
    if ( handleTargetIsCity( game ) ) return false;
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

  private boolean handleTargetIsCapitol( Game game )
  {
    if ( _target.isCapitol() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " could not fire storm the capitol " + _target.getName() );
      return true;
    }
    return false;
  }

  private boolean handleTargetIsCity( Game game )
  {
    if ( _target.getType() == PopCenter.PopType.City )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " could not fire storm the city " + _target.getName() );
      return true;
    }
    return false;
  }

  private boolean handleTargetProtected( Game game )
  {
    if ( isRegionProtected( game, _subject.getLocation().getRegion() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " could not fire storm " + _target.getName() +
                                      " because the region was magically shielded." );
      return true;
    }
    return false;
  }

  @SuppressWarnings({ "ChainOfInstanceofChecks" })
  public void processOrder( Game game )
  {
    // The pop is reduced to 1/2 it's original level (rounded up).
    int damage = _target.getLevel() / 2;
    _target.setLevel( _target.getLevel() - damage );
    publishCasterEvent( game );
    publishVictimEvent( game );

    // Every figure dies.
    for ( Figure figure : game.getAllPlayerFigures() )
    {
      if ( figure.getBase().equals( _target ) )
      {
        figure.getOwner().remove( figure );
        publishDeathEvent( game, figure );
      }
    }
  }

  private void publishDeathEvent( Game game, Figure figure )
  {
    String message = figure.getName() + " was killed by a magical fire storm!";
    addPlayerEvent( game, figure, message, _target.getLocation() );
  }

  private void publishCasterEvent( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " caused a fire storm to ravage " + _target.getName() );
  }

  protected void publishVictimEvent( Game game )
  {
    addPlayerEvent( game, _target, _target.getName() + " was ravaged by a magical fire storm!" );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " fire storm " + _target.getName() + _target.getLocation().getCoord();
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
