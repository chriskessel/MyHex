package kessel.hex.orders.army;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Army;
import kessel.hex.domain.CombatCapableItem;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.orders.Mechanics;
import kessel.hex.orders.Order;
import kessel.hex.orders.diplomat.DiplomatNegotiateFealty;
import kessel.hex.orders.wizard.DiplomaticAura;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Have a group negotiate with a population center in an attempt to have it swear fealty. The group's chances are relative to it's size
 * versus that of the population center. A group 2x the size of the pop has roughly a 50/50 chance of success. So, a group of 2x the combat
 * value of a level 9 pop would have an effective diplomatic level of 6.
 * <p/>
 * Normal diplomacy factors apply, such as embassies, inspire loyalty, etc.
 */
public class DemandSurrender extends AbstractArmyOrder
{
  private static final Logger LOG = Logger.getLogger( DemandSurrender.class );

  protected transient PopCenter _target;
  protected transient boolean _targetIsMissing = false;
  protected transient boolean _wasSuccessful = false;

  public DemandSurrender() { super(); } // GSON only
  public DemandSurrender( Army army )
  {
    super( army );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleOwnsPop( game ) ) return false;
    if ( handleSubjectCantEngageInCombat( game ) ) return false;
    if ( handlePopTargetAndLostPreviousCombat( game ) ) return false;
    return true;
  }

  private boolean handleOwnsPop( Game game )
  {
    if ( _subject.getOwner().equals( _target.getOwner() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " skips diplomacy because " + _target.getName() +
                                      " is already part of our kingdom." );
      return true;
    }
    return false;
  }

  private boolean handleSubjectCantEngageInCombat( Game game )
  {
    if ( _subject.getBaseCombatStrength() <= 0 )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " is not a military force and thus has no diplomatic power." );
      return true;
    }
    return false;
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing )
    {
      addPlayerEvent( game, _subject,
        _subject.getName() + " had no population center in it's location to attempt military diplomacy." );
      return true;
    }
    return false;
  }

  private boolean handlePopTargetAndLostPreviousCombat( Game game )
  {
    for ( Order order : _subject.getOrdersExecuted() )
    {
      if ( (order instanceof AbstractArmyAttack) && !((AbstractArmyAttack) order).attackerWon() )
      {
        addPlayerEvent( game, _subject, _subject.getName() + " lost a combat earlier and is unable to attempt " +
                                        "military diplomacy with " + _target.getName() );
        return true;
      }
    }
    return false;
  }

  public void processOrder( Game game )
  {
    if ( !_target.isCapitol() )
    {
      int armyLevel = deriveArmyDiplomaticLevel( game );
      int diplomatHits = Mechanics.standardLevelRoll( armyLevel );
      _wasSuccessful = diplomatHits >= determinePopResistance( game );
    }
    else
    {
      // Capitols never surrender diplomatically.
      _wasSuccessful = false;
    }

    // Publish game events about the military negotiation attempt.
    if ( _wasSuccessful )
    {
      changePopOwnership( game );
    }
    else
    {
      publishFailedEvents( game );
    }
  }

  public int deriveArmyDiplomaticLevel( Game game )
  {
    double armyStrength = (double) _subject.getBaseCombatStrength() * getDiplomaticAuraMultiplier( game );
    double popStrength = (double) _target.getBaseCombatStrength();
    double relativeArmyStrength = armyStrength / (popStrength * 2.0);
    int armyLevel = (int) (relativeArmyStrength * _target.getLevel() * 0.67);
    if ( LOG.isDebugEnabled() ) { LOG.debug( "A=" + armyStrength + ",P=" + popStrength + " Effective level of " + armyLevel ); }
    return armyLevel;
  }

  private int getDiplomaticAuraMultiplier( Game game )
  {
    List<DiplomaticAura> auraOrders = game.getCurrentTurn().getOrdersOfType( DiplomaticAura.class );
    int auraMultiplier = 1;
    for ( DiplomaticAura auraOrder : auraOrders )
    {
      boolean affectsThisOrder = auraOrder.getSubject().getBase().equals( _subject );
      if ( affectsThisOrder )
      {
        auraMultiplier += auraOrder.getSubject().getLevel();
      }
    }
    return auraMultiplier;
  }

  /** @return the amount of resistance that must be met to succeed. */
  public int determinePopResistance( Game game )
  {
    // Use a fake diplomat just for the purpose of figuring out the correct pop resistance.
    Diplomat fakeDiplomat = new Diplomat( _subject.getOwner().getDiplomats().get( 0 ) );
    fakeDiplomat.setBase( _target );
    DiplomatNegotiateFealty fakeFealtyOrder = new DiplomatNegotiateFealty( fakeDiplomat );
    return fakeFealtyOrder.determineResistance( game );
  }

  private void changePopOwnership( Game game )
  {
    PopCenter pop = _target;
    publishPopLostEvent( game );
    _target.getOwner().remove( pop );
    _subject.getOwner().add( pop );
    publishPopGainedEvent( game );
  }

  private void publishPopGainedEvent( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " negotiated the allegiance of " + _target.getName() + "!" );
  }

  private void publishPopLostEvent( Game game )
  {
    String message = _target.getName() + " was threatened by the " + _subject.getName() + " and switched allegiances!";
    addPlayerEvent( game, _target, message );
  }

  private void publishFailedEvents( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " failed to negotiate the allegiance of " + _target.getName() + "!" );

    String message = _target.getName() + " rejected the diplomatic overtures of the " + _subject.getName() + "!";
    addPlayerEvent( game, _target, message );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " negotiate fealty with " + _target.getName();
  }

  public CombatCapableItem getTarget() { return _target; }

  public boolean wasSuccessful() { return _wasSuccessful; }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _target = game.getPopCenter( _subject.getLocation() );
    if ( _target == null ) { _targetIsMissing = true; }
  }
}
