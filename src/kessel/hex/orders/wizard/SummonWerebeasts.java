package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.TroopType;
import kessel.hex.domain.Wizard;
import kessel.hex.map.Terrain;

public class SummonWerebeasts extends AbstractRaiseUnit
{
  public SummonWerebeasts() { super(); } // GSON only
  public SummonWerebeasts( Wizard wizard )
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
    if ( _subject.getLocation().getTerrain() != Terrain.Forest )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " must be in a forest to summon werebeasts." );
      return true;
    }
    return false;
  }

  protected TroopType getTroopType()
  {
    return TroopType.WEREBEAST;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " recruit werebeasts";
  }
}
