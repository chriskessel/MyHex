package kessel.hex.orders.wizard;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.Wizard;
import kessel.hex.util.Tuple;

import java.util.Map;

/** Teleport the wizard's group anywhere on the map. */
public abstract class AbstractTeleportArmy extends AbstractSpell
{
  // Use for json persistence.
  public static final String DESTINATION = "destination";

  /** The game item that's the figure's new base of operations. */
  protected Tuple _destination;

  protected AbstractTeleportArmy() { super(); } // GSON only
  protected AbstractTeleportArmy( Wizard wizard, Tuple destination )
  {
    super( wizard );
    _destination = destination;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleNotInArmy( game ) ) return false;
    if ( handleDestinationIsOffMap( game ) ) return false;
    if ( handleAlreadyActedThisTurn( game ) ) return false;
    if ( handleArmyTooBig( game ) ) return false;
    return true;
  }

  private boolean handleDestinationIsOffMap( Game game )
  {
    boolean isTargetOnMap = (_destination.x < game.getMap().getWidth()) && (_destination.x >= 0) &&
                            (_destination.y < game.getMap().getHeight()) && (_destination.y >= 0);
    if ( !isTargetOnMap )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " can not teleport the army to the off map location " + _destination );
      return true;
    }
    return false;
  }

  private boolean handleNotInArmy( Game game )
  {
    if ( !(_subject.getBase() instanceof Army) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " is not based in an army and thus could not cast the teleport." );
      return true;
    }
    return false;
  }

  private boolean handleArmyTooBig( Game game )
  {
    if ( ((Army) _subject.getBase()).getUnits().size() > determineTeleportCapacity() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " wasn't able to teleport such a large army." );
      return true;
    }
    return false;
  }

  protected abstract int determineTeleportCapacity();

  protected void processOrder( Game game )
  {
    _subject.getBase().setLocation( game.getMap().getLocation( _destination ) );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " teleport " + _subject.getBase().getName() + " to " + _destination;
  }

  protected void updateViews( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " teleported the " + _subject.getBase().getName() + " to " + _destination );
  }

  public Tuple getDestination() { return _destination; }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( DESTINATION, _destination );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _destination = context.deserialize( jsonOrder.getAsJsonObject().get( DESTINATION ), Tuple.class );
  }
}
