package kessel.hex.orders.agent;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Agent;
import kessel.hex.domain.Army;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Figure;
import kessel.hex.domain.Game;
import kessel.hex.domain.King;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.util.HexCalculator;
import kessel.hex.util.Tuple;

import java.util.Map;

/** An agent gives full information about a location and anything at that location. */
public class AgentReconLocation extends AbstractAgentMission
{
  // Use for json persistence.
  public static final String TARGET_JSON = "target";

  protected Tuple _target;

  public AgentReconLocation() { super(); } // GSON only
  public AgentReconLocation( Agent agent, Tuple target )
  {
    super( agent );
    _target = target;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  // asdf
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetIsOffMap( game ) ) return false;
    if ( handleTargetOutOfRange( game ) ) return false;
    return true;
    // asdfasdf
  }

  protected String getMissionDescription()
  {
    return "the reconnaissance of " + _target;
  }

  private boolean handleTargetIsOffMap( Game game )
  {
    boolean isTargetOnMap = (_target.x < game.getMap().getWidth()) && (_target.x >= 0) &&
                            (_target.y < game.getMap().getHeight()) && (_target.y >= 0);
    if ( !isTargetOnMap )
    {
      addPlayerEvent( game, _subject, "Agent " + _subject.getName() + " can not recon the off map location " + _target );
      return true;
    }
    return false;
  }

  private boolean handleTargetOutOfRange( Game game )
  {
    if ( !isTargetInRange() )
    {
      addPlayerEvent( game, _subject, "Agent " + _subject.getName() + " can not recon the out of range location " + _target );
      return true;
    }
    return false;
  }

  protected boolean isTargetInRange()
  {
    int distance = HexCalculator.calculateDistance( _subject.getLocation().getCoord(), _target );
    return distance <= _subject.getRange();
  }

  protected Player getTargetPlayer( Game game )
  {
    return Player.UNOWNED;
  }

  protected void handleAgentPromotion()
  {
    // Recons have no risk, so no reward.
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

  public int getOrderCost()
  {
    // Recons are always cheap.
    return (Game.BASE_HAMLET_PRODUCTION * Game.GOLD_GRANULARITY) / 5;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " recon " + _target;
  }

  protected void updateViews( Game game )
  {
    reconHex( game, _subject, _target );
  }

  public static void reconHex( Game game, Figure subject, Tuple target )
  {
    Player executingPlayer = subject.getOwner();

    // Full knowledge of the location and any pop on the location.
    executingPlayer.addKnownLocation( game.getMap().getLocation( target ) );
    PopCenter rawPop = game.getPopCenter( target );
    if ( rawPop != null && !rawPop.getOwner().equals( executingPlayer ) )
    {
      PopCenter intelPop = new PopCenter( rawPop );
      executingPlayer.addKnownItem( intelPop );
    }

    // TODO - learn about any unusual stuff

    // Things owned by other players.
    for ( Player player : game.getPlayers() )
    {
      if ( player.equals( executingPlayer ) ) continue;

      // Seems like there should be a more elegant way to do this...
      for ( Agent agent : player.getAgents() )
      {
        if ( agent.getLocation().getCoord().equals( target ) && !agent.getBase().isInvisible() )
        {
          Agent intelAgent = new Agent( agent );
          executingPlayer.addKnownItem( intelAgent );
        }
      }
      for ( Diplomat diplomat : player.getDiplomats() )
      {
        if ( diplomat.getLocation().getCoord().equals( target ) && !diplomat.getBase().isInvisible() )
        {
          Diplomat intelDiplomat = new Diplomat( diplomat );
          executingPlayer.addKnownItem( intelDiplomat );
        }
      }
      for ( Wizard wizard : player.getWizards() )
      {
        if ( wizard.getLocation().getCoord().equals( target ) && !wizard.getBase().isInvisible() )
        {
          Wizard intelWizard = new Wizard( wizard );
          executingPlayer.addKnownItem( intelWizard );
        }
      }
      for ( Army army : player.getArmies() )
      {
        if ( army.getLocation().getCoord().equals( target ) && !army.isInvisible() )
        {
          Army intelArmy = new Army( army );
          executingPlayer.addKnownItem( intelArmy );
        }
      }
      if ( player.getKing().getLocation().getCoord().equals( target ) )
      {
        King intelKing = new King( player.getKing() );
        executingPlayer.addKnownItem( intelKing );
      }
    }
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
