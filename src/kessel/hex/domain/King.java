package kessel.hex.domain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import kessel.hex.orders.Order;
import kessel.hex.orders.PolicyOrder;
import kessel.hex.orders.army.CreateArmy;
import kessel.hex.orders.king.ImproveEmbassy;
import kessel.hex.orders.king.ImprovePower;
import kessel.hex.orders.king.MoveCapitol;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A special diplomat representing the king. */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class King extends Figure
{
  protected King() { super(); } // GSON requires this.

  public King( int id, String name, int turnSeen, GameItem base )
  {
    super( id, name, turnSeen, base );
    _range = 0;
  }

  public King( int id, String name, int turnSeen, GameItem base, Player owner )
  {
    super( id, name, turnSeen, base, owner );
    _range = 0;
  }

  public int getAssassinationDifficulty()
  {
    // TODO - allow killing once the ability to pass the crown is figured out.
    return 1000;
  }

  public int getLevelCap()
  {
    // Not relevant
    return 0;
  }

  public double getTrainingCostModifier()
  {
    // Not relevant
    return 0;
  }

  public void forceMove( PopCenter target )
  {
    setBase( target );
    MoveCapitol move = new MoveCapitol( this, target );
    addOrderExecuted( move );
  }

  public King( King toClone )
  {
    this( toClone.getId(), toClone.getName(), toClone.getTurnSeen(), toClone.getBase(), toClone.getOwner() );
  }

  /** @return true if the king has already issued a policy order this turn. */
  public boolean hasIssuedPolicyOrder()
  {
    for ( Order order : _ordersExecuted )
    {
      if ( order instanceof PolicyOrder )
      {
        return true;
      }
    }
    return false;
  }

  public List<Class> getAvailableOrders()
  {
    boolean policyOrderIssued = false;
    for ( Order<GameItem> nextTurnOrder : getNextTurnOrders() )
    {
      if ( nextTurnOrder instanceof PolicyOrder )
      {
        policyOrderIssued = true;
        break;
      }
    }
    List<Class> orders = new ArrayList<>();
    if ( !policyOrderIssued )
    {
      orders.addAll( Arrays.asList( ImprovePower.class, CreateArmy.class, ImproveEmbassy.class ) );
    }
    return orders;
  }

  public String getShortStatusName()
  {
    return "King" + " (" + getOwnerShortName() + ")";
  }

  public String getLongStatusName()
  {
    return "King " + _name + " (" + getOwnerName() + ")";
  }

  public String getDescription()
  {
    return "King " + _name + " in " +
           _base.getShortStatusName() + " at " + _base.getLocation().getCoord();
  }

  public static class MyJsonAdapter extends Figure.MyJsonAdapter<King>
  {
    public JsonElement serialize( King king, Type type, JsonSerializationContext context )
    {
      JsonObject jsonResponse = new JsonObject();
      super.serialize( king, jsonResponse, type, context );
      return jsonResponse;
    }

    public King deserialize( JsonElement jsonOrder, Type type, JsonDeserializationContext context )
      throws JsonParseException
    {
      King king = new King();
      super.doDeserialize( king, jsonOrder, context );
      return king;
    }
  }
}
