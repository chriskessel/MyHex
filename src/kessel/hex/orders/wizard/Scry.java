package kessel.hex.orders.wizard;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Game;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.agent.AgentReconLocation;
import kessel.hex.util.Tuple;

import java.util.Map;

/** Magically scry any hex on the board like an agent. */
public class Scry extends AbstractSpell
{
  // Use for json persistence.
  public static final String TARGET_JSON = "target";

  private Tuple _target;

  public Scry() { super(); } // GSON only
  public Scry( Wizard wizard, Tuple target )
  {
    super( wizard );
    _target = target;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetIsOffMap( game ) ) return false;
    return true;
  }

  private boolean handleTargetIsOffMap( Game game )
  {
    boolean isTargetOnMap = (_target.x < game.getMap().getWidth()) && (_target.x >= 0) &&
                            (_target.y < game.getMap().getHeight()) && (_target.y >= 0);
    if ( !isTargetOnMap )
    {
      addPlayerEvent( game, _subject, "Wizard " + _subject.getName() + " can not scry the off map location " + _target );
      return true;
    }
    return false;
  }

  protected void processOrder( Game game )
  {
    // Results are handled in updateViews().
  }

  protected void updateViews( Game game )
  {
    AgentReconLocation.reconHex( game, _subject, _target );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " scry " + _target;
  }

  public Tuple getTarget() { return _target; }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( TARGET_JSON, _target );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _target = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_JSON ), Tuple.class );
  }
}
