package kessel.hex.admin;

import kessel.hex.domain.GameItem;
import kessel.hex.map.Location;
import kessel.hex.map.Region;
import kessel.hex.util.Tuple;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Listens for mouse events for selections. Knows exactly what types of things can be selected. */
class SingleTargetListener extends MouseAdapter
{
  /** The action that caused us to go looking for a target. */
  private final SingleTargetActions.SingleTargetAction _action;

  /** The types of targets that are valid. */
  private final List<Class> _selectionTypes;
  private final AbstractGameDisplay _gameDisplay;

  SingleTargetListener(
    AbstractGameDisplay gameDisplay, SingleTargetActions.SingleTargetAction action, List<Class> selectionTypes )
  {
    _action = action;
    _selectionTypes = selectionTypes;
    if ( _selectionTypes.isEmpty() ) { throw new RuntimeException( "Must have non-zero number of selection types" ); }
    _gameDisplay = gameDisplay;
  }

  public void mousePressed( MouseEvent e )
  {
    Tuple hex = _gameDisplay.getCellMetrics().getGridByPixel( e.getX(), e.getY() );
    if ( !_gameDisplay.isOnMap( hex ) ) return;

    // If the target type is a Tuple or Region, one click and we've got what we need.
    if ( _selectionTypes.size() == 1 && _selectionTypes.get( 0 ).equals( Tuple.class ) )
    {
      _action.targetAcquired( hex );
    }
    else if ( _selectionTypes.size() == 1 && _selectionTypes.get( 0 ).equals( Region.class ) )
    {
      Location location = _gameDisplay.getLocationAtHex( hex );
      if ( !location.getRegion().equals( Region.UNKNOWN_REGION ) )
      {
        _action.targetAcquired( location.getRegion().getName() );
      }
    }
    else // Target is a GameItem. Pop up a menu so the user can pick which item in the hex.
    {
      // Get a list of the items that can be selected at the clicked hex.
      List<GameItem> targetsAtHex = new ArrayList<>( _gameDisplay.getItemsAtHex( hex ) );
      for ( Iterator<GameItem> iterator = targetsAtHex.iterator(); iterator.hasNext(); )
      {
        Object next = iterator.next();
        if ( !_selectionTypes.contains( next.getClass() ) )
        {
          iterator.remove();
        }
      }

      // If there's anything to select, show a popup menu so the player can select which target.
      if ( !targetsAtHex.isEmpty() )
      {
        JPopupMenu targetMenu = new JPopupMenu();
        for ( GameItem item : targetsAtHex )
        {
          JMenuItem menuItem = new SingleTargetMenuItem( item );
          menuItem.addActionListener( _action );
          targetMenu.add( menuItem );
        }
        targetMenu.show( e.getComponent(), e.getX(), e.getY() );
      }
    }
  }

  static class SingleTargetMenuItem extends JMenuItem
  {
    private final GameItem _item;

    SingleTargetMenuItem( GameItem item )
    {
      super( item.getLongStatusName() );
      _item = item;
    }

    public GameItem getItem() { return _item; }
  }
}
