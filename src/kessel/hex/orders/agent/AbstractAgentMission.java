package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.domain.Player;
import kessel.hex.map.Region;
import kessel.hex.orders.Mechanics;
import kessel.hex.orders.Order;

import java.util.List;
import java.util.Random;

/** Common behavior for all agent missions. */
@SuppressWarnings({ "ClassReferencesSubclass" })
public abstract class AbstractAgentMission extends Order<Agent>
{
  static int PERCENT_SEEN = 50;
  protected static final int MINIMUM_KILLED_RISK_LEVEL = 5;
  protected static final int RISK_PER_DIFFICULTY = 2;

  protected final transient Random _r = new Random();
  protected transient boolean _wasSuccessful = false;
  protected transient boolean _wasKilled = false;
  protected transient int _agentHits;
  protected transient boolean _wasSeen = false;

  protected AbstractAgentMission() { super(); } // GSON only

  protected AbstractAgentMission( Agent subject )
  {
    super( subject );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleIsCharmed( game ) ) return false;
    if ( handleAlreadyActedThisTurn( game ) ) return false;
    if ( handleNotEnoughMoney( game ) ) return false;
    return true;
  }

  private boolean handleNotEnoughMoney( Game game )
  {
    if ( _subject.getOwner().getGold() < getOrderCost() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " lacked sufficient funds to execute the mission." );
      return true;
    }
    return false;
  }

  public void processOrder( Game game )
  {
    _wasSuccessful = makeAttempt( game );
    if ( _wasSuccessful )
    {
      handleSuccess( game );
    }

    _wasKilled = checkForAgentDeath( game );
    if ( _wasKilled )
    {
      _subject.getOwner().remove( _subject );
    }

    handleFateOfCounterAgents( game, getTargetPlayer( game ) );
    if ( _wasSuccessful )
    {
      handleAgentPromotion();
    }
    _wasSeen = _r.nextInt( 100 ) < PERCENT_SEEN;
    publishAgentEvent( game );
    publishVictimEvent( game );
  }

  /** Publish mission result for the agent's owner. */
  protected void publishAgentEvent( Game game )
  {
    String missionMessage = _subject.getName() + (_wasSuccessful ? " failed to complete " : " successfully completed ") +
                            getMissionDescription();
    String deathMessage = _wasKilled ?
                          " and was killed in the process." :
                          (" and escaped" + (_wasSeen ? " quietly." : " with armed guards in pursuit."));
    addPlayerEvent( game, _subject, missionMessage + deathMessage );
  }

  /** Publish mission result for the agent's victim. Not all missions have victims, so missions with victims should over ride this method. */
  protected void publishVictimEvent( Game game )
  {

  }

  /** @return a human readable name for the mission. */
  protected abstract String getMissionDescription();

  /** @return the player that's the target of the mission. */
  protected abstract Player getTargetPlayer( Game game );

  /** For some missions, a successful agent can advance a level. */
  protected abstract void handleAgentPromotion();

  protected abstract void handleSuccess( Game game );

  /**
   * This is the default implementation for missions requiring a roll. Missions with non-default criteria should override this.
   *
   * @return true if the mission was a success.
   */
  public boolean makeAttempt( Game game )
  {
    int difficultyLevel = determineResistance( game );
    _agentHits = Mechanics.standardLevelRoll( _subject.getLevel() );
    return _agentHits >= difficultyLevel;
  }

  /** @return the amount of resistance that must be met to succeed. */
  protected abstract int determineResistance( Game game );

  /**
   * This is the default implementation for risky missions. Missions with non-default risk should override this.
   *
   * @return true if the agent dies on the mission.
   */
  protected boolean checkForAgentDeath( Game game )
  {
    int difficultyLevel = determineResistance( game );
    int successDifference = _agentHits - difficultyLevel;
    int missionRisk = Math.max( MINIMUM_KILLED_RISK_LEVEL, (difficultyLevel - successDifference) * RISK_PER_DIFFICULTY );
    return _r.nextInt( 100 ) < missionRisk;
  }

  /** @return the amount of counter espionage. Only the best counter espionage applies (i.e. they aren't cumulative). */
  protected int determineCounterEspionage( Game game, Player targetPlayer )
  {
    int counterLevel = 0;
    Region missionRegion = getMissionRegion();
    List<CounterEspionage> counterEspionageOrders = game.getCurrentTurn().getOrdersOfType( CounterEspionage.class );
    for ( CounterEspionage order : counterEspionageOrders )
    {
      boolean affectsMyRegion = order.getSubject().getLocation().getRegion().equals( missionRegion );
      boolean wasIssuedByTarget = order.getSubject().getOwner().equals( targetPlayer );
      if ( affectsMyRegion && wasIssuedByTarget )
      {
        counterLevel = Math.max( counterLevel, order.getSubject().getLevel() );
      }
    }
    return counterLevel;
  }

  /** Should be overridden by any mission where the region is not that of the agent. */
  protected Region getMissionRegion()
  {
    return _subject.getLocation().getRegion();
  }

  /** This is the default implementation for any dangerous mission. Missions with non-standard dangers should override it. */
  protected void handleFateOfCounterAgents( Game game, Player targetPlayer )
  {
    List<CounterEspionage> counterEspionageOrders = game.getCurrentTurn().getOrdersOfType( CounterEspionage.class );
    for ( CounterEspionage order : counterEspionageOrders )
    {
      int actualMissionDifficulty = determineResistance( game );
      boolean affectsMissionRegion = order.getSubject().getLocation().getRegion().equals( getMissionRegion() );
      boolean wasIssuedByTarget = order.getSubject().getOwner().equals( targetPlayer );
      if ( affectsMissionRegion && wasIssuedByTarget )
      {
        // If the mission succeeded, the attacking agent may kill any defending agents.
        if ( _wasSuccessful )
        {
          int attacker = Mechanics.standardLevelRoll( _subject.getLevel() );
          int defender = Mechanics.standardLevelRoll( order.getSubject().getLevel() );
          if ( attacker >= 2 * defender )
          {
            order.getSubject().getOwner().remove( order.getSubject() );
            publishDeadCounterAgentEvent( game, order.getSubject() );

            // This would normally be in updateViews(), but it'd be messy to wait til that point to handle it.
            _subject.getOwner().removeKnownItem( order.getSubject() );
          }
        }
        else
        {
          // If the mission failed, the counter agent may be rewarded if they tipped the balance.
          int difficultyWithoutEspionage = actualMissionDifficulty - order.getSubject().getLevel();
          if ( _agentHits >= difficultyWithoutEspionage )
          {
            order.getSubject().incrementLevel();
          }
        }
      }
    }
  }

  protected void publishDeadCounterAgentEvent( Game game, Agent deadAgent )
  {
    addPlayerEvent( game, deadAgent, deadAgent.getName() + " was killed while performing counter espionage." );
    addPlayerEvent( game, _subject, _subject.getName() + " killed a counter agent during the mission." );
  }

  public int getOrderCost()
  {
    return (_subject.getLevel() * Game.BASE_HAMLET_PRODUCTION * Game.GOLD_GRANULARITY) / 5;
  }
}
