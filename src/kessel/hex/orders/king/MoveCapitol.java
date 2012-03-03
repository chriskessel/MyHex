package kessel.hex.orders.king;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Agent;
import kessel.hex.domain.Army;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Figure;
import kessel.hex.domain.Game;
import kessel.hex.domain.King;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractMoveFigure;
import kessel.hex.orders.Order;
import kessel.hex.orders.PolicyOrder;
import kessel.hex.orders.agent.MoveAgent;
import kessel.hex.orders.diplomat.MoveDiplomat;
import kessel.hex.orders.wizard.MoveWizard;

import java.util.Map;

/** Change the player's capitol population center. */
public class MoveCapitol extends Order<King> implements PolicyOrder
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "_targetId";

  protected PopCenter _target;
  protected transient Integer _jsonTargetId;
  protected transient boolean _targetIsMissing = false;

  public MoveCapitol() {} // GSON only

  public MoveCapitol( King king, PopCenter pop )
  {
    super( king );
    _target = pop;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleIsCharmed( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleKingWasAlreadyBusy( game ) ) return false;
    if ( handleNotEnoughMoney( game ) ) return false;
    if ( handleNotMyPop( game ) ) return false;
    return true;
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing )
    {
      addPlayerEvent( game, _subject, "Unable to move the capitol as the target does not exist." );
      return true;
    }
    return false;
  }

  private boolean handleKingWasAlreadyBusy( Game game )
  {
    if ( _subject.hasIssuedPolicyOrder() )
    {
      addPlayerEvent( game, _subject, "Unable to move the capitol since " + _subject.getName() +
                                      " has already issued a policy order this turn." );
      return true;
    }
    return false;
  }

  private boolean handleNotEnoughMoney( Game game )
  {
    if ( _subject.getOwner().getGold() < getOrderCost() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " lacked sufficient funds to move the capitol." );
      return true;
    }
    return false;
  }

  private boolean handleNotMyPop( Game game )
  {
    if ( !_target.getOwner().equals( _subject.getOwner() ) )
    {
      String message = _subject.getName() + " does not own " + _target.getName() + " and thus cannot move the capitol there.";
      addPlayerEvent( game, _subject, message, _target.getLocation() );
      return true;
    }
    return false;
  }

  @SuppressWarnings({ "ChainOfInstanceofChecks" })
  public void processOrder( Game game )
  {
    if ( _subject.getOwner().relocateCapitol( _target ) )
    {

    }

    PopCenter oldCapitol = _subject.getOwner().getCapitol();
    _subject.getOwner().setCapitol( _target );

    // Any figures at the capitol, that haven't already done something, are moved as well and it counts as the
    // figure's order for the turn.
    for ( Figure figure : _subject.getOwner().getFigures() )
    {
      if ( figure.getBase().equals( oldCapitol ) && figure.getOrdersExecuted().isEmpty() )
      {
        figure.setBase( _target );
        if ( figure instanceof Agent )
        {
          AbstractMoveFigure move = new MoveAgent( (Agent) figure, _target );
          figure.addOrderExecuted( move );
        }
        else if ( figure instanceof Diplomat )
        {
          AbstractMoveFigure move = new MoveDiplomat( (Diplomat) figure, _target );
          figure.addOrderExecuted( move );
        }
        else if ( figure instanceof Wizard )
        {
          AbstractMoveFigure move = new MoveWizard( (Wizard) figure, _target );
          figure.addOrderExecuted( move );
        }
        else if ( figure instanceof King )
        {
          // Handled by order.execute().
        }
        else
        {
          throw new RuntimeException( "Trying to relocate a non-figure with the capitol." );
        }
      }
    }

    // Any inactive armies are moved.
    for ( Army army : _subject.getOwner().getArmies() )
    {
      if ( !army.isActive() )
      {
        army.setLocation( _target.getLocation() );
        // NOTE: this does NOT count as a movement order for the army.
      }
    }

    addPlayerEvent( game, _subject, _subject.getName() + " relocated the capitol." );
  }

  public int getOrderCost()
  {
    return Game.GOLD_GRANULARITY * Game.BASE_HAMLET_PRODUCTION * 7;
  }

  public String getShortDescription()
  {
    return "Move capitol to " + _target.getName() + _target.getLocation().getCoord();
  }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( TARGET_ID_JSON, _target.getId() );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonTargetId = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_ID_JSON ), Integer.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _target = game.getPopCenter( _jsonTargetId );
    if ( _target == null ) { _targetIsMissing = true; }
  }
}
