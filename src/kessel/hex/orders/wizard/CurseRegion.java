package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Game;
import kessel.hex.domain.Wizard;
import kessel.hex.map.Region;

/** Damage troops of other players in the wizard's region by 10% combat value and reduce morale 10%. */
public class CurseRegion extends AbstractSpell
{
  public CurseRegion() { super(); } // GSON only
  public CurseRegion( Wizard wizard )
  {
    super( wizard );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetProtected( game ) ) return false;
    return true;
  }

  protected void processOrder( Game game )
  {
    Region affectedRegion = _subject.getLocation().getRegion();
    for ( Army army : game.getAllPlayerArmies() )
    {
      if ( !army.getOwner().equals( _subject.getOwner() ) &&
           army.getLocation().getRegion().equals( affectedRegion ) )
      {
        army.adjustMorale( -10 );
        for ( ArmyUnit armyUnit : army.getUnits() )
        {
          int damage = (int) (0.10 * armyUnit.getSize());
          armyUnit.increaseCasualties( damage );
        }
        publishArmyAffectedEvents( army, game );
      }
    }
  }

  private void publishArmyAffectedEvents( Army victim, Game game )
  {
    addPlayerEvent( game, _subject, "The regional curse affected " + victim.getName() );
    String victimMessage = victim.getName() + " suffered morale loss and desertion due to the curse the region.";
    addPlayerEvent( game, _subject, victimMessage );
  }

  private boolean handleTargetProtected( Game game )
  {
    if ( isRegionProtected( game, _subject.getLocation().getRegion() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " could not curse " + _subject.getLocation().getRegion().getName() +
                                      " because the region was magically shielded." );
      return true;
    }
    return false;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " curse enemy armies in " + _subject.getLocation().getRegion().getName();
  }

  protected void updateViews( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " cursed enemy armies in " + _subject.getLocation().getRegion().getName() );
  }
}
