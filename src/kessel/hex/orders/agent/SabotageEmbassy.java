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

/** Reduce the level of a kingdom's embassy in a region. */
public class SabotageEmbassy extends AbstractAgentMission
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "targetId";
  public static final String PLAYER_ID_JSON = "playerName";

  protected PopCenter _regionalCity;
  protected transient Integer _jsonCityId;
  protected transient boolean _targetIsMissing = false;
  protected String _playerName;

  public SabotageEmbassy() { super(); } // GSON only

  public SabotageEmbassy( Agent agent, PopCenter regionCity, String playerName )
  {
    super( agent );
    _regionalCity = regionCity;
    _playerName = playerName;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleTargetOutOfRange( game ) ) return false;
    if ( handleBadPlayerName( game ) ) return false;
    return true;
  }

  protected void publishVictimEvent( Game game )
  {
    String missionMessage = "The embassy in " + _regionalCity.getLocation().getRegion().getName() + " was damaged ";
    String seenMessage = (_wasSeen ? " by an agent of the " + _subject.getOwnerName() : " by unknown agents") + ".";
    String caughtMessage = _wasKilled ? "The enemy agents were caught and killed." : "";
    addPlayerEvent( game, game.getPlayer( _playerName ), missionMessage + seenMessage + caughtMessage, _regionalCity.getLocation() );
  }

  protected String getMissionDescription()
  {
    return "the sabotage of the embassy of " + _playerName + " in " + _regionalCity.getLocation().getRegion().getName();
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing || !_regionalCity.getType().equals( PopCenter.PopType.City ) )
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
      addPlayerEvent( game, _subject, _regionalCity.getName() + " was out of range." );
      return true;
    }
    return false;
  }

  private boolean handleBadPlayerName( Game game )
  {
    if ( game.getPlayer( _playerName ) == null )
    {
      addPlayerEvent( game, _subject, "The target player " + _playerName + " does not exist." );
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
    return game.getPlayer( _playerName );
  }

  protected void handleAgentPromotion()
  {
    // Sabotage is risky. Always advance a level.
    _subject.incrementLevel();
  }

  protected void handleSuccess( Game game )
  {
    Player victim = game.getPlayer( _playerName );
    victim.degradeEmbassy( _regionalCity.getLocation().getRegion() );
  }

  /** @return the amount of resistance that must be met to succeed. */
  protected int determineResistance( Game game )
  {
    int baseResistance = 15;
    int counterEspionage = determineCounterEspionage( game, game.getPlayer( _playerName ) );
    return baseResistance + counterEspionage;
  }

  protected Region getMissionRegion()
  {
    return _regionalCity.getLocation().getRegion();
  }

  public String getShortDescription()
  {
    return "Sabotage embassy for " + _playerName + " in " + _regionalCity.getLocation().getRegion().getName();
  }

  public PopCenter getRegionalCity() { return _regionalCity; }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( TARGET_ID_JSON, _regionalCity.getId() );
    map.put( PLAYER_ID_JSON, _playerName );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonCityId = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_ID_JSON ), Integer.class );
    _playerName = context.deserialize( jsonOrder.getAsJsonObject().get( PLAYER_ID_JSON ), String.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _regionalCity = game.getPopCenter( _jsonCityId );
    if ( _regionalCity == null ) { _targetIsMissing = true; }
  }
}
