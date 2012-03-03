package kessel.hex.admin;

import kessel.hex.domain.GameItem;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Location;
import kessel.hex.map.Region;
import kessel.hex.orders.AbstractMoveFigure;
import kessel.hex.orders.Order;
import kessel.hex.orders.agent.AbstractAgentScoutRegion;
import kessel.hex.orders.agent.AbstractAssassinateFigure;
import kessel.hex.orders.agent.AgentReconLocation;
import kessel.hex.orders.agent.SabotageEmbassy;
import kessel.hex.orders.agent.SabotagePopCenter;
import kessel.hex.orders.army.AbstractArmyMove;
import kessel.hex.orders.diplomat.MapRegion;
import kessel.hex.orders.diplomat.TakeRegionCensus;
import kessel.hex.orders.king.ImproveEmbassy;
import kessel.hex.orders.wizard.Scry;
import kessel.hex.orders.wizard.TeleportSelf;
import kessel.hex.util.Tuple;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** The map pane for the player display. */
public class PlayerMapPane extends AbstractMapPane
{
  private final PlayerDisplay _playerDisplay;

  PlayerMapPane( PlayerDisplay playerDisplay, AbstractGameDisplay abstractGameDisplay )
  {
    super( abstractGameDisplay );
    _playerDisplay = playerDisplay;
  }

  protected boolean canPaint() { return _playerDisplay._player != null; }

  protected List<GameItem> getGameItems()
  {
    List<GameItem> items = new ArrayList<>();
    if ( _playerDisplay._player != null )
    {
      items.addAll( _playerDisplay._player.getKnownItems() );
      items.addAll( _playerDisplay._player.getAllItems() );
    }
    return items;
  }

  protected List<GameItem> getGameItems( Tuple hex )
  {
    if ( (hex == null) || (_playerDisplay._player == null) )
    {
      return Collections.emptyList();
    }
    else
    {
      return _playerDisplay._player.getKnownItems( hex );
    }
  }

  protected List<String> getRegionNames()
  {
    List<String> names = new ArrayList<>();
    for ( Region region : _playerDisplay._player.getKnownRegions() )
    {
      names.add( region.getName() );
    }
    return names;
  }

  protected List<String> getPlayerNames()
  {
    return _playerDisplay._player.getGameView().getPlayerNames();
  }

  protected Map<Tuple, Location> getLocations()
  {
    return _playerDisplay._player.getKnownLocations();
  }

  @SuppressWarnings({ "ChainOfInstanceofChecks" })
  protected void paintExtras( Graphics g )
  {
    // Paint anything from an order in progress.
    if ( _playerDisplay._actionInProgress != null )
    {
      _playerDisplay._actionInProgress.paint( g );
    }
    else
    {
      // Paint whatever selected in the active orders panel.
      Order order = ((PlayerDetailsPane) _playerDisplay._detailsPane).getSelectedOrder();
      if ( order != null )
      {
        if ( order instanceof AbstractArmyMove )
        {
          PaintOrderUtil.paintArmyMove(
            g, (PlayerDetailsPane) _playerDisplay._detailsPane, ((AbstractArmyMove) order).getMovementSteps() );
        }
        else if ( order instanceof AbstractMoveFigure )
        {
          PaintOrderUtil.paintConnectHexes( g, (PlayerDetailsPane) _playerDisplay._detailsPane,
            order.getSubject().getLocation().getCoord(), ((AbstractMoveFigure) order).getNewBase().getLocation().getCoord() );
        }
        else if ( order instanceof TeleportSelf )
        {
          PaintOrderUtil.paintConnectHexes( g, (PlayerDetailsPane) _playerDisplay._detailsPane,
            order.getSubject().getLocation().getCoord(), ((TeleportSelf) order).getNewBase().getLocation().getCoord() );
        }
        else if ( order instanceof ImproveEmbassy )
        {
          for ( GameItem item : _playerDisplay._player.getKnownItems() )
          {
            if ( (item instanceof PopCenter) &&
                 (((PopCenter) item).getType() == PopCenter.PopType.City) &&
                 item.getLocation().getRegion().getName().equals( ((ImproveEmbassy) order).getRegion() ) )
            {
              PaintOrderUtil.paintConnectHexes( g, (PlayerDetailsPane) _playerDisplay._detailsPane,
                order.getSubject().getLocation().getCoord(), item.getLocation().getCoord() );
              break;
            }
          }
        }
        else if ( order instanceof AbstractAssassinateFigure )
        {
          PaintOrderUtil.paintConnectHexes(
            g, (PlayerDetailsPane) _playerDisplay._detailsPane,
            order.getSubject().getLocation().getCoord(),
            ((AbstractAssassinateFigure) order).getTargetBase().getLocation().getCoord() );
        }
        else if ( order instanceof AgentReconLocation )
        {
          PaintOrderUtil.paintConnectHexes(
            g, (PlayerDetailsPane) _playerDisplay._detailsPane,
            order.getSubject().getLocation().getCoord(), ((AgentReconLocation) order).getTarget() );
        }
        else if ( order instanceof Scry )
        {
          PaintOrderUtil.paintConnectHexes(
            g, (PlayerDetailsPane) _playerDisplay._detailsPane,
            order.getSubject().getLocation().getCoord(), ((Scry) order).getTarget() );
        }
        else if ( order instanceof SabotagePopCenter )
        {
          PaintOrderUtil.paintConnectHexes(
            g, (PlayerDetailsPane) _playerDisplay._detailsPane,
            order.getSubject().getLocation().getCoord(), ((SabotagePopCenter) order).getTarget().getLocation().getCoord() );
        }
        else if ( order instanceof SabotageEmbassy )
        {
          PaintOrderUtil.paintConnectHexes(
            g, (PlayerDetailsPane) _playerDisplay._detailsPane,
            order.getSubject().getLocation().getCoord(), ((SabotageEmbassy) order).getRegionalCity().getLocation().getCoord() );
        }
        else if ( order instanceof AbstractAgentScoutRegion )
        {
          PaintOrderUtil.paintConnectHexes(
            g, (PlayerDetailsPane) _playerDisplay._detailsPane,
            order.getSubject().getLocation().getCoord(),
            ((AbstractAgentScoutRegion) order).getRegionalCity().getLocation().getCoord() );
        }
        else if ( order instanceof MapRegion )
        {
          PaintOrderUtil.paintConnectHexes(
            g, (PlayerDetailsPane) _playerDisplay._detailsPane,
            order.getSubject().getLocation().getCoord(),
            ((MapRegion) order).getRegionalCity().getLocation().getCoord() );
        }
        else if ( order instanceof TakeRegionCensus )
        {
          PaintOrderUtil.paintConnectHexes(
            g, (PlayerDetailsPane) _playerDisplay._detailsPane,
            order.getSubject().getLocation().getCoord(),
            ((TakeRegionCensus) order).getRegionalCity().getLocation().getCoord() );
        }
      }
    }
  }
}
