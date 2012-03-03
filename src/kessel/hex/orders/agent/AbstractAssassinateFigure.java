package kessel.hex.orders.agent;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Agent;
import kessel.hex.domain.Figure;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Player;
import kessel.hex.map.Region;
import kessel.hex.util.HexCalculator;

import java.util.Map;

/** Common behavior for all assassination missions. */
public abstract class AbstractAssassinateFigure<T extends Figure> extends AbstractAgentMission
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "targetId";
  public static final String TARGET_BASE_ID_JSON = "targetBaseId";

  protected T _target;
  protected transient Integer _jsonTargetId;
  protected GameItem _targetBase;
  protected transient Integer _jsonTargetBaseId;

  protected transient boolean _targetIsMissing = false;
  protected transient boolean _targetBaseIsMissing = false;

  protected AbstractAssassinateFigure() { super(); } // GSON only
  protected AbstractAssassinateFigure( Agent subject, T target, GameItem targetBase )
  {
    super( subject );
    _target = target;
    _targetBase = targetBase;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleTargetNotAtBase( game ) ) return false;
    if ( handleTargetOutOfRange( game ) ) return false;
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

  private boolean handleTargetNotAtBase( Game game )
  {
    if ( _targetBaseIsMissing )
    {
      addPlayerEvent( game, _subject, "The target base does not exist" );
      return true;
    }
    else if ( !_targetBase.equals( _target.getBase() ) )
    {
      addPlayerEvent( game, _subject, _target.getName() + " was not at the expected location " + _targetBase.getName() );
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

  protected String getMissionDescription()
  {
    return "the assassination of " + _target.getName() + " from " + _target.getOwner().getName();
  }

  protected Player getTargetPlayer( Game game )
  {
    return _target.getOwner();
  }

  protected void handleSuccess( Game game )
  {
    _target.getOwner().remove( _target );
  }

  protected void handleAgentPromotion()
  {
    // Assassination is risky. Always advance a level.
    _subject.incrementLevel();
  }

  /** @return the amount of resistance that must be met to succeed. */
  protected int determineResistance( Game game )
  {
    int figureResistance = _target.getAssassinationDifficulty();
    if ( isCharmed( game, _target ) )
    {
      figureResistance = figureResistance / 2;
    }
    int counterEspionage = determineCounterEspionage( game, _target.getOwner() );
    return figureResistance + counterEspionage;
  }

  protected Region getMissionRegion()
  {
    return _target.getLocation().getRegion();
  }

  protected void publishVictimEvent( Game game )
  {
    String resultMessage = _target.getName() + (_wasSuccessful ? " was assassinated" : " survived an assassination attempt");
    String seenMessage = (_wasSeen ? " by an agent of the " + _subject.getOwnerName() : " by unknown agents") + ".";
    String caughtMessage = _wasKilled ? "The enemy agents were caught and killed." : "";
    addPlayerEvent( game, _target, resultMessage + seenMessage + caughtMessage );
  }

  protected void updateViews( Game game )
  {
    // If the mission succeeded, the agent's owner knows the target is gone from the game.
    if ( _wasSuccessful )
    {
      _subject.getOwner().removeKnownItem( _target );
    }
  }

  public String getShortDescription()
  {
    return _subject.getName() + " assassinate " + _target.getShortStatusName() + " at " + _target.getLocation().getCoord();
  }

  public GameItem getTargetBase() { return _targetBase; }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( TARGET_ID_JSON, _target.getId() );
    map.put( TARGET_BASE_ID_JSON, _targetBase.getId() );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonTargetId = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_ID_JSON ), Integer.class );
    _jsonTargetBaseId = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_BASE_ID_JSON ), Integer.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _target = (T) game.getItem( _jsonTargetId );
    if ( _target == null ) { _targetIsMissing = true; }
    _targetBase = game.getItem( _jsonTargetBaseId );
    if ( _targetBase == null )  { _targetBaseIsMissing = true; }
  }
}
