package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.Wizard;

/** Make all diplomatic activities in the region more likely to succeed, including troop diplomacy like DemandSurrender. */
public class CharmRegion extends AbstractSpell
{
  public CharmRegion() { super(); } // GSON only
  public CharmRegion( Wizard wizard )
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
    // Nothing, just the existence of the order matters. The diplomatic orders will adjust to it.
  }

  private boolean handleTargetProtected( Game game )
  {
    if ( isRegionProtected( game, _subject.getLocation().getRegion() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " could not charm " + _subject.getLocation().getRegion().getName() +
                                      " because the region was magically shielded." );
      return true;
    }
    return false;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " charms the populace of " + _subject.getLocation().getRegion().getName();
  }

  public int getCharmLevel()
  {
    return _subject.getLevel();
  }

  protected void updateViews( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " charmed the populace of " + _subject.getLocation().getRegion().getName() );
  }
}
