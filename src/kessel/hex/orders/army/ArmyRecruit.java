package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Game;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.TroopType;
import kessel.hex.map.ControlLevel;
import kessel.hex.orders.Order;

/** Recruit from the local population center. */
public class ArmyRecruit extends AbstractArmyOrder
{
  transient boolean _wasFreeCapitolRecruit = false;

  public ArmyRecruit() { super(); } // GSON only
  public ArmyRecruit( Army army )
  {
    super( army );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleNotOnOwnedPop( game ) ) return false;
    if ( handleTownTappedOut( game ) ) return false;
    if ( handleTownAlreadyRecruitedTwice( game ) ) return false;
    if ( handleNotEnoughMoney( game ) ) return false;
    return true;
  }

  protected boolean handleSubjectInactive( Game game )
  {
    // Inactive armies are allowed to recruit.
    return false;
  }

  private boolean handleNotOnOwnedPop( Game game )
  {
    PopCenter pop = game.getPopCenter( _subject.getLocation().getCoord() );
    if ( (pop == null) ||
         (pop.getType() == PopCenter.PopType.Hamlet) ||
         !pop.getOwner().equals( _subject.getOwner() ) )
    {
      addPlayerEvent( game, _subject,
        _subject.getName() + " cannot recruit as they're aren't on a kingdom controlled town or city." );
      return true;
    }
    return false;
  }

  private boolean handleTownTappedOut( Game game )
  {
    if ( isFreeCapitolRecruit() ) return false;
    PopCenter pop = game.getPopCenter( _subject.getLocation().getCoord() );
    if ( pop.getLevel() == 1 )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " cannot recruit as the population center is too small (level 1)." );
      return true;
    }
    return false;
  }

  private boolean handleTownAlreadyRecruitedTwice( Game game )
  {
    if ( isFreeCapitolRecruit() ) return false;
    PopCenter pop = game.getPopCenter( _subject.getLocation().getCoord() );
    int timesRecruited = countRecruitAttempts( pop );
    if ( timesRecruited == 2 )
    {
      addPlayerEvent( game, _subject,
        _subject.getName() + " cannot recruit as the population center has no more recruits this turn." );
      return true;
    }
    return false;
  }

  private boolean handleNotEnoughMoney( Game game )
  {
    if ( isFreeCapitolRecruit() ) return false;
    if ( _subject.getOwner().getGold() < getOrderCost() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " lacked sufficient funds to hire recruits." );
      return true;
    }
    return false;
  }

  private boolean isFreeCapitolRecruit()
  {
    boolean hasKingdomRecruitsAvailable = _subject.getOwner().getKingdomTroopsAvailable() > 0;
    boolean isOnCapitol = _subject.getLocation().equals( _subject.getOwner().getCapitol().getLocation() );
    return isOnCapitol && hasKingdomRecruitsAvailable;
  }

  @SuppressWarnings({ "RawUseOfParameterizedType" })
  private int countRecruitAttempts( PopCenter pop )
  {
    int timesRecruited = 0;
    for ( Order order : pop.getOrdersExecuted() )
    {
      if ( order instanceof ArmyRecruit )
      {
        timesRecruited++;
      }
    }
    return timesRecruited;
  }

  public void processOrder( Game game )
  {
    if ( isFreeCapitolRecruit() )
    {
      _wasFreeCapitolRecruit = true;
      _subject.getOwner().recruitKingdomArmyUnit( game, _subject );
    }
    else
    {
      PopCenter pop = game.getPopCenter( _subject.getLocation().getCoord() );
      ControlLevel controlLevel = _subject.getOwner().getControlLevel( _subject.getLocation().getRegion() );
      if ( (pop.getType() == PopCenter.PopType.City) &&
           (controlLevel == ControlLevel.Domination) )
      {
        _subject.addUnit( new ArmyUnit( game.generateUniqueId(), TroopType.REGIONAL, game.getTurn() ) );
      }
      else
      {
        _subject.addUnit( new ArmyUnit( game.generateUniqueId(), TroopType.LEVY, game.getTurn() ) );
      }
      checkForRecruitingDegradation( pop );
    }
  }

  /** The 2nd recruiting attempt on a pop in a turn drops it's population level. */
  private void checkForRecruitingDegradation( PopCenter pop )
  {
    pop.addOrderExecuted( this );
    int recruitedCount = countRecruitAttempts( pop );
    if ( recruitedCount == 2 )
    {
      pop.degradeLevel();
    }
  }

  public int getOrderCost()
  {
    if ( _wasFreeCapitolRecruit )
    {
      return 0;
    }
    else
    {
      return (int) (Game.BASE_HAMLET_PRODUCTION * Game.GOLD_GRANULARITY * 2.0);
    }
  }

  public String getShortDescription()
  {
    return _subject.getName() + " recruit";
  }
}
