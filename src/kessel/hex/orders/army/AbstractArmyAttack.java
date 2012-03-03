package kessel.hex.orders.army;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Agent;
import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.CombatCapableItem;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Figure;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.TroopType;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.Order;
import kessel.hex.orders.wizard.CombatSpell;
import kessel.hex.orders.wizard.DeepFog;
import kessel.hex.orders.wizard.Fireball;
import kessel.hex.orders.wizard.MagicDome;
import kessel.hex.orders.wizard.PhantomTroops;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Have one army attack another army or pop center. */
public abstract class AbstractArmyAttack extends AbstractArmyOrder
{
  private static final Logger LOG = Logger.getLogger( AbstractArmyAttack.class );

  private static final int WIZARD_INNATE_STRENGTH = new ArmyUnit( TroopType.WIZARD_INNATE ).getBaseCombatStrength();

  /** How often the pop loses a level when conquered. */
  static int DEGRADE_CHANCE = 50;

  /** How often figures in the pop are captures when the pop is conquered. */
  static int CAPTURE_CHANCE = 50;

  protected final transient Random _r = new Random();

  // Use for json persistence.
  public static final String TARGET_ID_JSON = "targetId";

  protected CombatCapableItem _target;
  protected transient Integer _jsonTargetId;
  protected Player _targetOwner;
  private transient boolean _targetIsMissing = false;

  protected transient boolean _attackerWon = false;

  protected AbstractArmyAttack() { super(); }
  protected AbstractArmyAttack( Army army, CombatCapableItem target )
  {
    super( army );
    _target = target;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleSubjectCantAttack( game ) ) return false;
    if ( handleSubjectCantEngageInCombat( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleTargetNotInSameLocation( game ) ) return false;
    if ( handleTargetCantEngageInCombat( game ) ) return false;
    if ( handleTargetProtected( game ) ) return false;
    if ( handleAttackedSameTargetTwice( game ) ) return false;
    if ( handleTargetInitiatedAttack( game ) ) return false;
    if ( handlePopTargetAndLostPreviousCombat( game ) ) return false;
    if ( handlePopProtected( game ) ) return false;
    return true;
  }

  private boolean handleSubjectCantAttack( Game game )
  {
    // If the subject cast Deep Fog, they're not allowed to attack.
    List<DeepFog> orders = game.getCurrentTurn().getOrdersOfType( DeepFog.class );
    for ( DeepFog deepFogOrder : orders )
    {
      if ( deepFogOrder.getSubject().getBase().equals( _subject ) )
      {
        addPlayerEvent( game, _subject,
          _subject.getName() + " had a wizard cast Deep Fog and is not allowed to initiate an attack." );
        return true;
      }
    }
    return false;
  }

