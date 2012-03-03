package kessel.hex.domain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import kessel.hex.orders.AbstractMoveFigure;
import kessel.hex.orders.wizard.MoveWizard;
import kessel.hex.orders.wizard.TrainWizard;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A wizard */
public class Wizard extends Figure
{
  protected Wizard() { super(); } // GSON requires this.

  public Wizard( int id, String name, int turnSeen, GameItem base )
  {
    super( id, name, turnSeen, base );
  }

  public Wizard( int id, String name, int turnSeen, GameItem base, Player owner )
  {
    super( id, name, turnSeen, base, owner );
  }

  public int getAssassinationDifficulty()
  {
    switch ( _level )
    {
      case 0:
        return 4;
      default:
        return 6 + (_level * 3);
    }
  }

  public int getLevelCap()
  {
    return getOwner().getKingdom().getWizardLevelCap();
  }

  public double getTrainingCostModifier()
  {
    return getOwner().getKingdom().getWizardCostModifier();
  }

  public void forceMove( PopCenter target )
  {
    setBase( target );
    AbstractMoveFigure move = new MoveWizard( this, target );
    addOrderExecuted( move );
  }

  public Wizard( Wizard toClone )
  {
    this( toClone.getId(), toClone.getName(), toClone.getTurnSeen(), toClone.getBase(), toClone.getOwner() );
  }

  @SuppressWarnings({ "RawUseOfParameterizedType" })
  public List<Class> getAvailableOrders()
  {
    List<Class> orders = new ArrayList<>();
    if ( !hasOrders() )
    {
      orders.addAll( Arrays.asList( MoveWizard.class ) );
      if ( getLevel() < getLevelCap() )
      {
        orders.add( TrainWizard.class );
      }
    }
    return orders;
  }

  public String getShortStatusName()
  {
    return "W-" + getLevelString( _level ) + " (" + getOwnerShortName() + ")";
  }

  public String getLongStatusName()
  {
    return "Wizard-" + getLevelString( _level ) + " " + _name + " (" + getOwnerName() + ")";
  }

  public String getDescription()
  {
    return "Wizard-" + getLevelString( _level ) + " " + _name + " in " +
           _base.getShortStatusName() + " at " + _base.getLocation().getCoord();
  }

  public static class MyJsonAdapter extends Figure.MyJsonAdapter<Wizard>
  {
    public JsonElement serialize( Wizard wizard, Type type, JsonSerializationContext context )
    {
      JsonObject jsonResponse = new JsonObject();
      super.serialize( wizard, jsonResponse, type, context );
      return jsonResponse;
    }

    public Wizard deserialize( JsonElement jsonOrder, Type type, JsonDeserializationContext context )
      throws JsonParseException
    {
      Wizard wizard = new Wizard();
      super.doDeserialize( wizard, jsonOrder, context );
      return wizard;
    }
  }
}
