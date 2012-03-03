package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.map.ControlLevel;

/** Raise the wizard's level by 1, but the city's population is sacrificed and is reduced to level 1. */
public class DarkRitual extends AbstractSpell
{
  public DarkRitual() { super(); } // GSON only
  public DarkRitual( Wizard wizard )
  {
    super( wizard );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleNotAtDominatedCity( game ) ) return false;
    return true;
  }

  protected void processOrder( Game game )
  {
    PopCenter city = game.getPopCenter( _subject.getLocation() );
    city.setLevel( 1 );
    _subject.incrementLevel();
    addPlayerEvent( game, _subject, _subject.getName() + " sacrificed the city of " + city.getName() + " for dark power!" );
  }

  private boolean handleNotAtDominatedCity( Game game )
  {
    PopCenter city = game.getPopCenter( _subject.getLocation() );
    if ( (city == null) ||
         !city.getOwner().equals( _subject.getOwner() ) ||
         (city.getLocation().getRegion().getControlLevel( _subject.getOwner() ) != ControlLevel.Domination) )
    {
      String message = _subject.getName() + " wasn't at a city in a region we dominate and could not attempt the dark ritual.";
      addPlayerEvent( game, _subject, message );
      return true;
    }
    return false;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " attempt a dark ritual.";
  }
}
