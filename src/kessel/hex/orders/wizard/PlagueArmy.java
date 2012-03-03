package kessel.hex.orders.wizard;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Figure;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;

import java.util.Map;
import java.util.Random;

/** Cause a plague to run through the army, killing off multiple units worth of men. */
public class PlagueArmy extends AbstractSpell
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "targetId";

  /** How often figures in a destroyed army is killed by the plague. */
  static int FIGURE_PLAGUE_CHANCE = 50;

  protected final transient Random _r = new Random();
  protected Army _target;
  protected transient Integer _jsonTargetId;
  protected transient boolean _targetIsMissing = false;

  public PlagueArmy() { super(); } // GSON only
  public PlagueArmy( Wizard wizard, Army target )
  {
    super( wizard );
    _target = target;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleTargetNotInSameLocation( game ) ) return false;
    if ( handleTargetTooSmall( game ) ) return false;
    return true;
  }

  protected boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing )
    {
      addPlayerEvent( game, _subject, "The target does not exist." );
      return true;
    }
    return false;
  }

  protected boolean handleTargetNotInSameLocation( Game game )
  {
    if ( !_subject.getLocation().equals( _target.getLocation() ) )
    {
      String message = _subject.getName() + " did not cast Plague Army since " + _target.getName() + " was not in our location.";
      addPlayerEvent( game, _subject, message );
      return true;
    }
    return false;
  }

  protected boolean handleTargetTooSmall( Game game )
  {
    if ( !_target.isActive() )
    {
      String message = _subject.getName() + " did not cast Plague Army since " + _target.getName() + " is not an active army.";
      addPlayerEvent( game, _subject, message );
      return true;
    }
    return false;
  }


  public void processOrder( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " cast plague army against " + _target.getName() );
    causePlagueAttrition();

    // If the army was wiped out by the plague, any figures in it may die as well.
    if ( _target.getUnits().isEmpty() )
    {
      checkForPlaguedFigures( game );
      _target.setLocation( _target.getOwner().getCapitol().getLocation() );
    }

  }

  /** Check for figures dying of the plague. */
  private void checkForPlaguedFigures( Game game )
  {
    Player player = _target.getOwner();
    for ( Figure figure : player.getFigures() )
    {
      if ( figure.getBase().equals( _target ) )
      {
        boolean hasNoCapitol = figure.getOwner().getCapitol().equals( PopCenter.THE_WILDS );
        if ( (_r.nextInt( 100 ) < FIGURE_PLAGUE_CHANCE) || hasNoCapitol )
        {
          player.remove( figure );
          _subject.getOwner().removeKnownItem( figure );
          publishDeathEvent( game, figure );
        }
        else
        {
          figure.forceMove( figure.getOwner().getCapitol() );
          publishEscapeEvent( game, figure );
        }
      }
    }
  }

  private void publishDeathEvent( Game game, Figure figure )
  {
    String targetMessage = figure.getName() + " died in the plague that destroyed the " + _target.getName();
    addPlayerEvent( game, figure, targetMessage, _target.getLocation() );
  }

  private void publishEscapeEvent( Game game, Figure figure )
  {
    String message = figure.getName() + " escaped the plague that destroyed the " + _target.getName();
    addPlayerEvent( game, figure, message, _target.getLocation() );
  }

  /** Cause enough attrition to kill roughly 3 normal sized ArmyUnits, where normal depends on the army suffering the plague. */
  private void causePlagueAttrition()
  {
    int maxTroops = 0;
    for ( ArmyUnit armyUnit : _target.getUnits() )
    {
      maxTroops = armyUnit.getTroopType().getSize();
    }
    int averageUnitSize = maxTroops / _target.getUnits().size();
    int troopsToKill = averageUnitSize * 4;
    double percentToKill = (double) troopsToKill / (double) maxTroops;
    for ( ArmyUnit armyUnit : _target.getUnits() )
    {
      armyUnit.increaseCasualties( (int) (percentToKill * armyUnit.getTroopType().getSize()) );
    }

    // Give the army a poke at the end to force unit consolidation.
    _target.takeDamage( 0 );
  }

  public GameItem getTarget() { return _target; }


  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( TARGET_ID_JSON, _target.getId() );
    return map;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " cast plague army against " + _target.getName();
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonTargetId = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_ID_JSON ), Integer.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _target = (Army) game.getItem( _jsonTargetId );
    if ( _target == null ) { _targetIsMissing = true; }
  }
}
