package kessel.hex.orders.agent;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Region;
import kessel.hex.util.HexCalculator;

import java.util.Map;

/** Reduce the level of a population center. */
public class SabotagePopCenter extends AbstractAgentMission
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "targetId";

  protected PopCenter _target;
  protected transient Integer _jsonTargetId;
  protected transient boolean _targetIsMissing = false;

  public SabotagePopCenter() { super(); } // GSON only

  public SabotagePopCenter( Agent agent, PopCenter pop )
  {
    super( agent );
    _target = pop;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleTargetOutOfRange( game ) ) return false;
    return true;
  }

  protected String getMissionDescription()
  {
    return "the sabotage of " + _target.getName();
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

  private boolean handleTargetOutOfRange( Game game )
  {
    if ( !isTargetInRange() )
    {
      addPlayerEvent( game, _subject, _target.getName() + " was out of range." );
      return true;
    }
    return false;
  }

  protected boolean isTargetInRange()
  {
    int distance = HexCalculator.calculateDistance( _subject.getLocation().getCoord(), _target.getLocation().getCoord() );
    return distance <= _subject.getRange();
  }

  protected Player getTargetPlayer( Game game )
  {
    return _target.getOwner();
  }

  protected void handleAgentPromotion()
  {
    // Sabotage is risky. Always advance a level.
    _subject.incrementLevel();
  }

  protected void handleSuccess( Game game )
  {
    _target.degradeLevel();
  }

  protected void publishVictimEvent( Game game )
  {
    if ( _target.isOwned() )
    {
      String missionMessage = "The population center " + _target.getName() + " was damaged ";
      String seenMessage = (_wasSeen ? " by an agent of the " + _subject.getOwnerName() : " by unknown agents") + ".";
      String caughtMessage = _wasKilled ? "The enemy agents were caught and killed." : "";
      addPlayerEvent( game, _target, missionMessage + seenMessage + caughtMessage );
    }
  }

  /** @return the amount of resistance that must be met to succeed. */
  protected int determineResistance( Game game )
  {
    int baseResistance = 6;
    int counterEspionage = determineCounterEspionage( game, _target.getOwner() );
    return baseResistance + counterEspionage;
  }

  protected Region getMissionRegion()
  {
    return _target.getLocation().getRegion();
  }

  protected void updateViews( Game game )
  {
    // Not relevant
  }

  public String getShortDescription()
  {
    return _subject.getName() + " sabotage " + _target.getName() + _target.getLocation().getCoord();
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
