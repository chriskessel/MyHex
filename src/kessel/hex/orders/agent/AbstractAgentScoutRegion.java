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

/** An agent gives info about something in a region */
public abstract class AbstractAgentScoutRegion extends AbstractAgentMission
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "targetId";

  protected PopCenter _regionalCity;
  protected transient Integer _jsonCityId;
  protected transient boolean _targetIsMissing = false;

  protected AbstractAgentScoutRegion() { super(); } // GSON only
  protected AbstractAgentScoutRegion( Agent agent, PopCenter target )
  {
    super( agent );
    _regionalCity = target;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleTargetOutOfRange( game ) ) return false;
    return true;
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing || !_regionalCity.getType().equals( PopCenter.PopType.City ) )
    {
      addPlayerEvent( game, _subject, "Unable to recon since the given location was not a valid regional city." );
      return true;
    }
    return false;
  }

  private boolean handleTargetOutOfRange( Game game )
  {
    if ( !isTargetInRange() )
    {
      addPlayerEvent( game, _subject,
        "Agent " + _subject.getName() + " can not reach the regional city of " + _regionalCity.getName() );
      return true;
    }
    return false;
  }

  protected boolean isTargetInRange()
  {
    int distance = HexCalculator.calculateDistance( _subject.getLocation().getCoord(), _regionalCity.getLocation().getCoord() );
    return distance <= _subject.getRange();
  }

  protected Player getTargetPlayer( Game game )
  {
    return Player.UNOWNED;
  }

  protected void handleAgentPromotion()
  {
    // Recons have low risk, so no reward.
  }

  protected void handleSuccess( Game game )
  {
    // The order itself doesn't change anything on the game. It only affects the updateViews().
  }

  public boolean makeAttempt( Game game )
  {
    // Recons can never fail.
    return true;
  }

  protected int determineResistance( Game game )
  {
    // Not relevant for recons.
    return 0;
  }

  protected boolean checkForAgentDeath( Game game )
  {
    // Once in a while a recon is fatal for junior agents.
    return _subject.getLevel() < 3 && _r.nextInt( 20 ) == 0;
  }

  protected void handleFateOfCounterAgents( Game game, Player targetPlayer )
  {
    // Not relevant for recons.
  }

  /** Should be overridden by any mission where the region is not that of the agent. */
  protected Region getMissionRegion()
  {
    return _regionalCity.getLocation().getRegion();
  }

  public PopCenter getRegionalCity() { return _regionalCity; }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( TARGET_ID_JSON, _regionalCity.getId() );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonCityId = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_ID_JSON ), Integer.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _regionalCity = game.getPopCenter( _jsonCityId );
    if ( _regionalCity == null ) { _targetIsMissing = true; }
  }
}
