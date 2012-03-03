package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Region;

/** An agent gives full embassy information for a region. */
public class AgentScoutEmbassies extends AbstractAgentScoutRegion
{
  public AgentScoutEmbassies() { super(); } // GSON only

  public AgentScoutEmbassies( Agent agent, PopCenter target )
  {
    super( agent, target );
  }

  protected void updateViews( Game game )
  {
    Player executingPlayer = _subject.getOwner();
    Region scoutedRegion = getMissionRegion();
    for ( Player player : game.getPlayers() )
    {
      executingPlayer.addKnownEmbassyLevel( scoutedRegion, player, player.getEmbassyLevel( scoutedRegion ) );
    }
  }

  public String getShortDescription()
  {
    return _subject.getName() + " scout embassies in " + _regionalCity.getLocation().getRegion().getName();
  }

  protected String getMissionDescription()
  {
    return "the scouting of embassy levels in " + _regionalCity.getLocation().getRegion().getName();
  }
}
