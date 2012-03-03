package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.TroopType;
import kessel.hex.domain.Wizard;
import kessel.hex.map.Terrain;

public class SummonDragons extends AbstractRaiseUnit
{
  public SummonDragons() { super(); } // GSON only
  public SummonDragons( Wizard wizard )
  {
    super( wizard );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleWrongTerrain( game ) ) return false;
    return true;
  }

  boolean handleWrongTerrain( Game game )
  {
    if ( _subject.getLocation().getTerrain() != Terrain.Mountain )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " must be in a mountain to summon dragons." );
      return true;
    }
    return false;
  }

  protected TroopType getTroopType()
  {
    return TroopType.DRAGON;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " recruit dragons";
  }
}