  private boolean handleSubjectCantEngageInCombat( Game game )
  {
    if ( _subject.getBaseCombatStrength() <= 0 )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " is not a military force and thus can't initiate an attack." );
      return true;
    }
    return false;
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing )
    {
      addPlayerEvent( game, _subject, _target.getName() + " is not available to attack." );
      return true;
    }
    return false;
  }

  private boolean handleTargetNotInSameLocation( Game game )
  {
    if ( !_target.getLocation().equals( _subject.getLocation() ) )
    {
      addPlayerEvent( game, _subject, _target.getName() + " is not at the same location as the attacker." );
      return true;
    }
    return false;
  }

  private boolean handleTargetCantEngageInCombat( Game game )
  {
    if ( _target.getBaseCombatStrength() == 0 )
    {
      addPlayerEvent( game, _subject, _target.getName() + " is not a military force." );
      return true;
    }
    return false;
  }

  private boolean handleTargetProtected( Game game )
  {
    // If the target cast Deep Fog against the attacker then the defender is safe unless the attacker dispels it.
    List<DeepFog> deepFogOrders = game.getCurrentTurn().getOrdersOfType( DeepFog.class );
    for ( DeepFog deepFogOrder : deepFogOrders )
    {
      if ( deepFogOrder.getTarget().equals( _subject ) )
      {
        if ( !CombatSpell.dispelSucceeds( game, _subject, _target, deepFogOrder ) )
        {
          // None of the dispels worked, so the fog stops the attack.
          addPlayerEvent( game, _subject, _subject.getName() + " was unable engage the " + _target.getName() +
                                          " due to a deep fog obscuring the battle field." );
          return true;
        }
        else
        {
          addPlayerEvent( game, _subject, _subject.getName() + " dispelled the Deep Fog cast by the enemy!" );
          Wizard deepFogCaster = deepFogOrder.getSubject();
          addPlayerEvent( game, deepFogCaster, deepFogCaster.getName() + " had the Deep Fog dispelled by the enemy!" );
        }
      }
    }
    return false;
  }

  private boolean handleAttackedSameTargetTwice( Game game )
  {
    AbstractArmyAttack previousCombat = getPreviousCombat( _subject, _target );
    if ( previousCombat != null )
    {
      // If it's because we tried to attack twice, complain. Otherwise they attacked us and that's normal.
      if ( previousCombat.wasExecuted() )
      {
        addPlayerEvent( game, _subject,
          _subject.getName() + " already engaged " + _target.getName() + " in combat earlier this turn." );
      }
      return true;
    }
    return false;
  }

  @SuppressWarnings({ "UnusedParameters" })
  private boolean handleTargetInitiatedAttack( Game game )
  {
    if ( _target instanceof Army )
    {
      AbstractArmyAttack targetInitiatedCombat = getPreviousCombat( (Army) _target, _subject );
      if ( targetInitiatedCombat != null )
      {
        // This special case just skips this order since the desired battle already took place. We do consider the order
        // "executed" and track who won/lost in case that impacts other combats by the _subject.
        _subject.addOrderExecuted( this );
        _attackerWon = !targetInitiatedCombat._attackerWon;
        return true;
      }
    }
    return false;
  }

  private boolean handlePopTargetAndLostPreviousCombat( Game game )
  {
    if ( _target instanceof PopCenter )
    {
      for ( Order order : _subject.getOrdersExecuted() )
      {
        if ( (order instanceof AbstractArmyAttack) && !((AbstractArmyAttack) order).attackerWon() )
        {
          addPlayerEvent( game, _subject,
            _subject.getName() + " lost a combat earlier and is unable to attack " + _target.getName() );
          return true;
        }
      }
    }
    return false;
  }

  private boolean handlePopProtected( Game game )
  {
    for ( MagicDome magicDome : game.getCurrentTurn().getOrdersOfType( MagicDome.class ) )
    {
      if ( magicDome.getTarget().equals( _target ) )
      {
        addPlayerEvent( game, _subject,
          _subject.getName() + " was prevented from attacking by a magic dome around " + _target.getName() );
        return true;
      }
    }
    return false;
  }

  /** @return any previously executed attack this turn by the subject on the target or null if none exists. */
  private AbstractArmyAttack getPreviousCombat( Army subject, CombatCapableItem target )
  {
    for ( Order order : subject.getOrdersExecuted() )
    {
      if ( order instanceof AbstractArmyAttack )
      {
        AbstractArmyAttack attackOrder = (AbstractArmyAttack) order;
        if ( attackOrder.getTarget().equals( target ) )
        {
          return attackOrder;
        }
      }
    }
    return null;
  }

  public void processOrder( Game game )
  {
    // Need to remember this before combat starts, since pops can change owners as a result of combat.
    _targetOwner = _target.getOwner();

    int attackerStrength = getCombatStrength( game, _subject, _target );
    int defenderStrength = getCombatStrength( game, _target, _subject );
    if ( LOG.isDebugEnabled() )
    { LOG.debug( _subject.getName() + "=" + attackerStrength + " " + _target.getName() + "=" + defenderStrength ); }

    inflictDamage( game, _target, _subject, defenderStrength );
    inflictDamage( game, _subject, _target, attackerStrength );

    _attackerWon = attackerStrength > defenderStrength;
    _subject.adjustMorale( _attackerWon ? 10 : -10 );
    if ( _target instanceof Army )
    {
      ((Army) _target).adjustMorale( _attackerWon ? -10 : 10 );
    }

    if ( _target instanceof PopCenter )
    {
      PopCenter pop = (PopCenter) _target;
      if ( _attackerWon )
      {
        changePopOwnership( game );
        checkForPopDegradation();
        checkForCapturedFigures( game, _subject.getOwner(), _target );
        checkForCapitolTaken();
      }
      else
      {
        String subjectMessage = _subject.getName() + " was repelled by the " + pop.getType().name() + " of " + _target.getName();
        addPlayerEvent( game, _subject, subjectMessage );

        String targetMessage = _target.getName() + " repelled the attack by " + _subject.getName();
        addPlayerEvent( game, _target, targetMessage, _subject.getLocation() );
      }
    }
    else
    {
      String subjectMessage = _subject.getName() + (_attackerWon ? " defeated the " : " was defeated by the ") + _target.getName();
      addPlayerEvent( game, _subject, subjectMessage );
      String targetMessage = _target.getName() + (_attackerWon ? " was defeated by the " : " defeated the ") + _subject.getName();
      addPlayerEvent( game, _target, targetMessage );
    }
    checkForDestroyedArmies( game );
  }

  /** @return the attackers combat strength vs. the defender, accounting for special modifiers like spells. */
  private int getCombatStrength( Game game, CombatCapableItem attacker, CombatCapableItem defender )
  {
    int fireballStrength = getFireballStrength( game, attacker, defender );
    int innateStrength = getInnateStrength( game, attacker, defender );
    return attacker.getCombatStrength() + innateStrength + fireballStrength;
  }

  private int getInnateStrength( Game game, CombatCapableItem attacker, CombatCapableItem defender )
  {
    int innateStrength = 0;
    for ( Wizard wizard : attacker.getOwner().getWizards() )
    {
      if ( wizard.getBase().equals( attacker ) )
      {
        innateStrength += wizard.getTotalLevels() * WIZARD_INNATE_STRENGTH;
      }
    }
    return innateStrength;
  }

  private int getFireballStrength( Game game, CombatCapableItem attacker, CombatCapableItem defender )
  {
    int fireballStrength = 0;
    List<Fireball> orders = game.getCurrentTurn().getOrdersOfType( Fireball.class );
    for ( Fireball fireballOrder : orders )
    {
      GameItem armyCastingFireball = fireballOrder.getSubject().getBase();
      GameItem armyGettingToasted = fireballOrder.getTarget();
      if ( armyCastingFireball.equals( attacker ) && armyGettingToasted.equals( defender ) )
      {
        // Found a relevant Fireball order, check if it's negated by a Dispel Combat Magic.
        if ( CombatSpell.dispelSucceeds( game, defender, attacker, fireballOrder ) )
        {
          addPlayerEvent( game, defender, defender.getName() + "'s wizards dispelled the Fireball cast by the enemy!" );
          GameItem fireballCaster = fireballOrder.getSubject();
          addPlayerEvent( game, fireballCaster, fireballCaster.getName() + " had the Fireball dispelled by the enemy!" );
        }
        else
        {
          fireballStrength += fireballOrder.getValue();
        }
      }
    }
    return fireballStrength;
  }

  /** Inflict damage from the attacker on the defender, accounting for any special modifiers like spells. */
  private void inflictDamage( Game game, CombatCapableItem attacker, CombatCapableItem defender, int damageToDefender )
  {
    int damageReduction = getPhantomTroopImpact( game, attacker, defender );
    int trueDamage = (int) (damageToDefender * ((100.0 - (double) damageReduction) / 100.0));
    trueDamage = Math.max( trueDamage, 0 );
    defender.takeDamage( trueDamage );
  }

  /** @return the percentage of the attacker's damage negated by the defender's phantom troops. */
  private int getPhantomTroopImpact( Game game, CombatCapableItem attacker, CombatCapableItem defender )
  {
    int damageReduction = 0;
    List<PhantomTroops> orders = game.getCurrentTurn().getOrdersOfType( PhantomTroops.class );
    for ( PhantomTroops phantomTroopsOrder : orders )
    {
      GameItem armyWithPhantoms = phantomTroopsOrder.getSubject().getBase();
      GameItem armySeeingPhantoms = phantomTroopsOrder.getTarget();
      if ( armyWithPhantoms.equals( defender ) && armySeeingPhantoms.equals( attacker ) )
      {
        // Found a relevant Phantom Troop order, check if it's negated by a Dispel Combat Magic.
        if ( CombatSpell.dispelSucceeds( game, attacker, defender, phantomTroopsOrder ) )
        {
          addPlayerEvent( game, attacker, attacker.getName() + "'s wizards dispelled the Phantom Troops cast by the enemy!" );
          GameItem phantomsCaster = phantomTroopsOrder.getSubject();
          addPlayerEvent( game, phantomsCaster, phantomsCaster.getName() + " had the Phantom Troops dispelled by the enemy!" );
        }
        else
        {
          damageReduction += phantomTroopsOrder.getValue();
        }
      }
    }
    return damageReduction;
  }

  @SuppressWarnings({ "ClassReferencesSubclass" })
  protected void trackOrderExecuted()
  {
    // Note that the defender executed an attack as well. Necessary so other defender actions know the combat result.
    _subject.addOrderExecuted( this );
    if ( _target instanceof Army )
    {
      ArmyAttackArmy defendersRole = new ArmyAttackArmy( (Army) _target, _subject );
      defendersRole._attackerWon = !_attackerWon;
      _target.addOrderExecuted( defendersRole );
    }
  }

  private void changePopOwnership( Game game )
  {
    PopCenter pop = (PopCenter) _target;
    _targetOwner.remove( pop );
    _subject.getOwner().add( pop );
    publishPopGainedEvent( game, pop );
    publishPopLostEvent( game, pop );
  }

  /** PopCenters can have their level degrade as a result of combat damage. */
  protected void checkForPopDegradation()
  {
    PopCenter pop = (PopCenter) _target;
    if ( _r.nextInt( 100 ) < DEGRADE_CHANCE )
    {
      pop.degradeLevel();
    }
  }

  private void checkForCapitolTaken()
  {
    if ( !_targetOwner.equals( Player.UNOWNED ) && _targetOwner.getCapitol().equals( _target ) )
    {
      _targetOwner.forcedCapitolRelocation();
    }
  }

  /** If an army is destroyed then any figures using it as a base could be captured. */
  private void checkForDestroyedArmies( Game game )
  {
    // If every unit was wiped out, then the army was destroyed, figures may have been captured, and the army is sent
    // back to it's owner's capitol (it's inactive).
    if ( _subject.getBaseCombatStrength() == 0 )
    {
      checkForCapturedFigures( game, _target.getOwner(), _subject );
      _subject.setLocation( _subject.getOwner().getCapitol().getLocation() );
    }

    if ( (_target instanceof Army) && _target.getBaseCombatStrength() == 0 )
    {
      checkForCapturedFigures( game, _subject.getOwner(), _target );
      _target.setLocation( _target.getOwner().getCapitol().getLocation() );
    }
  }

  /** Check for catching non-friendly figures in the target. */
  private void checkForCapturedFigures( Game game, Player winningPlayer, GameItem conqueredItem )
  {
    for ( Player player : game.getPlayers() )
    {
      if ( player.equals( winningPlayer ) ) continue; // Don't capture your own guys. Future - allies are safe?

      for ( Agent agent : new ArrayList<>( player.getAgents() ) )
      {
        boolean isBasedInTarget = agent.getBase().equals( conqueredItem );
        if ( isBasedInTarget )
        {
          boolean hasNoCapitol = agent.getOwner().getCapitol().equals( PopCenter.THE_WILDS );
          if ( (_r.nextInt( 100 ) < CAPTURE_CHANCE) || hasNoCapitol )
          {
            agent.getOwner().remove( agent );
            winningPlayer.removeKnownItem( agent );
            publishCaptureEvent( game, agent );
          }
          else
          {
            agent.forceMove( agent.getOwner().getCapitol() );
            publishEscapeEvent( game, agent );
          }
        }
      }
      for ( Diplomat diplomat : new ArrayList<>( player.getDiplomats() ) )
      {
        boolean isBasedInTarget = diplomat.getBase().equals( conqueredItem );
        if ( isBasedInTarget )
        {
          boolean hasNoCapitol = diplomat.getOwner().getCapitol().equals( PopCenter.THE_WILDS );
          if ( (_r.nextInt( 100 ) < CAPTURE_CHANCE) || hasNoCapitol )
          {
            diplomat.getOwner().remove( diplomat );
            winningPlayer.removeKnownItem( diplomat );
            publishCaptureEvent( game, diplomat );
          }
          else
          {
            diplomat.forceMove( diplomat.getOwner().getCapitol() );
            publishEscapeEvent( game, diplomat );
          }
        }
      }
      for ( Wizard wizard : new ArrayList<>( player.getWizards() ) )
      {
        // Wizards are always safe in towns, but not armies.
        boolean isBasedInTarget = wizard.getBase().equals( conqueredItem );
        boolean wasConqueredArmy = conqueredItem instanceof Army;
        if ( isBasedInTarget && wasConqueredArmy )
        {
          boolean hasNoCapitol = wizard.getOwner().getCapitol().equals( PopCenter.THE_WILDS );
          if ( (_r.nextInt( 100 ) < CAPTURE_CHANCE) || hasNoCapitol )
          {
            wizard.getOwner().remove( wizard );
            winningPlayer.removeKnownItem( wizard );
            publishCaptureEvent( game, wizard );
          }
          else
          {
            wizard.forceMove( wizard.getOwner().getCapitol() );
            publishEscapeEvent( game, wizard );
          }
        }
      }
    }
  }

  private void publishCaptureEvent( Game game, Figure figure )
  {
    String message = figure.getName() + " of " + figure.getOwnerName() +
                     " was caught and executed in defeat of " + (_target instanceof Army ? "the " : "") + _target.getName();
    addPlayerEvent( game, _subject, message, _target.getLocation() );
    addPlayerEvent( game, figure, message, _target.getLocation() );
  }

  private void publishEscapeEvent( Game game, Figure figure )
  {
    String message = figure.getName() + " escaped in the defeat of " + (_target instanceof Army ? "the " : "") + _target.getName();
    addPlayerEvent( game, figure, message, _target.getLocation() );
  }

  private void publishPopGainedEvent( Game game, PopCenter pop )
  {
    String message = _subject.getName() + " conquered " + _target.getName();
    addPlayerEvent( game, _subject, message, pop.getLocation() );
  }

  private void publishPopLostEvent( Game game, PopCenter pop )
  {
    String message = pop.getName() + " was conquered by the " + _subject.getName() + " of the " + _subject.getOwnerName();
    addPlayerEvent( game, _target, message, pop.getLocation() );
  }

  protected void updateViews( Game game )
  {
    // The subject and target have full knowledge of each other after the battle.
    if ( _target instanceof PopCenter )
    {
      _subject.getOwner().addKnownItem( new PopCenter( (PopCenter) _target ) );
    }
    else
    {
      _subject.getOwner().addKnownItem( new Army( (Army) _target ) );
    }
    _targetOwner.addKnownItem( new Army( _subject ) );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " attack " + _target.getName();
  }

  public boolean attackerWon() { return _attackerWon; }

  public CombatCapableItem getTarget() { return _target; }

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
    _target = (CombatCapableItem) game.getItem( _jsonTargetId );
    if ( _target != null )
    {
      _targetOwner = _target.getOwner();
    }
    else
    {
      _targetIsMissing = true;
      _targetOwner = Player.UNKNOWN;
    }
  }
}
