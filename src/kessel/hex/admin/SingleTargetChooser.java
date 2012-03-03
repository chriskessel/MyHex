package kessel.hex.admin;

import kessel.hex.domain.GameItem;
import kessel.hex.util.Tuple;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.List;

/** Listens for mouse events to select single targets for an action. */
class SingleTargetChooser<X> extends TargetListener
{
  /** The action that caused us to go looking for a target. */
  private final SingleTargetActions.SingleTargetAction _action;

  private final AbstractGameDisplay _gameDisplay;

  SingleTargetChooser( AbstractGameDisplay gameDisplay, SingleTargetActions.SingleTargetAction action )
  {
    _action = action;
    _gameDisplay = gameDisplay;
  }

  // Handles choosing an item via a single click, such as a hex.
  public void mouseClicked( MouseEvent e )
  {
    if ( (_action.getTargetState() == TargetAction.TargetState.SELECTING) && _action.isQuickTarget() )
    {
      Tuple hex = _gameDisplay.getCellMetrics().getGridByPixel( e.getX(), e.getY() );
      List<X> targets = _action.checkForTargetsAtHex( hex );
      if ( targets.size() == 1 )
      {
        _action.targetAcquired( targets.get( 0 ) );
      }
      else if ( targets.size() > 1 )
      {
        throw new RuntimeException( "Code logic failure, multiple targets from one click." );
      }
    }
  }

  // Supports target selection via popup. Displays the popup and the originating action handles the selection event.
  public void mousePressed( MouseEvent e )
  {
    if ( (_action.getTargetState() == TargetAction.TargetState.SELECTING) && !_action.isQuickTarget() )
    {
      Tuple hex = _gameDisplay.getCellMetrics().getGridByPixel( e.getX(), e.getY() );
      if ( !_gameDisplay.isOnMap( hex ) ) return;

      List<X> targets = _action.checkForTargetsAtHex( hex );
      if ( !targets.isEmpty() )
      {
        // Show a popup menu so the player can select which target.
        JPopupMenu targetMenu = new JPopupMenu();
        for ( X item : targets )
        {
          JMenuItem menuItem = new SingleTargetMenuItem( item );
          menuItem.addActionListener( _action );
          targetMenu.add( menuItem );
        }
        targetMenu.show( e.getComponent(), e.getX(), e.getY() );
      }
    }
  }

  static class SingleTargetMenuItem<X> extends JMenuItem
  {
    private final X _item;

    SingleTargetMenuItem( X item )
    {
      super( (item instanceof GameItem) ? ((GameItem) item).getLongStatusName() : item.toString() );
      _item = item;
    }

    public X getItem() { return _item; }
  }
}
