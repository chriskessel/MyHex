package kessel.hex.admin;

import kessel.hex.domain.GameItem;
import kessel.hex.domain.Player;
import kessel.hex.map.Location;
import kessel.hex.map.Region;
import kessel.hex.util.Tuple;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** The map pane for the admin display. */
public class AdminMapPane extends AbstractMapPane
{
  private final AdminDisplay _adminDisplay;

  AdminMapPane( AdminDisplay adminDisplay, AbstractGameDisplay abstractGameDisplay )
  {
    super( abstractGameDisplay );
    _adminDisplay = adminDisplay;
  }

  public void paint( Graphics g )
  {
    super.paint( g );
  }

  protected boolean canPaint()
  {
    return _adminDisplay._game != null;
  }

  protected List<GameItem> getGameItems()
  {
    if ( _adminDisplay._game.isCreated() )
    {
      return new ArrayList<>( _adminDisplay._game.getAllGameItems() );
    }
    return Collections.emptyList();
  }

  protected List<GameItem> getGameItems( Tuple hex )
  {
    if ( _adminDisplay._game.isCreated() )
    {
      return _adminDisplay._game.getAllGameItems( hex );
    }
    return Collections.emptyList();
  }

  protected Map<Tuple, Location> getLocations()
  {
    return _adminDisplay._game.getMap().getLocationsByHex();
  }

  protected void paintExtras( Graphics g )
  {
    // Nothing to do.
  }

  protected List<String> getRegionNames()
  {
    List<String> names = new ArrayList<>();
    for ( Region region : _adminDisplay._game.getMap().getRegions() )
    {
      names.add( region.getName() );
    }
    return names;
  }

  protected List<String> getPlayerNames()
  {
    List<String> playerNames = new ArrayList<>();
    for ( Player player : _adminDisplay._game.getPlayers() )
    {
      playerNames.add( player.getName() );
    }
    return playerNames;
  }
}
