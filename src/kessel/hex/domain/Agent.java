package kessel.hex.domain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import kessel.hex.orders.AbstractMoveFigure;
import kessel.hex.orders.agent.AgentReconLocation;
import kessel.hex.orders.agent.AgentScoutControlLevel;
import kessel.hex.orders.agent.AgentScoutEmbassies;
import kessel.hex.orders.agent.AssassinateAgent;
import kessel.hex.orders.agent.AssassinateDiplomat;
import kessel.hex.orders.agent.AssassinateWizard;
import kessel.hex.orders.agent.CounterEspionage;
import kessel.hex.orders.agent.MoveAgent;
import kessel.hex.orders.agent.SabotageEmbassy;
import kessel.hex.orders.agent.SabotagePopCenter;
import kessel.hex.orders.agent.TrainAgent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** An agent. */
public class Agent extends Figure
{
  protected Agent() { super(); } // GSON requires this.
  public Agent( int id, String name, int turnSeen, GameItem base )
  {
    super( id, name, turnSeen, base );
  }

  public Agent( int id, String name, int turnSeen, GameItem base, Player owner )
  {
    super( id, name, turnSeen, base, owner );
  }

  public int getAssassinationDifficulty()
  {
    return (int) (getLevel() * 1.5);
  }

  public int getLevelCap()
  {
    return getOwner().getKingdom().getAgentLevelCap();
  }

  public double getTrainingCostModifier()
  {
    return getOwner().getKingdom().getAgentCostModifier();
  }

  public void forceMove( PopCenter target )
  {
    setBase( target );
    AbstractMoveFigure move = new MoveAgent( this, target );
    addOrderExecuted( move );
  }

  public Agent( Agent toClone )
  {
    this( toClone.getId(), toClone.getName(), toClone.getTurnSeen(), toClone.getBase(), toClone.getOwner() );
  }

  @SuppressWarnings({ "RawUseOfParameterizedType" })
  public List<Class> getAvailableOrders()
  {
    List<Class> orders = new ArrayList<>();
    if ( !hasOrders() )
    {
      orders.addAll( Arrays.asList(
        CounterEspionage.class, AgentReconLocation.class, AgentScoutControlLevel.class,
        AgentScoutEmbassies.class, SabotagePopCenter.class, MoveAgent.class, SabotageEmbassy.class,
        AssassinateAgent.class, AssassinateDiplomat.class, AssassinateWizard.class ) );
      if ( getLevel() < getLevelCap() )
      {
        orders.add( TrainAgent.class );
      }
    }
    return orders;
  }

  public String getShortStatusName()
  {
    return "A-" + getLevelString( _level ) + " (" + getOwnerShortName() + ")";
  }

  public String getLongStatusName()
  {
    return "Agent-" + getLevelString( _level ) + " " + _name + " (" + getOwnerName() + ")";
  }

  public String getDescription()
  {
    return "Agent-" + getLevelString( _level ) + " " + _name + " in " +
           _base.getShortStatusName() + " at " + _base.getLocation().getCoord();
  }

  public static class MyJsonAdapter extends Figure.MyJsonAdapter<Agent>
  {
    public JsonElement serialize( Agent agent, Type type, JsonSerializationContext context )
    {
      JsonObject jsonResponse = new JsonObject();
      super.serialize( agent, jsonResponse, type, context );
      return jsonResponse;
    }

    public Agent deserialize( JsonElement jsonOrder, Type type, JsonDeserializationContext context )
      throws JsonParseException
    {
      Agent agent = new Agent();
      super.doDeserialize( agent, jsonOrder, context );
      return agent;
    }
  }
}
