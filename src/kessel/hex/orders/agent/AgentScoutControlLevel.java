package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Region;

/** An agent gives full control level information for a region. */
public class AgentScoutControlLevel extends AbstractAgentScoutRegion
{
  public AgentScoutControlLevel() { super(); } // GSON only

  public AgentScoutControlLevel( Agent agent, PopCenter target )
  {
    super( agent, target );
  }

  protected void updateViews( Game game )
  {
    Player executingPlayer = _subject.getOwner();
    Region scoutedRegion = getMissionRegion();
    for ( Player player : game.getPlayers() )
    {
      executingPlayer.addKnownControlLevel( scoutedRegion, player, player.getControlLevel( scoutedRegion ) );
    }
  }

  public String getShortDescription()
  {
    return _subject.getName() + " scout control levels in " + _regionalCity.getLocation().getRegion().getName();
  }

  protected String getMissionDescription()
  {
    return "the scouting of control levels in " + _regionalCity.getLocation().getRegion().getName();
  }
}
