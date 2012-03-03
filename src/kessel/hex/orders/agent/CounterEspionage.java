package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.domain.Player;

/**
 * Execute counter espionage in the region of the agent. This is not without risk as failing to stop an enemy agent may result in the
 * counter agent's demise.
 */
public class CounterEspionage extends AbstractAgentMission
{
  public CounterEspionage() { super(); } // GSON only

  public CounterEspionage( Agent agent )
  {
    super( agent );
  }

  protected String getMissionDescription()
  {
    return "counter espionage in " + _subject.getLocation().getRegion().getName();
  }

  protected Player getTargetPlayer( Game game )
  {
    return Player.UNOWNED;
  }

  protected void handleAgentPromotion()
  {
    // Handled in the order this counter espionage foils.
  }

  protected void handleSuccess( Game game )
  {
    // Not relevant
  }

  public boolean makeAttempt( Game game )
  {
    // This mission always succeeds in the sense that the agent is at least trying to do the job.
    return true;
  }

  protected int determineResistance( Game game )
  {
    // Not relevant
    return 0;
  }

  protected boolean checkForAgentDeath( Game game )
  {
    // Risk is handled when failing to stop another agent.
    return false;
  }

  protected void handleFateOfCounterAgents( Game game, Player targetPlayer )
  {
    // Not relevant
  }

  public String getShortDescription()
  {
    return _subject.getName() + " perform counter espionage in " + _subject.getLocation().getRegion().getName();
  }
}
