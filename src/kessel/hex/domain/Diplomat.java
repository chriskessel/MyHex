package kessel.hex.domain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import kessel.hex.orders.AbstractMoveFigure;
import kessel.hex.orders.diplomat.DiplomatInciteRebellion;
import kessel.hex.orders.diplomat.DiplomatInspireLoyalty;
import kessel.hex.orders.diplomat.DiplomatNegotiateFealty;
import kessel.hex.orders.diplomat.MapRegion;
import kessel.hex.orders.diplomat.MoveDiplomat;
import kessel.hex.orders.diplomat.TakeRegionCensus;
import kessel.hex.orders.diplomat.TrainDiplomat;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A political Diplomat. */
public class Diplomat extends Figure
{
  protected Diplomat() { super(); } // GSON requires this.

  public Diplomat( int id, String name, int turnSeen, GameItem base )
  {
    super( id, name, turnSeen, base );
  }

  public Diplomat( int id, String name, int turnSeen, GameItem base, Player owner )
  {
    super( id, name, turnSeen, base, owner );
  }

  public int getAssassinationDifficulty()
  {
    return (int) (getLevel() * 1.5);
  }

  public int getLevelCap()
  {
    return getOwner().getPower();
  }

  public double getTrainingCostModifier()
  {
    return getOwner().getKingdom().getDiplomatCostModifier();
  }

  public void forceMove( PopCenter target )
  {
    setBase( target );
    AbstractMoveFigure move = new MoveDiplomat( this, target );
    addOrderExecuted( move );
  }

  public Diplomat( Diplomat toClone )
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
        DiplomatInciteRebellion.class, DiplomatInspireLoyalty.class, DiplomatNegotiateFealty.class,
        TakeRegionCensus.class, MoveDiplomat.class, MapRegion.class ) );
      if ( getLevel() < getLevelCap() )
      {
        orders.add( TrainDiplomat.class );
      }
    }
    return orders;
  }

  public String getShortStatusName()
  {
    return "D-" + getLevelString( _level ) + " (" + getOwnerShortName() + ")";
  }

  public String getLongStatusName()
  {
    return "Diplomat-" + getLevelString( _level ) + " " + _name + " (" + getOwnerName() + ")";
  }

  public String getDescription()
  {
    return "Diplomat-" + getLevelString( _level ) + " " + _name + " in " +
           _base.getShortStatusName() + " at " + _base.getLocation().getCoord();
  }

  public static class MyJsonAdapter extends Figure.MyJsonAdapter<Diplomat>
  {
    public JsonElement serialize( Diplomat diplomat, Type type, JsonSerializationContext context )
    {
      JsonObject jsonResponse = new JsonObject();
      super.serialize( diplomat, jsonResponse, type, context );
      return jsonResponse;
    }

    public Diplomat deserialize( JsonElement jsonOrder, Type type, JsonDeserializationContext context )
      throws JsonParseException
    {
      Diplomat diplomat = new Diplomat();
      super.doDeserialize( diplomat, jsonOrder, context );
      return diplomat;
    }
  }
}
