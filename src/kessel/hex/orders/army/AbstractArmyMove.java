package kessel.hex.orders.army;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Terrain;
import kessel.hex.orders.Order;
import kessel.hex.util.HexCalculator;
import kessel.hex.util.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Abstract move an army. */
public abstract class AbstractArmyMove extends AbstractArmyOrder
{
  // Use for json persistence.
  protected static final String MOVEMENT_STEPS = "movementSteps";

  public static final int MAX_MOVEMENT = 50;

  List<Tuple> _movementSteps = new ArrayList<>();

  protected AbstractArmyMove() { super(); } // for GSON only

  protected AbstractArmyMove( Army army, List<Tuple> movementSteps )
  {
    super( army );
    _movementSteps.addAll( movementSteps );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleAlreadyMovedThisTurn( game ) ) return false;
    return true;
  }

  @SuppressWarnings({ "RawUseOfParameterizedType" })
  protected boolean handleAlreadyMovedThisTurn( Game game )
  {
    for ( Order order : _subject.getOrdersExecuted() )
    {
      if ( order instanceof AbstractArmyMove )
      {
        addPlayerEvent( game, _subject, _subject.getName() + " has already moved this turn." );
        return true;
      }
    }
    return false;
  }

  public void processOrder( Game game )
  {
    Tuple lastStep = _subject.getLocation().getCoord();
    int moveTotal = 0;
    List<Tuple> potentialSteps = new ArrayList<>( _movementSteps );
    _movementSteps.clear();
    for ( Tuple nextStep : potentialSteps )
    {
      boolean nextStepIsAdjacent = HexCalculator.calculateDistance( lastStep, nextStep ) == 1;
      boolean nextStepIsOnMap = (nextStep.x < game.getMap().getWidth()) && (nextStep.x >= 0) &&
                                (nextStep.y < game.getMap().getHeight()) && (nextStep.y >= 0);
      boolean stepSuccessful = false;
      if ( nextStepIsAdjacent && nextStepIsOnMap )
      {
        int moveStepCost = getLocationMovementCost( game, nextStep );
        boolean hasMovementForNextStep = moveTotal + moveStepCost <= MAX_MOVEMENT;
        if ( hasMovementForNextStep )
        {
          stepSuccessful = true;
          _movementSteps.add( nextStep );
          lastStep = nextStep;
          moveTotal += moveStepCost;
        }
      }

      // If it was an illegal step, complain, but keep trying as other steps may be legal.
      if ( !stepSuccessful )
      {
        reportIllegalStep( game, lastStep, nextStep );
      }
      if ( shouldStop( lastStep, game ) )
      {
        break;
      }
    }
    _subject.setLocation( game.getMap().getLocation( lastStep ) );
  }

  /** @return true if the army should stop moving at the given hex. */
  protected abstract boolean shouldStop( Tuple hex, Game game );

  protected void reportIllegalStep( Game game, Tuple lastStep, Tuple nextStep )
  {
    _movementSteps.remove( nextStep );
    String message = "Army " + _subject.getName() + " ignored illegal move step " + nextStep;
    addPlayerEvent( game, _subject, message, game.getMap().getLocation( lastStep ) );
  }


  public int getLocationMovementCost( Game game, Tuple coord )
  {
    Terrain terrain = game.getMap().getLocation( coord ).getTerrain();
    return _subject.getOwner().getKingdom().getMovementCost( terrain );
  }

  protected void updateViews( Game game )
  {
    // Know about each location covered and if there was a pop center on it.
    Player player = _subject.getOwner();
    for ( Tuple step : _movementSteps )
    {
      player.addKnownLocation( game.getMap().getLocation( step ) );
      PopCenter pop = game.getPopCenter( step );
      if ( (pop != null) && !pop.getOwner().equals( player ) )
      {
        PopCenter intelPop = new PopCenter( pop );
        intelPop.setTurnSeen( game.getTurn() );
        intelPop.setLevel( PopCenter.VALUE_UNKNOWN );
        intelPop.setOwner( Player.UNKNOWN );
        player.addKnownItem( intelPop );
      }
    }
  }

  public String getShortDescription()
  {
    StringBuilder s = new StringBuilder( _subject.getName() + " move " );
    for ( Tuple next : _movementSteps )
    {
      s.append( next );
    }
    return s.toString();
  }

  public List<Tuple> getMovementSteps() { return _movementSteps; }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( MOVEMENT_STEPS, _movementSteps );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _movementSteps = context.deserialize( jsonOrder.getAsJsonObject().get( MOVEMENT_STEPS ), new TypeToken<List<Tuple>>(){}.getType() );
  }
}
